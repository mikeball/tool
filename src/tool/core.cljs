(ns tool.core
  (:require
    [clojure.string :as string]
    [cljs.pprint :refer [pprint]]
    [tool.io :as io]))

;; config data
(def config nil)
(def deps-cache nil 

(def dep-keys
  [:dependencies
   :dev-dependencies])

(def build-paths-to-keywordize
  [[:compiler :optimizations]
   [:compiler :target]])

;; filenames
(def file-config-edn "cljs.edn")
(def file-deps-cache ".deps-cache.edn")
(def file-dep-retriever (str js/__dirname "/cdr.jar"))
(def file-build (str js/__dirname "/script/build.clj"))
(def file-watch (str js/__dirname "/script/watch.clj"))
(def file-repl (str js/__dirname "/script/repl.clj"))

(defn file-cljs-jar [version] (str js/__dirname "/cljs-" version ".jar"))
(defn url-cljs-jar [version] (str "https://github.com/clojure/clojurescript/releases/download/r" version "/cljs.jar"))

;;---------------------------------------------------------------------------
;; Misc
;;---------------------------------------------------------------------------

(def windows? (= "win32" js/process.platform))

(def child-process (js/require "child_process"))
(def spawn-sync (.-spawnSync child-process))
(def exec-sync (.-execSync child-process))
(def argsplit (js/require "argsplit"))

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

(defn ensure-cljs-version! []
  (let [version (:cljs-version config)
        jar-path (file-cljs-jar version)
        jar-url (url-cljs-jar version)]
    (or (io/path-exists? jar-path)
        (do (println "Downloading ClojureScript version" version)
            (io/download jar-url jar-path)))))

(defn ensure-cmd! [id]
  (or (get-in config [:scripts (keyword id)])
      (exit-error (str "Unrecognized command: '" id "' is not found in :scripts map"))))

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

(defn build-classpath [src]
  (let [{:keys [jars]} (ensure-dependencies!)
        source-paths (when src (if (sequential? src) src [src]))
        cljs-jar (file-cljs-jar (:cljs-version config))
        all (concat [cljs-jar] jars source-paths)
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

(defn task-custom-script [id user-args]
  (let [full-cmd (ensure-cmd! id)
        [script & prefilled-args] (vec (argsplit full-cmd))
        clojure? (string/ends-with? script ".clj")]
    (if-not clojure?
      (exec-sync full-cmd #js{:stdio "inherit"})
      (do
        (ensure-java!)
        (let [classpath (build-classpath (all-sources))
              onload (str "(do (def ^:dynamic *cljs-config* (quote " config ")) nil)")
              args (concat
                     ["-cp" classpath "clojure.main" "-e" onload script]
                     prefilled-args
                     user-args)]
          (spawn-sync "java" (clj->js args) #js{:stdio "inherit"}))))))

(defn print-welcome []
  (println)
  (println (str (io/color :green "(cl") (io/color :blue "js)")
                (io/color :grey " ClojureScript starting...")))
  (println))

(defn -main [task & args]
  (print-welcome)
  (set! config (ensure-config!))
  (ensure-cljs-version!)
  (cond
    (= task "install") (task-install)
    (= task "build") (task-script (first args) file-build)
    (= task "watch") (task-script (first args) file-watch)
    (= task "repl") (task-repl (first args))
    :else (task-custom-script task args)))

(set! *main-cli-fn* -main)
(enable-console-print!)
