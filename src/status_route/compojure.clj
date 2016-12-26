(ns status-route.compojure
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [ring.util.http-response :refer [ok]]
            [status-route :refer [status]]))

(defn handler
  "Wraps status route into a yada handler"
  [args]

  (routes
   (GET "/" {query :query-params}
        (ok (status args {:context (get query "context")
                          :depth (get query "depth")})))))
