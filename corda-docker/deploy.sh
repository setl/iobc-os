
#!/bin/bash

echo
echo " ____    _____      _      ____    _____ "
echo "/ ___|  |_   _|    / \    |  _ \  |_   _|"
echo "\___ \    | |     / _ \   | |_) |   | |  "
echo " ___) |   | |    / ___ \  |  _ <    | |  "
echo "|____/    |_|   /_/   \_\ |_| \_\   |_|  "
echo
echo "Running stack deploy to docker swarm."
echo

DELAY="$2"

: ${DELAY:="20"}

#Run docker stack deploy to deploy Corda nodes on the cluster
echo "Deploying corda-nodes stack..."
set -x
docker stack deploy -c ./docker-stack-cordanodes.yaml corda-nodes
set +x
sleep $DELAY

#Run docker stack deploy to deploy web servers on the cluster
echo "Deploying webservers stack..."
set -x
docker stack deploy -c ./docker-stack-webservers.yaml webservers
set +x
sleep $DELAY

echo
echo " _____   _   _   ____   "
echo "| ____| | \ | | |  _ \  "
echo "|  _|   |  \| | | | | | "
echo "| |___  | |\  | | |_| | "
echo "|_____| |_| \_| |____/  "
echo

exit 0