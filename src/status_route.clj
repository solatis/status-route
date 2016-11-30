(ns status-route
  (:require [clojure.test :refer [function?]]
            [clojure.walk :refer [postwalk]]

            [clojure.string :as str]
            [cemerick.url :as c]
            [manifold.deferred :as d]
            [aleph.http :as http]))

(def apply-functions
  (partial postwalk
           (partial #(if (function? %1) (%1) %1))))

(defn parse-context [context]
  (str/split context #","))

(defn merge-context [a b]
  (let [a' (if (coll? a) a [a])
        b' (if (coll? b) b [b])]
    (-> (conj a' b')
        (flatten)
        (set))))

(defn- build-dependency-query-
  [self-id self-context query]

  (let [dependency-context (-> (parse-context (get query "context"))
                               (merge-context self-context)
                               (conj (name self-id)))]
    (println "dependency-context = " (pr-str dependency-context))
    (->> (str/join "," dependency-context)
         (assoc query "context"))))

(defn- build-dependency-url-
  [self-id self-context base-url]

  (-> (update-in (c/url base-url)
                 [:query]
                 (partial build-dependency-query- self-id self-context))

      (str)))

(defn- resolve-dependency-
  [self-id context url]

  (println "self-id = " (pr-str self-id))
  (println "parent-context = " (pr-str context))
  (println "url = " (pr-str url))

  (d/chain
   (http/get (build-dependency-url- self-id context url)
             {:accept :json
              :as :json})
   :body))

(defn- status-
  [{:keys [id data dependencies]} context]

  (merge {id data}
         (when (seq dependencies)
           {:dependencies @(apply d/zip (map (partial resolve-dependency- id context) dependencies))})))

(defn status
  [args context]
  (status- (apply-functions args) (parse-context context)))
