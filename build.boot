(set-env!
 :source-paths #{"src" "test"}
 :resource-paths #{"src"}

 :dependencies
 '[[adzerk/boot-test "1.1.2" :scope "test"]
   [adzerk/bootlaces "0.1.13" :scope "test"]

   [com.taoensso/timbre "4.7.4" :scope "test"]
   [tolitius/boot-check "0.1.3" :scope "test"]

   [yada "1.1.44" :scope "test"]
   [aleph "0.4.2-alpha10" :scope "test"]
   [bidi "2.0.14" :scope "test"]])

(def +version+ "0.1.0")
(def +project+ 'status-route)

(require '[adzerk.boot-test :refer :all]
         '[adzerk.bootlaces :refer :all]
         '[tolitius.boot-check :as check])

(task-options!
 pom {:project +project+
      :version +version+}
 push {:repo "clojars"})

(bootlaces! +version+)

(deftask check-sources []
  (set-env! :source-paths #{"src" "test"})
  (comp
    (check/with-yagni)
    (check/with-eastwood)
    (check/with-kibit)
    (check/with-bikeshed)))
