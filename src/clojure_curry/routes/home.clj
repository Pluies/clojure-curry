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

(defn home-page [request]
  (layout/render
    "home.html"
    (merge {:authenticated? (authenticated? request)
            :username (username request)
            :orders (db/get-orders)}
           (select-keys (:flash request) [:name :message :errors]))))

(defn order-page [request]
  (layout/render
    "order.html"
    (merge {:authenticated? (authenticated? request)
            :username (username request)
            :curries (db/get-curries)}
           (select-keys (:flash request) [:curry :hotness :garlic :errors]))))

(defn login-page [request]
  (layout/render "login.html"))

(defn changepass-page [request]
  (layout/render "changepass.html"))

(defn changepass!
  [request]
  (let [password (get-in request [:form-params "password"])
        email    (get-in request [:form-params "email"])]
    (db/set-password! {:email email :password password})
    (layout/render "changepass.html")))

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
    (do
      (db/create-order! (merge {:user_email (:email session)
                                :garlic false ; default: no garlic naan
                                :timestamp (java.util.Date.)}
                               params))
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
      ; TODO fix this
      (db/set-password! {:email "florent@catalyst.net.nz" :password (hs/encrypt password)})
      (let [found-password (:pass (first user))]
        (if (and found-password (hs/check password found-password))
          (let [next-url (get-in request [:query-params :next] "/")
                updated-session (merge session {:identity (keyword email)
                                                :email email})]
            (prn next-url)
            (-> (redirect next-url)
                (assoc :session updated-session)))
          (redirect "/login"))))))

(defn logout! [req]
  (assoc (redirect "/") :session nil))

(defroutes home-routes
  (GET  "/" request (home-page request))
  (POST "/order" request (order! request))
  (GET  "/order" request (order-page request))
;  )
;
;(defroutes auth-routes
  (GET  "/login" [] login-page)
  (POST "/login" [] login!)
  (GET  "/changepass" [] changepass-page)
  (POST "/changepass" [] changepass!)
  (POST "/logout" [] logout!))

