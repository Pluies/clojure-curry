(ns clojure-curry.routes.home
  (:require [clojure-curry.layout :as layout]
            [clojure-curry.db.core :as db]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [ring.util.response :refer [redirect]]
            [buddy.hashers :as hs]
            [buddy.auth :refer (authenticated?)]
            [clojure.java.io :as io]))

(defn current-user [request]
  (if-not :authenticated?
    nil
    (let [current-email (:email (:session request))]
      (first (db/get-user {:email current-email})))))

(defn username [request]
  (:first_name (current-user request)))

(defn is-admin?
  [request]
  (if-not :authenticated?
    false
    (let [current-email (:email (:session request))]
      (boolean (:admin (first (db/get-user {:email current-email})))))))

(defn can-remove [session order]
  (let [current-email (:email session)
        found-email (:email order)]
    (= current-email found-email)))


(defn home-page [request]
  (layout/render
    "home.html"
    (merge {:authenticated? (authenticated? request)
            :username (username request)
            :orders (map
                      #(merge {:can-remove (can-remove (:session request) %)} %)
                      (db/get-todays-orders))}
           (select-keys (:flash request) [:name :message :errors]))))

(defn order-page [request]
  (layout/render
    "order.html"
    (merge {:authenticated? (authenticated? request)
            :username (username request)
            :curries (db/get-curries)}
           (select-keys (:flash request) [:curry :hotness :garlic :errors]))))

(defn admin-page [request]
  (layout/render
    "admin.html"
    (merge {:users (db/get-users)}
           (select-keys (:flash request) [:user :errors]))))

(defn login-page [request]
  (layout/render "login.html"))

(defn create-user! [request]
  (let [params (:params request)]
    (db/create-user! (assoc params
                            :pass (hs/encrypt (:password params))
                            :admin (boolean (:admin params))))
    (redirect "/admin")))


(defn add-payment! [request]
  (let [params (:params request)]
    (db/add-payment! (assoc params
                            :confirmed true
                            :timestamp (java.util.Date.)))
    (redirect "/admin")))

(defn changepass-page [request]
  (layout/render
    "changepass.html"
    (merge {:authenticated? (authenticated? request)
            :is-admin? (is-admin? request)
            :username (username request)}
           (select-keys (:flash request) [:email :password :errors]))))

(defn changepass!
  [request]
  (let [password (hs/encrypt (get-in request [:form-params "password"]))
        email    (if (is-admin? request)
                   (get-in request [:form-params "email"])
                   (:email (:session request)))]
    (db/set-password! {:email email :password password})
    (redirect "/")))

(defn validate-order [params]
  (first
    (b/validate
      params
      :curry v/required
      :hotness v/required)))

(defn order! [{:keys [params session]}]
  (if-let [errors (validate-order params)]
    (-> (redirect "/order")
        (assoc :flash (assoc params :errors errors)))
    (let [curry (first (db/get-curry {:name (:curry params)}))
          vegetarian (:vegetarian curry)
          garlic (if (nil? (:garlic params))
                   false ; default: no garlic naan
                   (Boolean/valueOf (:garlic params)))
          price (+ (if vegetarian 10 11)
                   (if garlic 1 0))]
      (db/create-order! (merge {:user_email (:email session)
                                :price price
                                :timestamp (java.util.Date.)}
                               params))
      (redirect "/"))))

(defn validate-delete-order [params]
  nil)

(defn delete-order! [request order-id]
  (if-let [errors (validate-delete-order order-id)]
    (-> (redirect "/order")
        (assoc :flash (assoc (:params request) :errors errors)))
    (do
      (db/delete-order! {:id order-id})
      (redirect "/"))))

(defn login!
  "Check request username and password against authdata username and passwords.
  On successful authentication, set appropriate user into the session and
  redirect to the value of (:query-params (:next request)).
  On failed authentication, renders the login page."
  [request]
  (let [email    (get-in request [:form-params "email"])
        password (get-in request [:form-params "password"])
        session  (:session request)
        user     (db/get-user {:email email})]
    (do
      (let [found-password (:pass (first user))]
        (if (and found-password (hs/check password found-password))
          (let [next-url (get-in request [:query-params :next] "/")
                updated-session (merge session {:identity (keyword email)
                                                :email email})]
            (-> (redirect next-url)
                (assoc :session updated-session)))
          (redirect "/login"))))))

(defn logout! [req]
  (assoc (redirect "/") :session nil))

(defroutes home-routes
  (GET    "/" request (home-page request))
  (GET    "/order" request (order-page request))
  (POST   "/order" request (order! request))
  (POST   "/delete-order/:id" [request id] (delete-order! request id))
  ;  )
  ;
  ;(defroutes auth-routes
  (GET    "/admin" [] admin-page)
  (POST   "/admin/create-user" [] create-user!)
  (POST   "/admin/add-payment" [] add-payment!)
  (GET    "/login" [] login-page)
  (POST   "/login" [] login!)
  (GET    "/changepass" [] changepass-page)
  (POST   "/changepass" [] changepass!)
  (POST   "/logout" [] logout!))

