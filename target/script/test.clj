; (require '[cljs.test :refer-macros [run-all-tests]])

(require 'cljs.test)

(println "Running tests for" (:id *build-config*) "...\n")


; pulled the regex out into a var to attempt to fix symbol passed problem
(def ^:dynamic *ns-regex*
  (-> (:id *build-config*)
      (name)
      (str ".*")
      (re-pattern)))

(println "*ns-regex*: " *ns-regex*)



;; a macro where I attempted to work around cljs.text/run-all-tests
;; which would not allow dynamic pattern to be passed
;; I also tried moving it into another file without luck
; (defmacro run-build-tests [prefix]
;   (println "prefix: " prefix)
;   (let [regex (re-pattern (str prefix ".*"))]
;     (println "regex: " regex)
;     `(cljs.test/run-all-tests ~regex)))


;; when calling this macro, it receievs the symbol/form, not the regex
;; object. Not sure why.
(cljs.test/run-all-tests *ns-regex*)

;; this is throwing exceptions from deeper in the compiler.
;; I believe it's because the test env isn't setup correctly?? Not sure.
; (cljs.test/run-all-tests #"tool.*")
