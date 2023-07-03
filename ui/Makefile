.PHONY: clean build docker_build all release

# docker namespace
export DOCKER_ORG := expediadotcom
export DOCKER_IMAGE_NAME := haystack-ui

clean:
	npm run clean

install:
	npm install

build: clean install
	npm run build

docker_build:
	docker build -t $(DOCKER_IMAGE_NAME) -f build/docker/Dockerfile .

all: build docker_build

# build all and release
release: all
	./build/docker/publish-to-docker-hub.sh
