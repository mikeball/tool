(require '[cljs.build.api :as b]
         '[clojure.edn :as edn]
         '[figwheel-sidecar.cljs-utils.exception-parsing :refer [print-exception]]
         '[figwheel-sidecar.build-middleware.notifications :refer [warning-message-handler]])

(def build (edn/read-string (first *command-line-args*)))

(let [{:keys [src compiler]} build]
  (binding [cljs.analyzer/*cljs-warning-handlers* [(warning-message-handler identity)]]
    (b/watch src
      (assoc compiler :watch-error-fn #(print-exception %)))))
