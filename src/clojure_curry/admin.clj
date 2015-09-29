(ns clojure-curry.admin
  (:require [clojure-curry.layout :as layout]
            [clojure-curry.db.core :as db]
            [ring.util.response :refer [redirect]]
            [buddy.hashers :as hs]
            [buddy.auth :refer (authenticated?)]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [clj-time.predicates :as pr]
            [clj-time.coerce :as c]))

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

(defn users
  []
  (db/get-users))

(defn users-with-balance []
  (map #(merge (first (db/get-balance %)) %)
       (users)))

(defn users-owing-money []
  (filter #(> 0 (:balance %))
          (users-with-balance)))

(defn users-owed-money []
  (filter #(< 0 (:balance %))
          (users-with-balance)))

(defn should-smart-email? [user]
  "User should be emailed if they've ordered in the last month"
  (let [orders (db/get-users-orders user)]
    (and (not (empty? orders))
         (let [last-order (c/from-sql-time (:timestamp (first orders)))
               a-month-ago (t/minus (t/now) (t/months 1))]
           (t/after? last-order a-month-ago)))))

(defn users-to-email []
  (filter #(let [pref (:email_preference %)]
             (or (= "always" pref)
                 (and (= "smart" pref)
                      (should-smart-email? %))))
          (users)))


(users-to-email)
