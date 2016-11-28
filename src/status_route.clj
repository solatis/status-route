(ns status-route
  (:require [clojure.test :refer [function?]]
            [clojure.walk :refer [postwalk]]
            [aleph.http.client :as http]))

(def apply-functions
  (partial postwalk
           (fn [x]
             (if (function? x)
               (x)
               x))))

(defn- resolve-dependency-
  [endpoint]
  {:foo endpoint})

(defn- status-
  [{:keys [data dependencies]}]

  (println "data = " (pr-str data))
  (println "dependencies = " (pr-str dependencies))

  (merge data
         (when (seq dependencies)
           {:dependencies (map resolve-dependency- dependencies)})))

(defn status
  [args]
  (status- (apply-functions args)))
