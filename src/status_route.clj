(ns status-route
  (:require [clojure.test :refer [function?]]
            [clojure.walk :refer [postwalk]]

            [manifold.deferred :as d]
            [aleph.http :as http]))

(def apply-functions
  (partial postwalk
           (fn [x]
             (if (function? x)
               (x)
               x))))

(defn- resolve-dependency-
  [url]
  (d/chain
   (http/get url
             {:accept :json
              :as :json})
   :body))

(defn- status-
  [{:keys [data dependencies]}]
  (merge data
         (when (seq dependencies)
           {:dependencies @(apply d/zip (map resolve-dependency- dependencies))})))

(defn status
  [args]
  (status- (apply-functions args)))
