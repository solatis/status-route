(ns status-route-test
  (:require [clojure.test :refer :all]
            [yada.yada :refer [listener]]
            [bidi.verbose :refer [leaf]]

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


(def default-routes (leaf "" (handler)))

(deftest first-test
  (with-server default-routes {:port 1337}
    (is (= 1 2))))
