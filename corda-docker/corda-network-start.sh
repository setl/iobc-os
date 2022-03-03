#!/bin/bash

BRIDGE_NETWORK_NAME=setlnetwork
COMPOSE_FILE=docker-compose.yaml

export CORDA_DOCKER_HOME=`dirname $0`

function deploy() {
    echo "Attempting to deploy the corda network containers."
    set -x
    docker-compose -f $COMPOSE_FILE up -d 2>&1
    { set +x; } 2>/dev/null
    if [ $? -ne 0 ]; then
        echo "docker-compose up failed!"
        exit 1
    fi
}

function createBridgeNetwork() {
    # Check if network exist
    NETWORK_ID=$(docker network ls -f name=$BRIDGE_NETWORK_NAME -q)
    if [ $NETWORK_ID ]
    then
        echo "'$BRIDGE_NETWORK_NAME' bridge network already exists with ID: '$NETWORK_ID'."
    fi

    # Create network on bridge network
    echo "Attempting to create external bridge network $BRIDGE_NETWORK_NAME"
    set -x
    docker network create --attachable --driver bridge $BRIDGE_NETWORK_NAME
    { set +x; } 2>/dev/null
    if [ $? -ne 0 ]; then
        echo "An error occured while attempting to create overlay network \"$BRIDGE_NETWORK_NAME\"."
        exit 1
    fi
}

createBridgeNetwork
sleep 10
deploy