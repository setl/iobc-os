#!/bin/bash

COMPOSE_FILE=docker-compose.yaml

function networkDown() {
  echo "# Tearing down corda network"
  docker-compose -f $COMPOSE_FILE down --volumes --remove-orphans
  # Don't remove the generated artifacts -- note, the ledgers are always removed
}

networkDown