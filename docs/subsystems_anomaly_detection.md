---
title: Anomaly Detection
sidebar_label: Anomaly Detection
---

<div class="note"><b>Note:</b> This topic describes work in progress. The released component may vary from what's described here. This topic will be updated when the code for Dependencies is released.</div>

The Anomaly Detection subsystem is responsible for identifying anomalies in services' health and triggering alerts. By default, Anomaly Detection will monitor for anomalies in duration, success percentage, and count (number of operations over time) for all operations of all services sending data to Haystack.

The Trends subsystem is responsible for generating duration, count, and success/failure trends for all services sending tracing data to Haystack. The Anomaly Detection subsystem will consume the Trends data and attempt to automatically learn what normal operation looks like so that it can raise alerts when it detects abnormal operation.
