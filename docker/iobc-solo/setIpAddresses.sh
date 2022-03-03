docker network create iobc-all

docker network connect iobc-all rpcnode
docker network connect iobc-all kafka
docker network connect iobc-all fabric-ca-org1
docker network connect iobc-all fabric-org1-peer0
docker network connect iobc-all fabric-org2-peer0
docker network connect iobc-all fabric-orderer

export BESU_HOST=`docker inspect rpcnode | jq '.[].NetworkSettings.Networks."iobc-all".IPAddress' | sed "s/\"//g"`
export KAFKA_HOST=`docker inspect kafka | jq '.[].NetworkSettings.Networks."iobc-all".IPAddress' | sed "s/\"//g"`
export FABRIC_CA_HOST=`docker inspect fabric-ca-org1 | jq '.[].NetworkSettings.Networks."iobc-all".IPAddress' | sed "s/\"//g"`
export FABRIC_HOST=`docker inspect fabric-org1-peer0 | jq '.[].NetworkSettings.Networks."iobc-all".IPAddress' | sed "s/\"//g"`

echo BESU_HOST=$BESU_HOST
echo KAFKA_HOST=$KAFKA_HOST
echo FABRIC_HOST=$FABRIC_HOST
echo FABRIC_CA_HOST=$FABRIC_CA_HOST

