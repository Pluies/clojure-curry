#!/bin/bash

USER=clojure-curry

useradd -r $USER

# Create log dir
mkdir -p /var/log/clojure-curry
chown $USER:$USER /var/log/clojure-curry

# Create DB/work dir
mkdir -p /var/lib/clojure-curry/
chown $USER:$USER /var/lib/clojure-curry

