# ![Haystack](../images/logo_small.png)

# Haystack UI
Haystack-ui is the web UI for haystack. It is the central place for visualizing processed data from various haystack sub-systems. 

Visualization tools in haystack-ui include -
* **Traces** - Distributed tracing visualization for easy root cause analysis 
* **Trends** - Visualization for vital service trends 
* **Service Dependency** [coming soon] - Real time dependency graph with health and connectivity indicators 
* **Alerts and Anomaly Detection** [coming soon] - UI for configuring and subscribing alerts 

Haystack-ui's navigation is pivoted around services. On selecting a service, you will get various visualizations tools each corresponding to a haystack sub-systems.

**Code, Deployment and Configuration**
Refer the source repo - [https://github.com/ExpediaDotCom/haystack-ui](https://github.com/ExpediaDotCom/haystack-ui)

Below is the list of major tools/pages available in Haystack UI.

## Traces 
Visualization for tracing sub-system of Haystack.

#### Search  
<TODO Add Image>

User can search for Traces going through a service. Operation can be 'all' or any specific operation from the given service. Time Picker allows presets and time range. 
Here are the available search options -
- **Service**: Service through which trace must go through
- **Operation**: Operation of the given service through which trace must go through
- **Tags**: User can specify white space separated list of tags in `key=value` format(eg. `success=false`), it will fetch Traces who have all the specified tags for provided service and operation combination. Please note that *only the tags whitelisted in haystack tracing subsystem will be eligible for search*, others will be ignored. Some special case for Tags search -
    - In case of `traceId` tag search, haystack will search for the given id across all services and ignore the provided service 
    - You can provide `minDuration=<x_milliseconds>` for searching for traces who took > x_milliseconds in total.
- **Time Range Picker**: User can select any presets or specify a custom time window.

#### Search Results
<TODO Add Image>

Trace search results are presented in tabluar format for easy visualization of the data. All the columns are sortable.
Here are details about how each column is calculated -
- **Start Time** - Start time of the first span in local timezone.
- **Root** - Operation name of the root span, root span(aka. loopback span) is the first span where user request started. Second line provides the URL(from span.tags.url in trace).
- **Success** - Status of trace. Is marked failure if any spans in trace have success marked as false.
- **Spans** - Total number of spans across all services in the trace. Second line shows upto top two services based on count in the trace.
- **Op Duration** - Total duration for the queried operation. Sum of duration of all spans of the queried operation.
- **Op Duration %** - Percentage of total duration for the queried operation as compared to duration of the trace. Could be > 100% if there are parallel calls.
- **Svc Duration** - Total duration for the queried service. Sum of duration of all spans of the queried service
- **Svc Duration %** - Percentage of total duration for the queried service as compared to duration of the trace. Could be > 100% if there are parallel calls.
- **Total Duration** - Duration of the span. It is the difference between the start time of earliest operation and the end time of last operation in the trace
  
#### Trace Details
<TODO Add Image>

Waterfall for the trace, it shows span timelines using horizontal bars and parent child relationship between them using dotted lines.

You can see more details about an individual span like Logs and Tags for that span and raw spans by clicking on the span.

Also, clicking on `Share Trace` copies a sharable persistent link for the trace.

## Trends
Visualization for vital service health trends. Haystack trends 3 metrices for each operation of all services - count, duration(tp95, tp99, median) and success %.

#### Operation Summary
<TODO Add Image>

You would get summary stats for count, duration and success % for all operation of the service on landing on Trends page. All 3 columns are sortable. You can change the duration for which you want summaries by changing `Showing summary for` dropdown.

#### Operation Trend Details 
<TODO Add Image>

Graphs for count, duraiton and success % trends. Here are some details on controls in Trend Details view -
- **Time Range** - By default the same time range as used for summary. You can change it to any preset or custom time range.
- **Metric Granularity** - By default, we select a reasonable granularity based on time range duration. You can change it to any available granularity ie. 1min, 5min, 15min.

## Servce Dependency
[Coming Soon]

## Alerts and Anomaly Detection
[Coming Soon]
