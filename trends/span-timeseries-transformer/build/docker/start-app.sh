#!/bin/bash

[ -z "$JAVA_XMS" ] && JAVA_XMS=1024m
[ -z "$JAVA_XMX" ] && JAVA_XMX=1024m

set -e
JAVA_OPTS="${JAVA_OPTS} \
-javaagent:${APP_HOME}/${JMXTRANS_AGENT}.jar=${APP_HOME}/jmxtrans-agent.xml \
-XX:+UseG1GC \
-Xloggc:/var/log/gc.log \
-XX:+PrintGCDetails \
-XX:+PrintGCDateStamps \
-XX:+UseGCLogFileRotation \
-XX:NumberOfGCLogFiles=5 \
-XX:GCLogFileSize=2M \
-Xmx${JAVA_XMX} \
-Xms${JAVA_XMS} \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.port=1098 \
-Dapplication.name=${APP_NAME} \
-Dapplication.home=${APP_HOME}"

exec java ${JAVA_OPTS} -jar "${APP_HOME}/${APP_NAME}.jar"
