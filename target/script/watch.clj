(require
  ;; the ClojureScript compiler
  '[cljs.build.api :as b]

  ;; borrowing pretty error printing from Figwheel
  '[figwheel-sidecar.cljs-utils.exception-parsing :refer [print-exception]]
  '[figwheel-sidecar.build-middleware.notifications :refer [warning-message-handler]]
  '[strictly-specking-standalone.ansi-util :refer [with-color]])

(let [{:keys [src compiler]} *build-config*
      source (if (sequential? src) (apply b/inputs src) src)
      opts (assoc compiler
             :warning-handlers [(warning-message-handler identity)]
             :watch-error-fn #(print-exception %))]
  (with-color
    (b/watch source opts)))
