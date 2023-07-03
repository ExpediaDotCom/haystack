.PHONY: docker_build prepare_integration_test_env integration_test release

export DOCKER_ORG := expediadotcom
export DOCKER_IMAGE_NAME := haystack-kinesis-span-collector
PWD := $(shell pwd)

docker_build:
	docker build -t $(DOCKER_IMAGE_NAME) -f build/docker/Dockerfile .

prepare_integration_test_env: docker_build
	#copy plugin jars to test
	mvn -f ${PWD}/../ clean package -pl sample-span-decorator -am
	mkdir -p ${PWD}/plugins/decorators
	cp ${PWD}/../sample-span-decorator/target/sample-span-decorator.jar ${PWD}/plugins/decorators/.

	docker-compose -f build/integration-tests/docker-compose.yml -p sandbox up -d

	# kafka sometimes take time to start
	sleep 30

    # create the stream and dynamodb table
	docker run \
    		-it --network=sandbox_default \
    		-e "AWS_CBOR_DISABLE=1" \
    		-v $(PWD)/build/integration-tests/scripts:/scripts \
    		-w /scripts \
    		node:6.11.3 \
    		./setup.sh

integration_test: prepare_integration_test_env
	# run tests in a container so that we can join the docker-compose network and talk to kafka and kinesis
	docker run \
		-it \
		--network=sandbox_default \
		-e "AWS_CBOR_DISABLE=1" \
		-e "AWS_ACCESS_KEY=fake" \
		-e "AWS_SECRET_KEY=fake" \
		-v $(PWD)/..:/src \
		-v ~/.m2:/root/.m2 \
		-w /src \
		maven:3.5.0-jdk-8 \
		mvn scoverage:integration-check -pl kinesis -am

	# stop all the containers
	docker-compose -f build/integration-tests/docker-compose.yml -p sandbox stop
	docker rm $(shell docker ps -a -q)
	docker volume rm $(shell docker volume ls -q)

release: docker_build
	../deployment/scripts/publish-to-docker-hub.sh
