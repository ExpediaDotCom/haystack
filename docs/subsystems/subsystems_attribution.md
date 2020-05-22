---
title: Attribution Components
sidebar_label: Attribution
---

<div class="note"><i><b>Note:</b> This topic describes work in progress. The released component may vary from what's described here.</i> </div>
<br>

[Haystack-attribution](https://github.com/ExpediaDotCom/haystack-attribution) is a group of components that are used to monitor and manage the health of a Haystack deployment. It contains the following subsystems: 

### Haystack-attributor Module

For smooth operation of a Haystack cluster, traffic should be monitored on a service-to-service basis. If a service goes berserk, it could put a severe load on the whole infrastructure and impact all other services. Attributor is the component responsible for measuring and keeping track of the volume of spans sent by each service.

In addition to few basic attribution fields like span count, span size, operation count; one can also do attribution on the basis of span tags.

For using this attributed data, there are two ways provided for persistence. One can either setup the email for getting the attribution report as an email at regular intervals. Or other way is to dump the report in s3 and use it the way user wants to use.

### Haystack-attribution-persistence-email Module
This sub-component fetches the stats from attributor via a rest call and sends the attributed data / stats as an email. It is scheduled as a cron job in our k8s cluster to send the email (for previous calender day, currently).

What it does:

* rest call to attributor to fetch service stats for previous day's time window
* creates an email template for stats
* email using aws ses service / smtp to all the ids in configuration

### Haystack-attribution-persistence-s3 Module

Component which persists the attributed data to s3 for consumption by any other tool. This component is also scheduled as a cron job in our k8s cluster to persist the cost attribution details as csv in S3.

What it does:

* rest call to attributor to fetch service stats (for previous day's time window, currently)
* transforms the stats data using a transformer (transformer can be overridden)
* persist the transformed data to s3


