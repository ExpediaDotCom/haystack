.PHONY: docker_build integration_test release

export DOCKER_ORG := expediadotcom
export DOCKER_IMAGE_NAME := haystack-trace-indexer
PWD := $(shell pwd)

docker_build:
	docker build -t $(DOCKER_IMAGE_NAME) -f build/docker/Dockerfile .

prepare_integration_test_env: docker_build
	docker-compose -f build/integration-tests/docker-compose.yml -p sandbox up -d

integration_test: prepare_integration_test_env
	# run tests in a container so that we can join the docker-compose network and talk to kafka, elasticsearch and trace-backend
	cd ../ && ./mvnw -q integration-test -pl indexer -am
	# clean up the docker
	docker-compose -f build/integration-tests/docker-compose.yml -p sandbox stop
	docker rm $(shell docker ps -a -q)
release:
	../deployment/scripts/publish-to-docker-hub.sh
