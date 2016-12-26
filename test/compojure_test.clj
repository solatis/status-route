(ns compojure-test
  (:require [clojure.test :refer :all]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.api.sweet :refer [context]]

            [test-suite :as suite]
            [status-route.compojure :refer [handler]]))

(defn- handler->routes [h]
  (context "/status" [] h))

(defn- launch-server
  [{:keys [model opts]}]
  (let [server (run-jetty (-> (handler->routes (handler model))
                              wrap-params
                              wrap-json-response)
                          (merge {:join? false}
                                 opts))]
    (fn []
      (.stop server))))

(deftest single-server (suite/single-server launch-server))
(deftest default-values (suite/default-values launch-server))
(deftest function-as-data (suite/function-as-data launch-server))
(deftest multi-server (suite/multi-server launch-server))
(deftest dynamic-dependencies (suite/dynamic-dependencies launch-server))
(deftest deep-dependencies (suite/deep-dependencies launch-server))
(deftest undeep-dependencies (suite/undeep-dependencies launch-server))
(deftest recursion-deadlock (suite/recursion-deadlock launch-server))
