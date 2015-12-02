(ns clojure-curry.db.core
  (:require
    [yesql.core :refer [defqueries]]
    [environ.core :refer [env]]
    [clojure.java.io :as io]))

(def db-store (env :database-url))

(def db-spec
  {:classname   "org.h2.Driver"
   :subprotocol "h2"
   :subname     db-store
   :make-pool?  true
   :naming      {:keys   clojure.string/lower-case
                 :fields clojure.string/upper-case}})

(defqueries "sql/queries.sql" {:connection db-spec})
