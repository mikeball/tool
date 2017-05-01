(require '[cljs.repl :as repl])
(require '[cljs.repl.node :as node])

(println "Starting a REPL for" (:id *build-config*) "...")

(let [{:keys [src compiler]} *build-config*]
  (cljs.repl/repl (node/repl-env)
    :output-dir (:output-dir compiler)))
