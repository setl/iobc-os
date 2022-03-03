FROM adoptopenjdk/openjdk11:centos-slim

ARG PROJ_VERSION
# Work around for docker not wishing to have build args persisted/used within the image
ENV PROJ_VERSION ${PROJ_VERSION}

#Debugging and VM
ENV JVM_DEBUG_PORT ""
ENV JVM_MAX_MEMORY 4g
ENV EXTRA_JVM_OPTIONS ""

#General config
ENV EXTRA_CMD_LINE_OPTIONS ""

#Vault config
ENV DEV_VAULT_FILE=/setlnet/dev_vault.yaml
ENV VAULT_NAME password-server
ENV VAULT_LISTEN_PORT 20565
ENV VAULT_SERVERS 20.20.200.42:20563

#RUN apk add --update tzdata
RUN yum install -y tzdata wget

HEALTHCHECK --interval=25s --timeout=3s --retries=5 --start-period=30s CMD wget -q -t 1 http://localhost:8080/actuator/health || exit 1
WORKDIR /iobc

COPY libs/iobc-server-${PROJ_VERSION}.jar .
COPY dev_vault.yaml /setlnet/

CMD if [ "x$DEV_VAULT_FILE" != "x" ] ; then export DEV_VAULT_LINE="-Dio.setl.passwd.dev_vault_file=${DEV_VAULT_FILE}" ; \
else export STD_VAULT_LINE="--setl.vault.name=${VAULT_NAME} --setl.vault.listen=${VAULT_LISTEN_PORT} --setl.vault.servers=${VAULT_SERVERS}" ;fi && \
if [ "x$JVM_DEBUG_PORT" != "x" ] ; then export DBG_LINE="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${JVM_DEBUG_PORT}" ; \
else export DBG_LINE="" ;fi && \
exec java \
    ${DEV_VAULT_LINE} \
    ${DBG_LINE} \
    -Xmx${JVM_MAX_MEMORY} \
    ${EXTRA_JVM_OPTIONS} \
    -jar iobc-server-${PROJ_VERSION}.jar \
    --management.endpoint.health.enabled=true \
    ${STD_VAULT_LINE} \
    ${EXTRA_CMD_LINE_OPTIONS}
