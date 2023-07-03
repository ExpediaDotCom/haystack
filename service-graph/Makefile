.PHONY: all clean build report-coverage node-finder graph-builder snapshotter release

PWD := $(shell pwd)

clean:
	mvn clean

build: clean
	mvn package

node-finder:
	mvn verify -DfinalName=haystack-service-graph-node-finder -pl node-finder -am

graph-builder:
	mvn verify -DfinalName=haystack-service-graph-graph-builder -pl graph-builder -am

snapshotter:
	mvn verify -DfinalName=haystack-service-graph-snapshotter -pl snapshotter -am

all: clean node-finder graph-builder snapshotter

# build all and release
release: clean node-finder graph-builder snapshotter
	cd node-finder && $(MAKE) release
	cd graph-builder && $(MAKE) release
	cd snapshotter && $(MAKE) release
	./.travis/deploy.sh

# run coverage tests
report-coverage:
	mvn clean scoverage:test scoverage:report-only
	open target/site/scoverage/index.html
