(ns yada-test
  (:require [clojure.test :refer :all]
            [yada.yada :refer [listener]]
            [bidi.verbose :refer [leaf]]

            [test-suite :as suite]
            [status-route.yada :refer [handler]]))

(defn- handler->routes [h]
  (leaf "/status" h))

(defn- launch-server
  [{:keys [model opts]}]
  (-> (listener (handler->routes (handler model)) opts)
      :close))

(deftest single-server (suite/single-server launch-server))
(deftest default-values (suite/default-values launch-server))
(deftest function-as-data (suite/function-as-data launch-server))
(deftest multi-server (suite/multi-server launch-server))
(deftest dynamic-dependencies (suite/dynamic-dependencies launch-server))
(deftest deep-dependencies (suite/deep-dependencies launch-server))
(deftest undeep-dependencies (suite/undeep-dependencies launch-server))
(deftest recursion-deadlock (suite/recursion-deadlock launch-server))
