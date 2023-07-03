.PHONY: integration_test release all

MAVEN := ../mvnw

export DOCKER_ORG := expediadotcom
export DOCKER_IMAGE_NAME := haystack-timeseries-aggregator

docker_build:
	docker build -t $(DOCKER_IMAGE_NAME) -f build/docker/Dockerfile .

integration_test:
	${MAVEN} scoverage:integration-check

# build jar, docker image and run integration tests
all: docker_build integration_test

# build all and release
release: docker_build
	../deployment/scripts/publish-to-docker-hub.sh
