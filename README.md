# iobc


### besu docker stack

In a convenient folder....
```
npx quorum-dev-quickstart \
--clientType besu \
--outputPath ./quorum-test-network \
--monitoring none \
--privacy false \
--orchestrate false \
--quorumKeyManager false

cd quorum-test-network/

./run.sh

```
### kafka docker stack (in kafka-docker module)
```
docker compose -p ioc33 up -d
```


### iobc-server docker

iobc-server may be run with the following docker command

```
docker run  -it --rm -e EXTRA_CMD_LINE_OPTIONS="--spring.kafka.bootstrap-servers=kafka:9093 --setl.iobc.besu.address=http://rpcnode:8545/" --name xxxx --network quorum-dev-quickstart dreg.ad.setl.io/setl/iobc-server:100-SNAPSHOT
``` 

stop with

`docker stop xxxx`
