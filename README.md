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

## Run

The command name is `cljs`, the abbreviation for ClojureScript.

```
$ cljs

cljs.user=> (+ 1 2 3)
10
```

Or run a script:

```clojure
;; in my_file.cljs
(println (+ 1 2 3))
```

```sh
$ cljs my_file.cljs
10
```

Fast experimenting should be the default. Thus, the previous examples work by
using [Lumo], a pure Node environment for running ClojureScript.
It allows quick tinkerers to try the most basic things as fast as possible.

## Dependencies

You can pull in external libraries by specifying them in a plain config file, `cljs.edn`:

```edn
{:dependencies [[markdown-clj "0.9.94"]]}
```

```
$ cljs

cljs.user=> (require '[markdown.core :refer [md->html]])
cljs.user=> (md->html "## Hello World")
"<h2>Hello World</h2>"
```

Dependencies are resolved and installed automatically when running any `cljs`
command, or manually with:

```
$ cljs install
```

## Organizing Source

Source files should be able to refer to each other, and likewise you should be
able to use them at the REPL.
This is best done by creating a build name that points to a source directory.

```edn
{:dependencies [...]
 :builds {:main {:src "src"}}} ;; <-- Source at "src" directory,
                               ;;     or use ["src" ...] for multiple.
                               ;;     (:main can be any name for the build)
```

If you have a `src/example/core.cljs` with a `foo` function, you can
run it with this and do likewise in other files:

```sh
$ cljs

cljs.user=> (require 'example.core)
cljs.user=> (in-ns 'example.core)
example.core=> (foo)
```

## Compiling to JavaScript

To run your ClojureScript code without the `cljs` command, you can
compile it to a JavaScript output file for use in a browser or elsewhere.
Specify extra config for compiler:

```edn
{:cljs-version "1.9.456"  ;; <-- compiler version
 :dependencies [...]
 :builds {:main {:src "src"
                 :compiler {:output-to "main.js"}}}} ;; <-- compiler options
```

You can build once or continue rebuilding after changes:

```sh
$ cljs build main
$ cljs watch main
```

## Figwheel

[Figwheel] allows you to compile your project using a much
more fluid and interactive developer experience:

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

## Manual build script

For direct access to the ClojureScript compiler API,
run with a `*.clj` file (not `*.cljs`).

```
$ cljs my_build_script.clj
```

Your Clojure program will be run with access to the compiler API,
receiving config in a `*cljs-config*` var.

```clojure
;; my_build_script.clj
(require '[cljs.build.api :as b])

(let [{:keys [src compiler]} *build-config*]
  (b/build src compiler))
```

[Lumo]:https://github.com/anmonteiro/lumo
[Figwheel]:https://github.com/bhauman/lein-figwheel
[Quick Start]:https://clojurescript.org/guides/quick-start
