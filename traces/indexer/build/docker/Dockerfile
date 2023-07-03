FROM openjdk:8-jre
MAINTAINER Haystack <haystack@expedia.com>

ENV APP_NAME haystack-trace-indexer
ENV APP_HOME /app/bin
ENV JMXTRANS_AGENT jmxtrans-agent-1.2.6
ENV DOCKERIZE_VERSION v0.6.1

ADD https://github.com/jwilder/dockerize/releases/download/${DOCKERIZE_VERSION}/dockerize-alpine-linux-amd64-${DOCKERIZE_VERSION}.tar.gz dockerize.tar.gz
RUN tar xzf dockerize.tar.gz
RUN chmod +x dockerize

RUN mkdir -p ${APP_HOME}

COPY target/${APP_NAME}.jar ${APP_HOME}/
COPY build/docker/start-app.sh ${APP_HOME}/
COPY build/docker/jmxtrans-agent.xml ${APP_HOME}/

RUN chmod +x ${APP_HOME}/start-app.sh
ADD https://github.com/jmxtrans/jmxtrans-agent/releases/download/${JMXTRANS_AGENT}/${JMXTRANS_AGENT}.jar ${APP_HOME}/

WORKDIR ${APP_HOME}

ENTRYPOINT ["./start-app.sh"]
