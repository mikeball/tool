(require '[cljs.build.api :as b]
         '[clojure.edn :as edn]
         '[figwheel-sidecar.cljs-utils.exception-parsing :refer [print-exception]]
         '[figwheel-sidecar.build-middleware.notifications :refer [warning-message-handler]]
         '[strictly-specking-standalone.ansi-util :refer [with-color]])

(def build (edn/read-string (first *command-line-args*)))

(let [{:keys [src compiler]} build]
  (with-color
    (binding [cljs.analyzer/*cljs-warning-handlers* [(warning-message-handler identity)]]
      (b/watch src
        (assoc compiler :watch-error-fn #(print-exception %))))))
