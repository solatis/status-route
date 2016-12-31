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

## Compojure adapter

```clojure
(ns my-status-routes
  (:require [compojure.api.sweet :refer [defroutes context]]
            [clj-time.core :as    t]
            [clj-time.coerce :as    c]
            [status-route.compojure :refer [handler]]))

(def start (t/now))

(defroutes status-routes
  (context "/status" [] (handler {:id :my-service
                                  :data {:status :ok
                                         :uptime (-> start
                                                     (t/interval (t/now))
                                                     (t/in-seconds))
                                         :dependencies ["http://my-child-service/status"]}))))
```

## Yada adapter
