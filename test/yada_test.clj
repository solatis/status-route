(ns yada-test
  (:require [clojure.test :refer :all]
            [yada.yada :refer [listener]]
            [bidi.verbose :refer [leaf]]
            [aleph.http :as http]

            [status-route.yada :refer [handler]]))

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
    (with-servers [{:model {:id :single
                            :data data}
                    :opts {:port 1337}}]
      (let [response @(http/get default-endpoint
                                {:accept :json
                                 :as :json})]
        (is (= 200 (-> response :status)))
        (is (= {:single data} (-> response :body)))))))

(deftest default-values
  (let [data {:status "ok"}]
    (with-servers [{:model {:data data}
                    :opts {:port 1337}}]
      (let [response @(http/get default-endpoint
                                {:accept :json
                                 :as :json})]
        (is (= 200 (-> response :status)))
        (is (= {:default data} (-> response :body)))))))

(deftest function-as-data
  (let [data (fn [] {:status "ok"})]
    (with-servers [{:model {:data data}
                    :opts {:port 1337}}]
      (let [response @(http/get default-endpoint
                                {:accept :json
                                 :as :json})]
        (is (= 200 (-> response :status)))
        (is (= {:default {:status "ok"}} (-> response :body)))))))

(deftest multi-server
  (let [data {:status "ok"}]
    (with-servers [{:model {:id :multi-one
                            :data data
                            :dependencies ["http://127.0.0.1:1338/status"]}
                    :opts {:port 1337}}
                   {:model {:id :multi-two
                            :data data}
                    :opts {:port 1338}}]
      (let [response @(http/get default-endpoint
                                {:accept :json
                                 :as :json})]
        (is (= 200 (-> response :status)))
        (is (= {:multi-one {:status "ok"
                            :dependencies [{:multi-two data}]}} (-> response :body)))))))

(deftest dynamic-dependencies
  (let [data {:status "ok"}]
    (with-servers [{:model {:id :multi-one
                            :data data
                            :dependencies (fn []
                                            ["http://127.0.0.1:1338/status"])}
                    :opts {:port 1337}}
                   {:model {:id :multi-two
                            :data data}
                    :opts {:port 1338}}]
      (let [response @(http/get default-endpoint
                                {:accept :json
                                 :as :json})]
        (is (= 200 (-> response :status)))
        (is (= {:multi-one {:status "ok"
                            :dependencies [{:multi-two data}]}} (-> response :body)))))))

(deftest recursion-deadlock
  (let [data {:status "ok"}]
    (with-servers [{:model {:id :multi-one
                            :data data
                            :dependencies ["http://127.0.0.1:1338/status"]}
                    :opts {:port 1337}}
                   {:model {:id :multi-two
                            :data data
                            :dependencies ["http://127.0.0.1:1337/status"]}
                    :opts {:port 1338}}]
      (let [response @(http/get default-endpoint
                                {:accept :json
                                 :as :json})]

        (is (= 200 (-> response :status)))
        (is (= {:multi-one {:status "ok"
                            :dependencies [{:multi-two {:status "ok"
                                                        :dependencies [{:multi-one {:status "ok"}}]}}]}} (-> response :body)))))))


(deftest deep-dependencies
  (let [data {:status "ok"}]
    (with-servers [{:model {:id :multi-one
                            :data data
                            :dependencies ["http://127.0.0.1:1338/status"]}
                    :opts {:port 1337}}
                   {:model {:id :multi-two
                            :data data
                            :dependencies ["http://127.0.0.1:1339/status"]}
                    :opts {:port 1338}}
                   {:model {:id :multi-three
                            :data data}
                    :opts {:port 1339}}]
      (let [response @(http/get default-endpoint
                                {:accept :json
                                 :as :json})]

        (is (= 200 (-> response :status)))
        (is (= {:multi-one {:status "ok"
                            :dependencies [{:multi-two {:status "ok"
                                                        :dependencies [{:multi-three {:status "ok"}}]}}]}} (-> response :body)))))))

(deftest undeep-dependencies
  (let [data {:status "ok"}]
    (with-servers [{:model {:id :multi-one
                            :data data
                            :dependencies ["http://127.0.0.1:1338/status"]
                            :deep? false}
                    :opts {:port 1337}}
                   {:model {:id :multi-two
                            :data data
                            :dependencies ["http://127.0.0.1:1339/status"]
                            :deep? false}
                    :opts {:port 1338}}
                   {:model {:id :multi-three
                            :data data
                            :deep? false}
                    :opts {:port 1339}}]

      (let [response @(http/get (str default-endpoint "?depth=1")
                                {:accept :json
                                 :as :json})]

        (is (= 200 (-> response :status)))
        (is (= {:multi-one {:status "ok"
                            :dependencies [{:multi-two {:status "ok"}}]}} (-> response :body)))))))
