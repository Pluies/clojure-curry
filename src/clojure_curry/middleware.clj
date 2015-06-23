(ns clojure-curry.middleware
  (:require [clojure-curry.session :as session]
            [clojure-curry.layout :refer [*servlet-context*]]
            [clojure-curry.db.core :as db]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [clojure.java.io :as io]
            [selmer.middleware :refer [wrap-error-page]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.session-timeout :refer [wrap-idle-session-timeout]]
            [ring.middleware.session.memory :refer [memory-store]]
            [ring.middleware.format :refer [wrap-restful-format]]
            ; Authentication
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.accessrules :refer [wrap-access-rules]]
            [buddy.auth :refer [authenticated?]]
            [clojure-curry.layout :refer [*identity*]]
            
            ))

(defn wrap-servlet-context [handler]
  (fn [request]
    (binding [*servlet-context*
              (if-let [context (:servlet-context request)]
                ;; If we're not inside a servlet environment
                ;; (for example when using mock requests), then
                ;; .getContextPath might not exist
                (try (.getContextPath context)
                     (catch IllegalArgumentException _ context)))]
      (handler request))))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (timbre/error t)
        {:status 500
         :headers {"Content-Type" "text/html"}
         :body (-> "templates/error.html" io/resource slurp)}))))

(defn wrap-dev [handler]
  (if (env :dev)
    (-> handler
        wrap-error-page
        wrap-exceptions)
    handler))

(defn wrap-csrf [handler]
  (wrap-anti-forgery handler))

(defn wrap-formats [handler]
  (wrap-restful-format handler :formats [:json-kw :transit-json :transit-msgpack]))

(defn on-unauthorized [request response]
  (redirect "/login"))

(defn any-access
  [request]
  true)

(defn admin-access
  [request]
  (if-not :authenticated?
    false
    (let [current-email (:email (:session request))]
      (boolean (:admin (first (db/get-user {:email current-email})))))))

(def rules
  [{:pattern #"^/admin$"
    :handler admin-access}
   {:pattern #"^/$"
    :handler any-access}
   {:pattern #"^/login$"
    :handler any-access}
   {:pattern #"^/.+"
    :handler authenticated?}])

(defn wrap-identity [handler]
  (fn [request]
    (binding [*identity* (or (get-in request [:session :identity]) nil)]
      (handler request))))

(defn wrap-auth [handler]
  (-> handler
      wrap-identity
      (wrap-access-rules {:rules rules :on-error on-unauthorized})
      (wrap-authentication (session-backend))))

(defn wrap-base [handler]
  (-> handler
      wrap-dev
      wrap-auth
      (wrap-idle-session-timeout
        {:timeout (* 60 60 24 30) ; 30 days
         :timeout-response (redirect "/")})
      wrap-formats
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (assoc-in  [:session :store] (memory-store session/mem))))
      wrap-servlet-context
      wrap-internal-error))

