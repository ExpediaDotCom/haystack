#!/bin/bash

set -e

DOCKER_ORG=expediadotcom
DOCKER_IMAGE_NAME=haystack-grafana-dashboards
QUALIFIED_DOCKER_IMAGE_NAME=$DOCKER_ORG/$DOCKER_IMAGE_NAME

echo "DOCKER_ORG=$DOCKER_ORG, DOCKER_IMAGE_NAME=$DOCKER_IMAGE_NAME, QUALIFIED_DOCKER_IMAGE_NAME=$QUALIFIED_DOCKER_IMAGE_NAME"
echo "BRANCH=$BRANCH, SHA=$SHA"

docker build -t $(DOCKER_IMAGE_NAME) -f deployment/grafana/build/docker/Dockerfile .

# login
docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD

if [[ "$BRANCH" == "master" ]]; then
    echo "releasing master branch"

    docker tag $DOCKER_IMAGE_NAME $QUALIFIED_DOCKER_IMAGE_NAME:$SHA
    docker push $QUALIFIED_DOCKER_IMAGE_NAME

    docker tag $DOCKER_IMAGE_NAME $QUALIFIED_DOCKER_IMAGE_NAME:latest
    docker push $QUALIFIED_DOCKER_IMAGE_NAME:latest
fi
