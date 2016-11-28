(ns status-route-test
  (:require [clojure.test :refer :all]
            [yada.yada :refer [listener]]
            [bidi.verbose :refer [leaf]]
            [aleph.http :as http]

            [status-route.yada-adapter :refer [handler]]))

(defn- handler->routes [h]
  (leaf "/status" h))

(defn- launch-server
  [{:keys [model opts]}]
  (-> (listener (handler->routes (handler model)) opts)
      :close))

(defmacro with-servers
  [servers & body]
  `(let [closers# (doall (map launch-server ~servers))]
     (try
       ~@body
       (finally
         (doseq [close# closers#]
           (close#))))))

(def default-endpoint "http://localhost:1337/status")

(deftest single-server
  (let [data {:status "ok"}]
    (with-servers [{:model {:data data}
                    :opts {:port 1337}}]
      (let [response @(http/get default-endpoint
                                {:accept :json
                                 :as :json})]

        (is (= 200 (-> response :status)))
        (is (= data (-> response :body)))))))

(deftest multi-server
  (let [data {:status "ok"}]
    (with-servers [{:model {:data data
                            :dependencies ["http://127.0.0.1:1338/status"]}
                    :opts {:port 1337}}
                   {:model {:data data}
                    :opts {:port 1338}}]
      (let [response @(http/get default-endpoint
                                {:accept :json
                                 :as :json})]
        (is (= 200 (-> response :status)))
        (is (= {:status "ok"
                :dependencies [{:foo "http://127.0.0.1:1338/status"}]} (-> response :body)))))))
