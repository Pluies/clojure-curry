# Declare env variables

# Unix user to run the daemon as
export RUN_AS=clojure-curry
export PORT=18080
# DB url, needs to be an H2 db for now
export DATABASE_URL="/var/lib/clojure-curry/site.db"
export LOG_PATH="/var/log/clojure-curry/clojure-curry.log"
export LOG_LEVEL=info
# Where the text output of java -jar goes
export OUTPUT_LOG_PATH="/var/log/clojure-curry/output.log"
