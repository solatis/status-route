(ns status-route.yada-adapter
  (:require [yada.yada :as yada]
            [bidi.verbose :refer [branch leaf]]))

(defn handler
  "Wraps status route into a yada handler"
  []
  (yada/handler nil))
