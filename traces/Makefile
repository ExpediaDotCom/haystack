.PHONY: all indexer reader backends release

PWD := $(shell pwd)

clean:
	./mvnw clean

build: clean
	./mvnw package

all: clean reader indexer  backends

report-coverage:
	./mvnw scoverage:report-only

indexer: build_indexer
	cd indexer && $(MAKE) integration_test

reader: build_reader
	cd reader && $(MAKE) integration_test

build_reader:
	./mvnw -q package -DfinalName=haystack-trace-reader -pl reader -am

build_indexer:
	./mvnw -q package -DfinalName=haystack-trace-indexer -pl indexer -am

#Backends
backends:
	cd backends && $(MAKE) all

# build all and release
release: clean indexer reader backends
	cd indexer && $(MAKE) docker_build && $(MAKE) release
	cd reader && $(MAKE) docker_build && $(MAKE) release
	cd backends && $(MAKE) release
	./.travis/deploy.sh

