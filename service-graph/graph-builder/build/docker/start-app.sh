#!/bin/bash

[ -z "$JAVA_XMS" ] && JAVA_XMS=1024m
[ -z "$JAVA_XMX" ] && JAVA_XMX=1024m

set -e
JAVA_OPTS="${JAVA_OPTS} \
-javaagent:${APP_HOME}/${JMXTRANS_AGENT}.jar=${APP_HOME}/jmxtrans-agent.xml \
-XX:+UseG1GC \
-Xmx${JAVA_XMX} \
-Xms${JAVA_XMS} \
-Dapplication.name=${APP_NAME} \
-Dapplication.home=${APP_HOME}"

if [[ -n "$SERVICE_DEBUG_ON" ]] && [[ "$SERVICE_DEBUG_ON" == true ]]; then
   JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=5005,server=y"
fi

exec java ${JAVA_OPTS} -jar "${APP_HOME}/${APP_NAME}.jar"
