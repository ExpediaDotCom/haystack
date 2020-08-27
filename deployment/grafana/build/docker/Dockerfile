FROM alpine:3.12.0

MAINTAINER Haystack <haystack@expedia.com>

RUN apk --no-cache add curl

COPY grafana_run.sh /
COPY datasources/* /datasources/
COPY dashboards/* /dashboards/

RUN chmod -R 755 /grafana_run.sh

ENTRYPOINT ["/bin/sh","/grafana_run.sh"]