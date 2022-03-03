#!/bin/bash

OVERLAY_NETWORK_NAME=setlnetwork

function initSwarm() {
    echo "Attempting to initialize the swarm network."
    set -x
    docker swarm init
    { set +x; } 2>/dev/null
    if [ $? -ne 0 ]; then
        echo "docker swarm init failed!"
        exit 1
    fi
}

function createOverlayNetwork() {
    # Check if network exist
    NETWORK_ID=$(docker network ls -f name=$OVERLAY_NETWORK_NAME -q)
    if [ $NETWORK_ID ]
    then
        echo "'$OVERLAY_NETWORK_NAME' overlay network already exists with ID: '$NETWORK_ID'."
    fi

    # Create network on swarm network
    echo "Attempting to create swarm overlay network $OVERLAY_NETWORK_NAME"
    set -x
    docker network create --attachable --subnet=16.10.0.0/16 --gateway=16.10.0.1 --driver overlay $OVERLAY_NETWORK_NAME
    { set +x; } 2>/dev/null
    if [ $? -ne 0 ]; then
        echo "An error occured while attempting to create overlay network \"$OVERLAY_NETWORK_NAME\"."
        exit 1
    fi
}

initSwarm
createOverlayNetwork
