(set-env!
 :source-paths #{"src" "test"}
 :resource-paths #{"src"}

 :watcher-debounce 1000

 :dependencies
 '[[adzerk/boot-test "1.1.2" :scope "test"]
   [adzerk/bootlaces "0.1.13" :scope "test"]
   [tolitius/boot-check "0.1.3" :scope "test"]

   ;; REPL stuff
   [org.clojure/tools.namespace "0.2.11" :scope "test"]

   ;; Needed for test cases
   [yada "1.1.44" :scope "test"]

   ;; Actual real dependencies
   [manifold "0.1.5"]
   [aleph "0.4.1" :exclusions [manifold]]
   [com.cemerick/url "0.1.1"]])

(def +version+ "0.1.0")
(def +project+ 'status-route)

(require '[adzerk.boot-test :refer :all]
         '[adzerk.bootlaces :refer :all]

         ;; REPL stuff
         'clojure.tools.namespace.repl

         ;; Code quality stuff
         '[tolitius.boot-check :as check])

(task-options!
 pom {:project +project+
      :version +version+}
 push {:repo "clojars"})

(bootlaces! +version+)

(deftask dev
  "This is the main development entry point."
  []
  (set-env! :source-paths #(conj % "dev"))

  ;; Needed by tools.namespace to know where the source files are
  (apply clojure.tools.namespace.repl/set-refresh-dirs (get-env :directories)))

(deftask check-sources []
  (set-env! :source-paths #{"src" "test"})
  (comp
    (check/with-yagni)
    (check/with-eastwood)
    (check/with-kibit)
    (check/with-bikeshed)))
