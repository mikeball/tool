(require
  ;; the ClojureScript compiler
  '[cljs.build.api :as b]

  ;; borrowing pretty error printing from Figwheel
  '[figwheel-sidecar.cljs-utils.exception-parsing :refer [print-exception]]
  '[figwheel-sidecar.build-middleware.notifications :refer [warning-message-handler]]
  '[strictly-specking-standalone.ansi-util :refer [with-color]])

(def start (System/nanoTime))
(println "Building" (:id *build-config*) "...")

(let [{:keys [src compiler]} *build-config*
      source (if (sequential? src) (apply b/inputs src) src)
      opts (assoc compiler
             :warning-handlers [(warning-message-handler identity)])]
  (with-color
    (try
      (b/build source opts)
      (catch Throwable e
        (print-exception e)))))

(println "... done. Elapsed" (/ (- (System/nanoTime) start) 1e9) "seconds")
