(ns clojure-curry.routes.home
  (:require [clojure-curry.layout :as layout]
            [clojure-curry.db.core :as db]
            [clojure-curry.admin :as admin]
            [environ.core :refer [env]]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [ring.util.response :refer [redirect]]
            [buddy.hashers :as hs]
            [buddy.auth :refer (authenticated?)]
            [clojure.java.io :as io]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [clj-time.predicates :as pr]))

(defn current-user [request]
  (if-not :authenticated?
    nil
    (:user (:session request))))

(defn balance [request]
  (-> (db/get-balance {:email (-> (:session request)
                                  :email)})
      first
      :balance))

(defn username [request]
  (-> (:session request) :user :first_name))

(defn can-remove [session order]
  (let [current-email (:email session)
        found-email (:email order)]
    (= current-email found-email)))

(defn top-bar-variables
  "Compute variables for the top-bar, to be used in the template"
  [request]
  (if-let [authenticated (authenticated? request)]
    {:authenticated? true
     :admin?         (admin/is-admin? request)
     :user           (current-user request)
     :username       (username request)
     :balance        (balance request)}
    {:authenticated false}))

(defn home-page [request]
  (layout/render
    "home.html"
    (merge (top-bar-variables request)
           {:server-time (l/local-now)
            ; Order is until 11:30am
            :time-to-order? (and
                              (pr/thursday? (l/local-now))
                              (> 1130 (read-string (f/unparse (f/formatter-local "HHmm") (l/local-now)))))
            :orders (map
                      #(merge {:can-remove (can-remove (:session request) %)} %)
                      (db/get-todays-orders))}
           (select-keys (:flash request) [:name :message :errors]))))

(defn order-page [request]
  (layout/render
    "order.html"
    (merge (top-bar-variables request)
           {:curries (db/get-curries)}
           (select-keys (:flash request) [:curry :hotness :garlic :errors]))))

(defn login-page [request]
  (layout/render "login.html"))

(defn admin-page [request]
  (layout/render
    "admin.html"
    (merge (top-bar-variables request)
           {:users (db/get-users)}
           {:users_owing_money (admin/users-owing-money)}
           {:users_owed_money (admin/users-owed-money)}
           (select-keys (:flash request) [:user :errors]))))

(defn changepass-page [request]
  (layout/render
    "changepass.html"
    (merge (top-bar-variables request)
           (select-keys (:flash request) [:email :password :errors]))))

(defn payment-page [request]
  (layout/render
    "payment.html"
    (merge (top-bar-variables request)
           {:bank-account-number (env :bank-account-number)}
           (select-keys (:flash request) [:email :password :errors]))))

(defn changepass!
  [request]
  (let [password (hs/encrypt (get-in request [:form-params "password"]))
        email    (if (admin/is-admin? request)
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

(defn pay! [{:keys [params session]}]
  nil)

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
        user     (first (db/get-user {:email email}))]
    (do
      (let [found-password (:pass user)]
        (if (and found-password (hs/check password found-password))
          (let [next-url (get-in request [:query-params :next] "/")
                updated-session (merge session {:identity (keyword email)
                                                :email email
                                                :user user})]
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
  (POST   "/admin/create-user" [] admin/create-user!)
  (POST   "/admin/add-payment" [] admin/add-payment!)
  (GET    "/payment" [] payment-page)
  (POST   "/payment" [] pay!)
  (GET    "/login" [] login-page)
  (POST   "/login" [] login!)
  (GET    "/changepass" [] changepass-page)
  (POST   "/changepass" [] changepass!)
  (POST   "/logout" [] logout!))

