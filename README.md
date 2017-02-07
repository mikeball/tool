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

 <table>
<tr>
<td valign="top">
__Try REPL__
</td>
<td>
```
$ cljs

cljs.user=> (+ 1 2 3)
10
```
</td>
</tr>
<tr>
<td valign="top">
__Try Script__
</td>
<td>
```clojure
;; my_file.cljs
(println (+ 1 2 3))
```
</td>
<td>
```sh
$ cljs my_file.cljs
10
```
</td>
</tr>
</table>

## Use dependencies

You can pull in external libraries by specifying them in a plain config file, `cljs.edn`:

 <table>
<tr>
<td valign="top">
__Try Deps__
</td>
<td valign=top>
```edn
;; cljs.edn
{:dependencies
 [[markdown-clj "0.9.94"]]}
```

```
$ cljs install
```
</td>
<td valign=top>
```clojure
$ cljs

cljs.user=> (require '[markdown.core :refer [md->html]])
cljs.user=> (md->html "## Hello World")
"<h2>Hello World</h2>"
```
</td>
</tr>
<tr>
<td valign=top>
__In Script__
</td>
<td valign=top>
```html
$ cljs my_file.cljs
<h2>Hello World</h2>
```
</td>
<td>
```clojure
;; my_file.cljs
(require '[markdown.core :refer [md->html]])

(println (md->html "## Hello World"))
```
</td>
</tr>
</table>

## Organize Source

If you create a build name that points to a source directory, you can
start organizing files into canonical namespaces.

 <table>
<tr>
<td valign=top>
__Specify src directory__
</td>
<td colspan=2>
```edn
;; cljs.edn
{:dependencies [...]
 :builds {:main {:src "src"}}} ;; <-- Source at "src" directory,
                               ;;     or use ["src" ...] for multiple.
                               ;;     (:main can be any name for the build)
```
</td>
</tr>
<tr>
<td valign=top>
__Use namespaces__
</td>
<td valign=top>
```clojure
;; src/example/core.cljs
(ns example.core)

(defn hello []
  (println "Hello World"))
```
</td>
<td>
```sh
$ cljs

cljs.user=> (require 'example.core)
cljs.user=> (in-ns 'example.core)
example.core=> (hello)
Hello World
```
</td>
</tr>
</table>

## Compile to JavaScript

To run your ClojureScript code without the `cljs` command, you can
compile it to a JavaScript output file for use in a browser or elsewhere.
Specify extra config for compiler:

 <table>
<tr>
<td valign=top>
__Compiler config__
</td>
<td>
```edn
;; cljs.edn
{:cljs-version "1.9.456"  ;; <-- compiler version
 :dependencies [...]
 :builds {:main {:src "src"
                 :compiler {:output-to "main.js"}}}} ;; <-- compiler options
```
</td>
</tr>
<tr>
<td valign=top>
__Build or watch__
</td>
<td valign=top>
```sh
$ cljs build main
$ cljs watch main
```
</td>
</tr>
</table>

Rather than using Lumo, we use the fast ClojureScript compiler optimized for the JVM,
with better default errors and warnings provided by Figwheel.

## Develop for the web

When developing for the web, you can take full advantage of [Figwheel].
It allows you to compile your project using a much more fluid and interactive
developer experience (e.g. browser-connected console, hotloading, in-page status):

 <table>
<tr>
<td valign=top>
__Figwheel config__
</td>
<td>
```edn
;; cljs.edn
{:cljs-version "1.9.456"
 :dependencies [...]
 :figwheel {...} ;; <-- optional server-level config
 :builds {:main {:src "src"
                 :figwheel ... ;; <-- optional build-level config
                 :compiler {...}}}}
```
</td>
</tr>
<tr>
<td valign=top>
__Run Figwheel__
</td>
<td>
```sh
$ cljs figwheel main
```
</td>
</tr>
<tr>
<td valign=top>
_Try in this repo_
</td>
<td>
```sh
$ cljs figwheel example
```
</td>
</tr>
</table>


## Customize build scripts

For direct access to the ClojureScript compiler API,
run with a Clojure file (`.clj` not `.cljs`).  
Your Clojure program will be given access to the compiler API and
your config in a `*cljs-config*` var.

 <table>
<tr>
<td valign=top>
__Custom Build__
</td>
<td>
```clojure
;; build.clj
(require '[cljs.build.api :as b]) ;; <-- official cljs compiler api

(let [{:keys [src compiler]} (-> *cljs-config* :builds :main)]
  (b/build src compiler))
```
</td>
</tr>
<tr>
<td valign=top>
__Run__
</td>
<td>
```
$ cljs build.clj
```
</td>
</tr>
</table>

[Lumo]:https://github.com/anmonteiro/lumo
[Figwheel]:https://github.com/bhauman/lein-figwheel
[Quick Start]:https://clojurescript.org/guides/quick-start
