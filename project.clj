(defproject clojure-curry "0.1.0-SNAPSHOT"

  :description "Clojure-curry: a curry-ordering website"
  :url "http://florent:3000/curry"

  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [selmer "0.8.2"]
                 [com.taoensso/timbre "3.4.0"]
                 [com.taoensso/tower "3.0.2"]
                 [markdown-clj "0.9.66"]
                 [environ "1.0.0"]
                 [compojure "1.3.4"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-session-timeout "0.1.0"]
                 [metosin/ring-middleware-format "0.6.0"]
                 [metosin/ring-http-response "0.6.2"]
                 [bouncer "0.3.3"]
                 [prone "0.8.2"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [ring-server "0.4.0"]
                 [ragtime "0.3.9"]
                 [buddy "0.5.4"]
                 [clojurewerkz/mailer "1.2.0"]
                 [org.clojure/java.jdbc "0.3.7"]
                 [instaparse "1.4.0"]
                 [yesql "0.5.0-rc2"]
                 [com.h2database/h2 "1.4.187"]]

  :min-lein-version "2.0.0"
  :uberjar-name "clojure-curry.jar"
  :jvm-opts ["-server"]

;;enable to start the nREPL server when the application launches
  :env {:repl-port 7001}

  :main clojure-curry.core

  :plugins [[lein-ring "0.9.1"]
            [lein-environ "1.0.0"]
            [lein-ancient "0.6.5"]
            [ragtime/ragtime.lein "0.3.8"]]
  

  
  :ring {:handler clojure-curry.handler/app
         :init    clojure-curry.handler/init
         :destroy clojure-curry.handler/destroy
         :uberwar-name "clojure-curry.war"}

  
  :ragtime
  {:migrations ragtime.sql.files/migrations
   :database "jdbc:h2:./site.db"}
  
  
  
  
  :profiles
  {:uberjar {:omit-source true
             :env {:production true}
             
             :aot :all}
   :dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.3.2"]
                        [pjstadig/humane-test-output "0.7.0"]
                        ]
         
         
         
         :repl-options {:init-ns clojure-curry.core}
         :injections [(require 'pjstadig.humane-test-output)
                      (pjstadig.humane-test-output/activate!)]
         :env {:dev true}}})
