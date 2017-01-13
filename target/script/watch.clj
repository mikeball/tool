(require '[cljs.build.api :as b]
         '[clojure.edn :as edn]
         '[figwheel-sidecar.cljs-utils.exception-parsing :refer [print-exception]])

(def build (edn/read-string (first *command-line-args*)))

(let [{:keys [src compiler]} build]
  (b/watch src
    (assoc compiler :watch-error-fn #(print-exception %))))
