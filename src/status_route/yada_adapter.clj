(ns status-route.yada-adapter
  (:require [yada.yada :as yada]
            [bidi.verbose :refer [branch leaf]]))

(defn handler []
  (yada/handler nil))
