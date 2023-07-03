.PHONY: integration_test release

export DOCKER_ORG := expediadotcom
export DOCKER_IMAGE_NAME := haystack-service-graph-graph-builder
PWD := $(shell pwd)

docker-image:
	docker build -t $(DOCKER_IMAGE_NAME) -f build/docker/Dockerfile .

release: docker-image
	../deployment/scripts/publish-to-docker-hub.sh
