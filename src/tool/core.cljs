(ns tool.core
  (:require
    [clojure.string :as string]
    [cljs.pprint :refer [pprint]]
    [tool.io :as io]))

;; filenames
(def file-config-edn "cljs.edn")
(def file-deps-cache ".deps-cache.edn")
(def file-dep-retriever (str js/__dirname "/cdr.jar"))
(def file-build (str js/__dirname "/script/build.clj"))
(def file-watch (str js/__dirname "/script/watch.clj"))
(def file-repl (str js/__dirname "/script/repl.clj"))

;; config data
(def config nil)
(defn load-config! []
  (when (io/path-exists? file-config-edn)
    (set! config (io/slurp-edn file-config-edn))))

(def deps-cache nil)

(def dep-keys
  [:dependencies
   :dev-dependencies])

(def build-paths-to-keywordize
  [[:compiler :optimizations]
   [:compiler :target]])

;;---------------------------------------------------------------------------
;; JARs used for compiling w/ JVM
;; (they are AOT'd to reduce load time)
;;---------------------------------------------------------------------------

;; ClojureScript downloaded based on version in config
(defn file-cljs-jar [version] (str js/__dirname "/cljs-" version ".jar"))
(defn url-cljs-jar [version] (str "https://github.com/clojure/clojurescript/releases/download/r" version "/cljs.jar"))

;; We build/host our own AOT'd uberjar for figwheel sidecar
;; (FIXME: find a better way to host this, though changes may be infrequent
;;  since we are only using it for pretty errors/warnings, or move it to own
;;  error/warning prettier repo)
(defn file-fig-jar [version] (str js/__dirname "/figwheel-sidecar-" version ".jar"))
(def fig-version "0.5.8")

(defn get-jvm-jars []
  [(file-cljs-jar (:cljs-version config))
   (file-fig-jar fig-version)])

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

(defn ensure-cljs-version! []
  (let [version (:cljs-version config)
        jar-path (file-cljs-jar version)
        jar-url (url-cljs-jar version)]
    (or (io/path-exists? jar-path)
        (do (println "Downloading ClojureScript version" version)
            (io/download jar-url jar-path)))))

(defn ensure-config! []
  (or config
      (exit-error "No config found. Please create one in" file-config-edn)))

(defn ensure-build! [id]
  (or (get-in config [:builds (keyword id)])
      (exit-error (str "Unrecognized build: '" id "' is not found in :builds map"))))

(declare task-install)

(defn ensure-dependencies! []
  (let [cache (io/slurp-edn file-deps-cache)
        stale? (not= (select-keys config dep-keys)
                     (select-keys cache dep-keys))]
    (if stale?
      (task-install)
      cache)))

;;---------------------------------------------------------------------------
;; Main Tasks
;;---------------------------------------------------------------------------

(defn task-install []
  (ensure-java!)
  (let [deps (apply concat (map config dep-keys))
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

(defn build-classpath [& {:keys [src jvm?]}]
  (let [{:keys [jars]} (ensure-dependencies!)
        source-paths (when src (if (sequential? src) src [src]))
        jars (cond->> jars jvm? (concat (get-jvm-jars)))
        all (concat jars source-paths)
        sep (if windows? ";" ":")]
    (string/join sep all)))

(defn task-script [id file-script]
  (ensure-java!)
  (let [{:keys [src] :as build} (ensure-build! id)
        cp (build-classpath :src src :jvm? true)]
    (spawn-sync "java"
      #js["-cp" cp "clojure.main" file-script (pr-str build)]
      #js{:stdio "inherit"})))

(defn all-sources []
  (->> (:builds config)
       (vals)
       (map :src)
       (filter identity)
       (flatten)))

(defn task-repl [id]
  (ensure-java!)
  (let [cp (build-classpath :src (all-sources) :jvm? true)]
    (spawn-sync "java"
      #js["-cp" cp "clojure.main" file-repl]
      #js{:stdio "inherit"})))

(defn task-custom-script [path user-args]
  (ensure-java!)
  (let [cp (build-classpath :src (all-sources) :jvm? true)
        onload (str "(do (def ^:dynamic *cljs-config* (quote " config ")) nil)")
        args (concat ["-cp" cp "clojure.main" "-e" onload path] user-args)]
    (spawn-sync "java" (clj->js args) #js{:stdio "inherit"})))

;;---------------------------------------------------------------------------
;; Lumo
;;---------------------------------------------------------------------------

(defn build-lumo-args [args]
  (apply array
    (concat args
      ;; Add dependencies to classpath, and all source directories
      (when config
        ["-c" (build-classpath :src (all-sources))]))))

(def npmRun (js/require "npm-run"))
(defn run-lumo [args]
  (npmRun.spawnSync "lumo" (build-lumo-args args) #js{:stdio "inherit"}))

;;---------------------------------------------------------------------------
;; Entry
;;---------------------------------------------------------------------------

(defn print-welcome []
  (println)
  (println (str (io/color :green "(cl") (io/color :blue "js)")
                (io/color :grey " ClojureScript starting...")))
  (println))

(defn -main [task & args]
  (load-config!)
  (cond
    (nil? task) (do (print-welcome) (run-lumo nil))
    (string/ends-with? task ".cljs") (run-lumo (cons task args))
    :else
    (do
      (print-welcome)
      (ensure-config!)
      (ensure-cljs-version!)
      (cond
        (= task "install") (task-install)
        (= task "build") (task-script (first args) file-build)
        (= task "watch") (task-script (first args) file-watch)
        (= task "repl") (task-repl (first args))
        (string/ends-with? task ".clj") (task-custom-script task args)
        :else (exit-error "Unrecognized task:" task)))))

(set! *main-cli-fn* -main)
(enable-console-print!)
