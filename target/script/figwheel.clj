(require '[figwheel-sidecar.repl :as r]
         '[figwheel-sidecar.repl-api :as ra])

(let [{:keys [id src compiler]} *build-config*
      source-paths (if (sequential? src) src [src])]
  (ra/start-figwheel!
    {:figwheel-options (or (:figwheel *cljs-config*) {})
     :build-ids [id]
     :all-builds
     [{:id id
       :figwheel (or (:figwheel *build-config*) {})
       :source-paths source-paths
       :compiler compiler}]}))

(ra/cljs-repl)
