# clojure-curry

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

Create the local database with:

    lein ragtime migrate

To start a web server for the application, run:

    lein ring server

## Deploy

Create a .deb with:

    make deb

(You'll need `fpm`)

## License

Copyright © 2015 FIXME
