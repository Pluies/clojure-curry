(ns clojure-curry.email
  (:require [clojure-curry.admin :as adm]
            [environ.core :refer [env]]
            [clojurewerkz.mailer.core :refer [delivery-mode! with-settings with-defaults with-settings build-email deliver-email]]))

(delivery-mode! :sendmail)

(defn send-email-to [user]
  (deliver-email
    {:from (env :email-from)
     :to (:email user)
     :subject "Curry!"}
    "email/templates/order.mustache"
    (assoc user
           :order-url (env :order-url)
           :account-url (env :account-url))
    :text/html))

(defn send-order-reminder-email! []
  (map send-email-to
       (adm/users-to-email)))

