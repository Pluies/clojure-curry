(ns clojure-curry.admin
  (:require [clojure-curry.layout :as layout]
            [clojure-curry.db.core :as db]
            [ring.util.response :refer [redirect]]
            [buddy.hashers :as hs]
            [buddy.auth :refer (authenticated?)]))

(defn is-admin?
  [request]
  (if-not :authenticated?
    false
    (boolean (:admin (:user (:session request))))))

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

