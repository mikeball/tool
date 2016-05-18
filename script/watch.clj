(require '[cljs.build.api :as b]
         '[clojure.edn :as edn])

(def build (edn/read-string (first *command-line-args*)))

(let [{:keys [src compiler]} build]
  (b/watch src compiler))
