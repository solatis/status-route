(ns test-suite
  (:require  [clojure.test :refer :all]

             [aleph.http :as http]
             [status-route.compojure :refer [handler]]))

(defmacro with-servers
  [launch-server servers & body]
  `(let [closers# (doall (map ~launch-server ~servers))]
     (try
       ~@body
       (finally
         (doseq [close# closers#]
           (close#))))))

(def default-endpoint "http://localhost:1337/status")

(defn single-server [launch-server]
  (let [data {:status "ok"}]
    (with-servers
      launch-server
      [{:model {:id :single
                :data data}
        :opts {:port 1337}}]
      (let [response @(http/get default-endpoint
                                {:accept :json
                                 :as :json})]
        (is (= 200 (-> response :status)))
        (is (= {:single data} (-> response :body)))))))

(defn default-values [launch-server]
  (let [data {:status "ok"}]
    (with-servers
      launch-server
      [{:model {:data data}
        :opts {:port 1337}}]
      (let [response @(http/get default-endpoint
                                {:accept :json
                                 :as :json})]
        (is (= 200 (-> response :status)))
        (is (= {:default data} (-> response :body)))))))

(defn function-as-data [launch-server]
  (let [data (fn [] {:status "ok"})]
    (with-servers
      launch-server
      [{:model {:data data}
        :opts {:port 1337}}]
      (let [response @(http/get default-endpoint
                                {:accept :json
                                 :as :json})]
        (is (= 200 (-> response :status)))
        (is (= {:default {:status "ok"}} (-> response :body)))))))

(defn multi-server [launch-server]
  (let [data {:status "ok"}]
    (with-servers
      launch-server
      [{:model {:id :multi-one
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

(defn dynamic-dependencies [launch-server]
  (let [data {:status "ok"}]
    (with-servers
      launch-server
      [{:model {:id :multi-one
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

(defn recursion-deadlock [launch-server]
  (let [data {:status "ok"}]
    (with-servers
      launch-server
      [{:model {:id :multi-one
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


(defn deep-dependencies [launch-server]
  (let [data {:status "ok"}]
    (with-servers
      launch-server
      [{:model {:id :multi-one
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

(defn undeep-dependencies [launch-server]
  (let [data {:status "ok"}]
    (with-servers
      launch-server
      [{:model {:id :multi-one
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
