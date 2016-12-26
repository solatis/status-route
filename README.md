# status-route

This library handles the status route reporting and aggregation, tailored towards
 more complex service dependency hierarchies you frequently see in microservices. 

It is specifically designed to not only allow the reporting of a single server's
status, but to also include all its dependencies (and their dependencies) in a
single report, resulting in an aggregated overview of an entire cluster's health.

Notable features:

 * Get an aggregated overview of a node's status including its dependencies
 * Asynchronously requests all dependencies' status
 * Ability to detect and prevent "infinite recursion"
 * Flexibly restrict the depth you want to query using a query parameter

## Ring adapter

## Yada adapter
