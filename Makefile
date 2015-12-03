clean:
	rm -rf build
	rm -f **/*.deb

jar:
	DATABASE_URL=/tmp/placeholder.db lein ragtime migrate
	DATABASE_URL=/tmp/placeholder.db lein uberjar

deb: clean jar
	cp target/clojure-curry*.jar packaging/debian/var/lib/clojure-curry/
	fpm -s dir -t deb -e -C packaging/debian/ \
		--name clojure-curry \
		--architecture all \
		--maintainer "curryoverlord@catalyst.net.nz" \
		--description "App for ordering Thursday Curry"\
		--version "1.0.$(shell date +%Y%m%d-%H%M)" \
		--config-files /etc \
		--after-install packaging/scripts/after-install.sh \
		--verbose \
		.

