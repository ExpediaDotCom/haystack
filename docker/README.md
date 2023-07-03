- [Running Haystack using docker-compose](#running-haystack-using-docker-compose)
  * [Allocate memory to docker](#allocate-memory-to-docker)
  * [To start Haystack's traces, blobs, trends, service graph and adaptive-alerting](#to-start-haystacks-traces-blobs-trends-service-graph-and-adaptive-alerting)
  * [To start Zipkin (tracing) with Haystack's trends, service graph and adaptive-alerting](#to-start-zipkin-tracing-with-haystacks-trends-service-graph-and-adaptive-alerting)
  * [Note on composing components](#note-on-composing-components)

## Running Haystack using docker-compose

### Allocate memory to docker

Please check this [Stackoverflow answer](https://stackoverflow.com/questions/44533319/how-to-assign-more-memory-to-docker-container)

To run all of haystack and its components, __it is suggested to change the default in docker settings from `2GiB` to `6GiB`__

### To start Haystack's traces, trends, service graph and adaptive-alerting

```bash
docker-compose -f docker-compose.yml \
               -f traces/docker-compose.yml \
               -f trends/docker-compose.yml \
               -f service-graph/docker-compose.yml \
               -f adaptive-alerting/docker-compose.yml \
               -f agent/docker-compose.yml \
               -f example/traces/docker-compose.yml up
```

The command above starts haystack components, and two sample web applications with the haystack-agent. It may take a minute or two for the containers to come up and connect with each other.

Haystack's UI will be available at http://localhost:8080 

Haystack's agent will be available on host port 35000 (i.e., localhost: 35000).

[Sample application](https://github.com/ExpediaDotCom/opentracing-spring-haystack-example) has a 'frontend' and 'backend'. The 'frontend' app will be available at http://localhost:9090/hello. Sending a request to frontend will cause a call to the backend before fulfilling this request. 

Send some sample requests to the 'frontend' application by running 

```bash
run.sh
```

One can then see the traces, trends and a service-graph showing the relationship between the two applications in the UI.

### To start Haystack's traces, blobs, trends, service graph and adaptive-alerting

```bash
docker-compose -f docker-compose.yml \
               -f traces/docker-compose.yml \
               -f trends/docker-compose.yml \
               -f service-graph/docker-compose.yml \
               -f adaptive-alerting/docker-compose.yml \
               -f agent/docker-compose.yml \
               -f example/blobs/docker-compose.yml up
```

The command above starts haystack components, and two sample web applications with the haystack-agent. It may take a minute or two for the containers to come up and connect with each other.

Haystack's UI will be available at http://localhost:8080

Haystack's agent will be available in port 35000 in the host (i.e., localhost: 35000).

[Sample Application](https://github.com/ExpediaDotCom/haystack-blob-example) has a 'client' and a 'server'. The client interacts with the server listening on port `9090`. The client app will be available at `http://localhost:9091/displayMessage`. Sending a request to client will cause a call to the server before fulfilling this request.

Call the client using the link given above and then you will be able to see the traces, trends and a service-graph showing the relationship between the two applications in the UI.

Alternatively, you can also send some sample requests to the 'server' application by running

```bash
run.sh
```

### To start Zipkin (tracing) with Haystack's trends, service graph and adaptive-alerting

```bash
docker-compose -f docker-compose.yml \
               -f zipkin/docker-compose.yml \
               -f trends/docker-compose.yml \
               -f adaptive-alerting/docker-compose.yml \
               -f service-graph/docker-compose.yml up
```

The command above starts [Pitchfork](https://github.com/HotelsDotCom/pitchfork) to proxy data to [Zipkin](https://github.com/openzipkin/) and Haystack. 

Give a minute or two for the containers to come up and connect with each other.  Once the stack is up, one can use the sample application @ https://github.com/openzipkin/brave-webmvc-example and send some sample data to see traces (from Zipkin), trends and service-graph in haystack-ui @ http://localhost:8080

### Note on composing components

Note the two commands above combine a series of `docker-compose.yml` files. 

- Haystack needs at least one trace provider ( `traces/docker-compose.yml` or `zipkin/docker-compose.yml` ) and one trends provider ( `trends/docker-compose.yml` )
- One can remove `adaptive-alerting/docker-compose.yml` and `service-graph/docker-compose.yml` if those components are not required
- One can remove `examples/traces/docker-compose.yml` or `examples/blobs/docker-compose.yml` and just have `agent/docker-compose.yml` to start your application integrated with haystack to send data
- If one is using Zipkin instrument app, use `zipkin/docker-compose.yml` to send data to the stack and use trends, service-graph and adaptive-alerting as needed
- Starting the stack with only the base docker-compose.yml will start core services like kafka, cassandra and elastic-search along with haystack-ui with mock backend

```bash
docker-compose -f docker-compose.yml up
```

### Note on Adaptive Alerting

- Model Service that fetches anomaly detection model for a specific metric has been replaced with a mocked (using wiremock) to allow the stack to use a default model. Default detection model us [EWMA](https://en.wikipedia.org/wiki/EWMA_chart) 
- Model Service is being refactored to allow better model selection and we will be releasing it in the next month or two
- Alert-Notification service that dispatches alerts to either email or slack is [commented in docker-compose](adaptive-alerting/docker-compose.yml#L100) file for local testing. You can uncomment it and provide slack_token or smtp credentials via environment.
