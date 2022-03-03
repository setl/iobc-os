#!/bin/bash
# Get docker sock path from environment variable
SOCK="${DOCKER_HOST:-/var/run/docker.sock}"
export DOCKER_SOCK="${SOCK##unix://}"

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
   echo "Source this script to automatically set DOCKER_SOCK"
   echo
   echo "source ./setEnv.sh"
   echo
   echo "export DOCKER_SOCK=$DOCKER_SOCK"
else
   echo "Set DOCKER_SOCK to $DOCKER_SOCK"
fi

