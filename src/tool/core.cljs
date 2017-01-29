(ns tool.core
  (:require
    [clojure.string :as string]
    [cljs.pprint :refer [pprint]]
    [tool.io :as io]))

;; config data
(def config nil)
(def deps-cache nil)

(def dep-keys
  [:dependencies
   :dev-dependencies])

(def build-paths-to-keywordize
  [[:compiler :optimizations]
   [:compiler :target]])

(defn base-deps []
  {:dependencies [['org.clojure/clojurescript (:cljs-version config)]]
   :dev-dependencies [['figwheel-sidecar "0.5.8"]]})

;; filenames
(def file-config-edn "cljs.edn")
(def file-deps-cache ".deps-cache.edn")
(def file-dep-retriever (str js/__dirname "/cdr.jar"))
(def file-build (str js/__dirname "/script/build.clj"))
(def file-watch (str js/__dirname "/script/watch.clj"))
(def file-repl (str js/__dirname "/script/repl.clj"))

;;---------------------------------------------------------------------------
;; Lumo
;;---------------------------------------------------------------------------

(def npmRun (js/require "npm-run"))
(defn run-lumo
  ([] (npmRun.spawnSync "lumo" #js{:stdio "inherit"}))
  ([filename args] (npmRun.spawnSync "lumo" (apply array (cons filename args)) #js{:stdio "inherit"})))

;;---------------------------------------------------------------------------
;; Misc
;;---------------------------------------------------------------------------

(def windows? (= "win32" js/process.platform))

(def child-process (js/require "child_process"))
(def spawn-sync (.-spawnSync child-process))

(defn exit-error [& args]
  (apply js/console.error args)
  (js/process.exit 1))

;;---------------------------------------------------------------------------
;; Validation
;;---------------------------------------------------------------------------

(defn java-installed? []
  (not (.-error (spawn-sync "java"))))

(defn ensure-java! []
  (or (java-installed?)
      (exit-error "Please install Java.")))

(defn ensure-config! []
  (or (io/slurp-edn file-config-edn)
      (exit-error "No config found. Please create one in" file-config-edn)))

(defn ensure-build! [id]
  (or (get-in config [:builds (keyword id)])
      (exit-error (str "Unrecognized build: '" id "' is not found in :builds map"))))

(declare task-install)

(defn get-current-deps []
  (let [user-deps (select-keys config dep-keys)
        all-deps (merge-with concat (base-deps) user-deps)]
    all-deps))

(defn ensure-dependencies! []
  (let [cache (io/slurp-edn file-deps-cache)
        stale? (not= (get-current-deps)
                     (select-keys cache dep-keys))]
    (if stale?
      (task-install)
      cache)))

;;---------------------------------------------------------------------------
;; Main Tasks
;;---------------------------------------------------------------------------

(defn task-install []
  (ensure-java!)
  (let [deps (apply concat (map (get-current-deps) dep-keys))
        result (spawn-sync "java"
                 #js["-jar" file-dep-retriever (pr-str deps)]
                 #js{:stdio #js["pipe" "pipe" 2]})
        stdout-lines (when-let [output (.-stdout result)]
                       (string/split (.toString output) "\n"))
        success? (and (zero? (.-status result))
                      (not (.-error result)))]
    (when success?
      (let [cache (-> config
                      (select-keys dep-keys)
                      (assoc :jars stdout-lines))]
        (io/spit file-deps-cache (with-out-str (pprint cache)))
        cache))))

(defn build-classpath [src]
  (let [{:keys [jars]} (ensure-dependencies!)
        source-paths (when src (if (sequential? src) src [src]))
        all (concat jars source-paths)
        sep (if windows? ";" ":")]
    (string/join sep all)))

(defn task-script [id file-script]
  (ensure-java!)
  (let [build (ensure-build! id)]
    (spawn-sync "java"
      #js["-cp" (build-classpath (:src build)) "clojure.main" file-script (pr-str build)]
      #js{:stdio "inherit"})))

(defn all-sources []
  (->> (:builds config)
       (vals)
       (map :src)
       (filter identity)
       (flatten)))

(defn task-repl [id]
  (ensure-java!)
  (spawn-sync "java"
    #js["-cp" (build-classpath (all-sources)) "clojure.main" file-repl]
    #js{:stdio "inherit"}))

(defn task-custom-script [path user-args]
  (ensure-java!)
  (let [classpath (build-classpath (all-sources))
        onload (str "(do (def ^:dynamic *cljs-config* (quote " config ")) nil)")
        args (concat ["-cp" classpath "clojure.main" "-e" onload path] user-args)]
    (spawn-sync "java" (clj->js args) #js{:stdio "inherit"})))

(defn print-welcome []
  (println)
  (println (str (io/color :green "(cl") (io/color :blue "js)")
                (io/color :grey " ClojureScript starting...")))
  (println))

(defn -main [task & args]
  (cond
    (nil? task) (do (print-welcome) (run-lumo))
    (string/ends-with? task ".cljs") (run-lumo task args)
    :else
    (do
      (print-welcome)
      (set! config (ensure-config!))
      (cond
        (= task "install") (task-install)
        (= task "build") (task-script (first args) file-build)
        (= task "watch") (task-script (first args) file-watch)
        (= task "repl") (task-repl (first args))
        (string/ends-with? task ".clj") (task-custom-script task args)
        :else (exit-error "Unrecognized task:" task)))))

(set! *main-cli-fn* -main)
(enable-console-print!)
