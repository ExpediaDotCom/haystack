---
title: Haystack Universal Search
sidebar_label: Universal Search
---

Haystack's Universal Searchbar (USB) is a powerful way to drill deep into available data and show only the information that you want to see. 

## Simple Workflow
Tabs for the different UI subsystems are displayed based on what query is submitted. Provided all UI subsystems are enabled in [base.js](https://github.com/ExpediaDotCom/haystack-ui/blob/master/server/config/base.js), the default view will show the [Service Graph](./ui_service_graph.html) and service performance tabs. 

![Default View](/haystack/img/universal_search_default.png)

To view information about a specific service, submit the query **`serviceName=example-service`**. This will render the [traces](./ui_traces.html), [trends](./ui_trends.html), [alerts](./ui_alerts.html), [service graph](./ui_service_graph.html), and [service insights](./ui_service_insights.html) tabs for the supplied service **`example-service`**. 

Adding **`operationName=example-operation`** to the search query will display only traces, trends, alerts, and a service insight view that includes that operation.

![Simple View](/haystack/img/universal_search.png)

## Complex Workflow

Any key is searchable in the USB, provided it is [whitelisted](https://github.com/ExpediaDotCom/haystack/blob/98cb086ed5d5740f698cb5d946811bc38154f2df/deployment/terraform/modules/haystack-apps/kubernetes/ui/variables.tf#L39). Any key that is searched on other than **`serviceName`** and **`operationName`** will only display the traces tab. 

Adding multiple key/value pairs to the an individual search will only display traces that fulfill the submitted criteria, exclusively. That means if you supply both **`serviceName=example-service`** and **`error=true`**, the traces will have at least one span that is from **`example-service`** and is tagged as an error. 

![Nested Query](/haystack/img/universal_search_nested.png)

You can customize even further by submitting multiple separate queries. When submitting another query, it will show up as a separate color. 
 
 ![Detailed Query](/haystack/img/universal_search_detailed.png)
 
 This will display only traces with at least one span that is from **`example-service`** and at least one (potentially separate) span that is tagged as an **error**. The notable difference is that in the first condition, the error does not have to be in a **`example-service`** span. 

Note that values that include whitespace characters must be enclosed with quotations, example: **`serviceName="example whitespace service"`**

