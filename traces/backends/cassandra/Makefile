.PHONY: docker_build prepare_integration_test_env integration_test release

export DOCKER_ORG := expediadotcom
export DOCKER_IMAGE_NAME := haystack-trace-backend-cassandra
PWD := $(shell pwd)
SERVICE_DEBUG_ON ?= false

docker_build:
	# build docker image using existing app jar
	docker build -t $(DOCKER_IMAGE_NAME) -f build/docker/Dockerfile .

prepare_integration_test_env: docker_build
	# prepare environment to run integration tests against
	docker-compose -f build/integration-tests/docker-compose.yml -p sandbox up -d
	sleep 30

integration_test: prepare_integration_test_env
	cd ../../ &&./mvnw -q integration-test -pl backends/cassandra -am
	docker-compose -f build/integration-tests/docker-compose.yml -p sandbox stop
	docker rm $(shell docker ps -a -q)
	docker volume rm $(shell docker volume ls -q)

release:
	../../deployment/scripts/publish-to-docker-hub.sh
