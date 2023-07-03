.PHONY: docker_build release prepare_integration_test_env

export DOCKER_ORG := expediadotcom
export DOCKER_IMAGE_NAME := haystack-http-span-collector
PWD := $(shell pwd)

docker_build:
	docker build -t $(DOCKER_IMAGE_NAME) -f build/docker/Dockerfile .

prepare_integration_test_env: docker_build
	docker-compose -f build/integration-tests/docker-compose.yml -p sandbox up -d

	# kafka sometimes take time to start
	sleep 30

integration_test: prepare_integration_test_env
	# run tests in a container so that we can join the docker-compose network and talk to kafka
	docker run \
		-it \
		--network=sandbox_default \
		-v $(PWD)/..:/src \
		-v ~/.m2:/root/.m2 \
		-w /src \
		maven:3.5.0-jdk-8 \
		mvn scoverage:integration-check -pl http -am

	# stop all the containers
	docker-compose -f build/integration-tests/docker-compose.yml -p sandbox stop
	docker rm $(shell docker ps -a -q)
	docker volume rm $(shell docker volume ls -q)

release: docker_build
	../deployment/scripts/publish-to-docker-hub.sh
