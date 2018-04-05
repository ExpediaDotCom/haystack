---
title: Dependencies
sidebar_label: Dependencies
---

<div class="note"><b>Note:</b> This topic describes work in progress. The released component may vary from what's described here. This topic will be updated when the code for Dependencies is released.</div>

The Dependencies subsystem is designed for visualizing service graphs and how requests are flowing between them. It will combine data from the Alerts and Trending subsystems into a dependency graph to show the current state of the system at a glance. You will be able to drill down into the graph to get details and fine-grain data.

## Building the dependency graph

The Dependencies subsystem uses the parent-child relationships of spans to create the dependency graph. Each partial span pair (one server span and one client span) represents a call over the network. In a Kafka stream app, we will generate graph edges by buffering partial spans and emitting dependency graph edges when the span pair is complete. The collection of all edges forms the global dependency graph. The graph will be persisted in a Cassandra table as a time series, and consumed by the Dependencies UI.
