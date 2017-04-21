(require '[cljs.build.api :as b]
         '[figwheel-sidecar.cljs-utils.exception-parsing :refer [print-exception]]
         '[figwheel-sidecar.build-middleware.notifications :refer [warning-message-handler]]
         '[strictly-specking-standalone.ansi-util :refer [with-color]])

(let [{:keys [src compiler]} *build-config*
      source (if (sequential? src) (apply b/inputs src) src)
      opts (assoc compiler :watch-error-fn #(print-exception %))]
  (with-color
    (binding [cljs.analyzer/*cljs-warning-handlers* [(warning-message-handler identity)]]
      (b/watch source opts))))
