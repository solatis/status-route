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
  (if (or (nil? context)
          (str/blank? context))
    []
    (str/split context #",")))

(defn merge-context [a b]
  (let [a' (if (coll? a) a [a])
        b' (if (coll? b) b [b])]
    (-> (conj a' b')
        (flatten)
        (set))))

(defn- build-dependency-query-
  [self-id self-context self-depth query]

  (let [dependency-context (-> (parse-context (get query "context" ""))
                               (merge-context self-context)
                               (conj self-id))]

    (-> query
        (assoc "context" (str/join "," dependency-context))
        (cond-> self-depth (assoc "depth" self-depth)))))



(defn- build-dependency-url-
  [self-id self-context self-depth base-url]

  (-> (update-in (c/url base-url)
                 [:query]
                 (partial build-dependency-query-
                          self-id
                          self-context
                          self-depth))
      (str)))

(defn- resolve-dependency-
  [self-id context depth url]

  (d/chain
   (http/get (build-dependency-url- self-id context depth url)
             {:accept :json
              :as :json})
   :body))

(defn- status-
  [{:keys [id dependencies deep? data]
    :or {id :default
         dependencies []
         data {}}}
   {:keys [context
           depth]
    :or {context ""}}]

  (let [self-id (name id)
        context' (parse-context context)]
    {id (merge data
               (when (and
                      ;; We actually have dependencies
                      (seq dependencies)

                      ;; Prevent infinite recursion deadlock by checking we
                      ;; do not see ourselves in our context
                      (not (some #(= self-id %) context'))

                      ;; And in case somebody do not wants a deep-search, limit
                      ;; our search depth to just one dependency level.
                      (or (nil? depth)
                          (< (count context') (read-string depth))))
                 {:dependencies @(apply d/zip
                                        (map (partial resolve-dependency-
                                                      self-id
                                                      context'
                                                      depth)
                                             dependencies))}))}))

(defn status
  [args query]
  (status- (apply-functions args) query))
