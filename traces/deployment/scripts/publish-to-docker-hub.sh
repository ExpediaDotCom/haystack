#!/bin/bash

set -e

QUALIFIED_DOCKER_IMAGE_NAME=$DOCKER_ORG/$DOCKER_IMAGE_NAME
echo "DOCKER_ORG=$DOCKER_ORG, DOCKER_IMAGE_NAME=$DOCKER_IMAGE_NAME, QUALIFIED_DOCKER_IMAGE_NAME=$QUALIFIED_DOCKER_IMAGE_NAME"
echo "BRANCH=$BRANCH, TAG=$TAG, SHA=$SHA"

# login
docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD

# Add tags
if [[ $TAG =~ ([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
    echo "releasing semantic versions"

    unset MAJOR MINOR PATCH
    MAJOR="${BASH_REMATCH[1]}"
    MINOR="${BASH_REMATCH[2]}"
    PATCH="${BASH_REMATCH[3]}"

    # for tag, add MAJOR, MAJOR.MINOR, MAJOR.MINOR.PATCH and latest as tag
    # publish image with tags
    docker tag $DOCKER_IMAGE_NAME $QUALIFIED_DOCKER_IMAGE_NAME:$MAJOR
    docker push $QUALIFIED_DOCKER_IMAGE_NAME:$MAJOR
    
    docker tag $DOCKER_IMAGE_NAME $QUALIFIED_DOCKER_IMAGE_NAME:$MAJOR.$MINOR
    docker push $QUALIFIED_DOCKER_IMAGE_NAME:$MAJOR.$MINOR
    
    docker tag $DOCKER_IMAGE_NAME $QUALIFIED_DOCKER_IMAGE_NAME:$MAJOR.$MINOR.$PATCH
    docker push $QUALIFIED_DOCKER_IMAGE_NAME:$MAJOR.$MINOR.$PATCH

    docker tag $DOCKER_IMAGE_NAME $QUALIFIED_DOCKER_IMAGE_NAME:latest
    docker push $QUALIFIED_DOCKER_IMAGE_NAME:latest

elif [[ "$BRANCH" == "master" ]]; then
    echo "releasing master branch"

    # for 'master' branch, add SHA as tags
    docker tag $DOCKER_IMAGE_NAME $QUALIFIED_DOCKER_IMAGE_NAME:$SHA

    # publish image with tags
    docker push $QUALIFIED_DOCKER_IMAGE_NAME
fi
