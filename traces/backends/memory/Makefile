.PHONY: docker_build prepare_integration_test_env integration_test release

export DOCKER_ORG := expediadotcom
export DOCKER_IMAGE_NAME := haystack-trace-backend-memory
PWD := $(shell pwd)
SERVICE_DEBUG_ON ?= false

docker_build:
	# build docker image using existing app jar
	docker build -t $(DOCKER_IMAGE_NAME) -f build/docker/Dockerfile .

integration_test:
	cd ../../ && ./mvnw -q integration-test -pl backends/memory -am

release:
	../../deployment/scripts/publish-to-docker-hub.sh
