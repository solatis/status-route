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
  [result]
  (let [result' (apply-result result)]
    result'))
