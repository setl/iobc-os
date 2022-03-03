FROM azul/zulu-openjdk:8u192

## Add packages, clean cache, create dirs, create corda user and change ownership
RUN apt-get update && \
    apt-get -y upgrade && \
    apt-get -y install bash curl unzip && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /opt/webserver && \
    mkdir -p /opt/webserver/jars && \
    addgroup corda && \
    useradd corda -g corda -m -d /opt/webserver && \
    chown -R corda:corda /opt/webserver && \
    chown -R corda:corda /opt/webserver/jars

ENV MY_SERVER_PORT=8080 \
    MY_RPC_HOST=master.setl.net \
    MY_RPC_PORT=10003 \
    MY_RPC_USER=user2 \
    MY_RPC_USERPW=test \
    JARS_FOLDER="/opt/webserver/jars" \
    MY_ORGNAME="" \
    MY_LOCALITY="" \
    MY_COUNTRY="" \
    #PATH=$PATH:/opt/corda/bin \
    JVM_ARGS="-XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap "

##CORDA JAR
COPY --chown=corda:corda jars /opt/webserver/jars
#COPY jars /opt/webserver/jars
##CONFIG MANIPULATOR JAR
#COPY --chown=corda:corda config-exporter.jar /opt/corda/config-exporter.jar
##CONFIG GENERATOR SHELL SCRIPT
#COPY --chown=corda:corda generate-config.sh /opt/corda/bin/config-generator
##CORDA RUN SCRIPT
COPY --chown=corda:corda run-server.sh /run-server.sh
RUN chmod +x /run-server.sh && \
    chmod +xr /opt/webserver/jars/* && \
    sync
    #chown corda:corda -r /opt/webserver/jars
##BASE CONFIG FOR GENERATOR
#COPY --chown=corda:corda starting-node.conf /opt/corda/starting-node.conf

USER "corda"
EXPOSE ${MY_SERVER_PORT}
WORKDIR /opt/webserver
CMD ["/run-server.sh"]