---
title: Haystack Universal Search
sidebar_label: Universal Search
---

Haystack's Universal Searchbar (USB) is a powerful way to drill deep into available data and show only the information that you want to see. 

## Simple Workflow
Tabs for the different UI subsystems are displayed based on what query is submitted. Provided all UI subsystems are enabled in [base.js](https://github.com/ExpediaDotCom/haystack-ui/blob/master/server/config/base.js), the default view will show the [Service Graph](https://expediadotcom.github.io/haystack/docs/ui/ui_service_graph.html) and service performance tabs. 

![Default View](/haystack/img/universal_search_default.png)

To view information about a specific service, submit the query **`serviceName=example-service`**. This will render the [traces](https://expediadotcom.github.io/haystack/docs/ui/ui_traces.html), [trends](https://expediadotcom.github.io/haystack/docs/ui/ui_trends.html), [alerts](https://expediadotcom.github.io/haystack/docs/ui/ui_alerts.html), and service graph (detail view) tabs for the supplied service **`example-service`**. 

Supplying an **`operationName=example-operation`** query will display only traces, trends, and alerts that include that operation name.

![Simple View](/haystack/img/universal_search.png)

## Complex Workflow

Any key is searchable in the USB, provided it is [whitelisted](https://github.com/ExpediaDotCom/haystack/blob/98cb086ed5d5740f698cb5d946811bc38154f2df/deployment/terraform/modules/haystack-apps/kubernetes/ui/variables.tf#L39). Any key that is searched on other than **`serviceName`** and **`operationName`** will only display the traces tab. 

Adding multiple key/value pairs to the USB will only display traces that fulfill the submitted criteria, inclusively. That means if you supply both **`serviceName=example-service`** and **`error=true`**, the traces will have at least one span that is from **`example-service`** and at least one span that is tagged as an error. 

![Detailed Query](/haystack/img/universal_search_detailed.png)

You can drill even deeper by creating a nested query. This is done with the following syntax: **`(serviceName=example-service error=true)`**. This will display only traces with at least one span that is from **`example-service`** *AND* is tagged as an **error**. The notable difference is that in the above condition, the error does not have to be in a **`example-service`** span, while in this condition it will be. 

![Nested Query](/haystack/img/universal_search_nested.png)
