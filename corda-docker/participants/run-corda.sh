#!/usr/bin/env bash

if [[ ${JVM_ARGS} == *"Xmx"* ]]; then
  echo "WARNING: the use of the -Xmx flag is not recommended within docker containers. Use the --memory option passed to the container to limit heap size"
fi

java \
  -Dcapsule.jvm.args="${JVM_ARGS}" \
  -Dcorda.dataSourceProperties.dataSource.url="jdbc:h2:file:"${PERSISTENCE_FOLDER}"/persistence.mv.db;DB_CLOSE_ON_EXIT=FALSE;WRITE_DELAY=0;LOCK_TIMEOUT=10000" \
  -jar /opt/corda/corda.jar \
  --base-directory /opt/corda \
  --log-to-console \
  --config-file ${CONFIG_FOLDER}/node.conf ${CORDA_ARGS}

