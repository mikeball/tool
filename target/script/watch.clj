(require '[cljs.build.api :as b]
         '[figwheel-sidecar.cljs-utils.exception-parsing :refer [print-exception]]
         '[figwheel-sidecar.build-middleware.notifications :refer [warning-message-handler]]
         '[strictly-specking-standalone.ansi-util :refer [with-color]])

(let [{:keys [src compiler]} *build-config*]
  (with-color
    (binding [cljs.analyzer/*cljs-warning-handlers* [(warning-message-handler identity)]]
      (b/watch src
        (assoc compiler :watch-error-fn #(print-exception %))))))
