
# Zipkin haystack-ui Quickstart Utility

Utility script to run haystack-ui with a zipkin instance as backend for traces. It spins sleuth-webmvc-example services for feeding traces in Zipkin cluster and generates some examples. It configures haystack-ui point to Zipkin V2 api and has only traces subsystem.


### PREREQUISITES

- Assumes that you have mvn and git available on your machine. 
- haystack-ui must be already installed (npm install) and built (npm build), if not please install and build before running this script


### USAGE

```> ./zipkin-quickstart```

Wait for couple of minutes till you see `Express server listening : 8080` message. Then you can hit [http://localhost:8080/search?serviceName=backend](http://localhost:8080/search?serviceName=backend) to use haystack-ui. Search for `serviceName=backend` to see pre-feeded traces coming from Zipkin backend. 


### OPTIONS

```
-h help
-d debug mode, will emit out all logs from zipkin and sleuth-webmvc-example
```
    
