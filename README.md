> __NOTE__: work in progress

# ClojureScript starter tool

A tool to answer the following questions:

- What would a great first run of ClojureScript look like?
- How do we create that in the clearest and cheapest way possible?
- Should it do everything?
- Can it improve adoption without fragmenting the community?

## Install

```
npm install cljs/tool -g
```

Users will not be asked to install Java until required.

## Cljs

There should be nothing special about the tool name.
`cljs` is the abbreviation for ClojureScript, and the most unsurprising
name choice for a first run ClojureScript tool.

## Run in terminal

Fast experimenting should be the default.  We use
[Lumo] to allow you to try the most basic things as fast as possible.

Try commands in a REPL:

```
$ cljs

cljs.user=> (+ 1 2 3)
10
```

Run commands in a script:

```clojure
;; in my_file.cljs
(println (+ 1 2 3))
```

```sh
$ cljs my_file.cljs
10
```

## Get dependencies

You can pull in external libraries by specifying them in a plain config file, `cljs.edn`:

```edn
{:dependencies [[markdown-clj "0.9.94"]]}
```

```
$ cljs install
$ cljs

cljs.user=> (require '[markdown.core :refer [md->html]])
cljs.user=> (md->html "## Hello World")
"<h2>Hello World</h2>"
```

## Organize Source

If you create a build name that points to a source directory, you can
start organizing files into canonical namespaces.

```edn
{:dependencies [...]
 :builds {:main {:src "src"}}} ;; <-- Source at "src" directory,
                               ;;     or use ["src" ...] for multiple.
                               ;;     (:main can be any name for the build)
```

With this config, say you have a `src/example/core.cljs`:

```clojure
(ns example.core)

(defn hello []
  (println "Hello World"))
```

You can do the following:

```sh
$ cljs

cljs.user=> (require 'example.core)
cljs.user=> (in-ns 'example.core)
example.core=> (hello)
Hello World
```

## Compile to JavaScript

To run your ClojureScript code without the `cljs` command, you can
compile it to a JavaScript output file for use in a browser or elsewhere.
Specify extra config for compiler:

```edn
{:cljs-version "1.9.456"  ;; <-- compiler version
 :dependencies [...]
 :builds {:main {:src "src"
                 :compiler {:output-to "main.js"}}}} ;; <-- compiler options
```

You can build once or continue rebuilding after changes.  This is not done by
Lumo, but uses the fastest ClojureScript compiler (optimized for the JVM),
with better default errors and warnings provided by Figwheel.

```sh
$ cljs build main
$ cljs watch main
```

## Develop for the web

When developing for the web, you can take full advantage of [Figwheel].
It allows you to compile your project using a much more fluid and interactive
developer experience (e.g. browser-connected console, hotloading, in-page status):

```sh
$ cljs figwheel main
```

Provide optional keys for more configuration:

```edn
{:cljs-version "1.9.456"
 :dependencies [...]
 :figwheel {...} ;; <-- optional server-level config
 :builds {:main {:src "src"
                 :figwheel ... ;; <-- optional build-level config
                 :compiler {...}}}}
```

__Try it yourself:__ In this repo, run the following and open `public/index.html`.

```sh
$ cljs figwheel example
```

## Customize build scripts

For direct access to the ClojureScript compiler API,
run with a Clojure file (`.clj` not `.cljs`):

```
$ cljs build.clj
```

Your Clojure program will be given access to the compiler API and
your config in a `*cljs-config*` var.

```clojure
;; build.clj
(require '[cljs.build.api :as b]) ;; <-- official cljs compiler api

(let [{:keys [src compiler]} (-> *cljs-config* :builds :main)]
  (b/build src compiler))
```

[Lumo]:https://github.com/anmonteiro/lumo
[Figwheel]:https://github.com/bhauman/lein-figwheel
[Quick Start]:https://clojurescript.org/guides/quick-start
