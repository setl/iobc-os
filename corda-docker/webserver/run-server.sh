#!/usr/bin/env bash

#server.port=10050;config.rpc.host=localhost;config.rpc.port=10006;config.rpc.username=user1;config.rpc.password=test;config.org=ClientA;config.locality=London;config.country=GB

java -jar ${JARS_FOLDER}/setl-cordapp.clients.main.jar --server.port=${MY_SERVER_PORT} --config.rpc.host=${MY_RPC_HOST} --config.rpc.port=${MY_RPC_PORT} --config.rpc.username=${MY_RPC_USER} --config.rpc.password=${MY_RPC_USERPW} --config.org=${MY_ORGNAME} --config.locality=${MY_LOCALITY} --config.country=${MY_COUNTRY}

#java -jar out/artifacts/setl_cordapp_clients_main_jar/setl-cordapp.clients.main.jar --server.port=10051 --config.rpc.host=localhost --config.rpc.port=10009 --config.rpc.username=user2 --config.rpc.password=test --config.org=Setl --config.locality=London --config.country=GB

#java -jar webserver/jars/setl-cordapp.clients.main.jar --server.port=10050 --config.rpc.host=localhost --config.rpc.port=10006 --config.rpc.username=user2 --config.rpc.password=test --config.org=ClientA --config.locality=London --config.country=GB
