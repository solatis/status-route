(ns status-route.yada
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

                                (status args {:context (get query "context")
                                              :depth (get query "depth")}))}}}))
