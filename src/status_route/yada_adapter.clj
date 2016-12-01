(ns status-route.yada-adapter
  (:require [yada.yada :as yada]
            [status-route :refer [status]]))

(defn handler
  "Wraps status route into a yada handler"
  [args]

  (yada/resource
   {:description "Service status"
    :produces #{"application/edn;q=0.9"
                "application/json;q=0.8"
                "application/transit+json;q=0.7"}
    :methods {:get {:response (fn [{{:keys [query]
                                    :or {query {}}} :parameters}]
                                (status args (get query "context" "")))}}}))
