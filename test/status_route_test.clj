(ns status-route-test
  (:require [clojure.test :refer :all]
            [yada.yada :refer [listener]]
            [bidi.verbose :refer [leaf]]
            [aleph.http :as http]

            [status-route.yada-adapter :refer [handler]]))

(defmacro with-server
  [model options & body]
  `(let [server# (listener ~model ~options)]
     (try
       ~@body
       (finally
         (when-let [close# (-> server# :close)]
           (println "closing server")
           (close#))))))


(defn handler->routes [h]
  (leaf "/status" h))

(def default-endpoint "http://localhost:1337/status")

(deftest first-test
  (with-server (handler->routes (handler {:result
                                          {:status :ok
                                           :foo (fn [] :bar)}
                                          :dependencies []})) {:port 1337}

    (let [result @(http/get default-endpoint
                            {:accept :json
                             :as :json})]

      (println "result = " (pr-str result))
      (is (= 1 2)))))
