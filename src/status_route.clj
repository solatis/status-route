(ns status-route
  (:require [clojure.test :refer [function?]]
            [clojure.walk :refer [postwalk]]))

(def apply-result
  (partial postwalk
           (fn [x]
             (if (function? x)
               (x)
               x))))

(defn status
  [{:keys [result dependencies]}]

  (apply-result result))
