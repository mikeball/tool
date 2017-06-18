(ns tool.test-runner
  (:require [cljs.test :refer-macros [run-tests]]
            [tool.core-test]))

; (require 'tool.core-test)

(defn -test-main [task & args]
  (println "now running tests...")

  ; (cljs.test/run-all-tests))
  (cljs.test/run-tests 'tool.core-test))


(set! *main-cli-fn* -test-main)
(enable-console-print!)
