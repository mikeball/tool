> __NOTE__: work in progress

# ClojureScript starter tool

A very small tool for introducing ClojureScript as quickly as possible.

```
npm install cljs/tool -g
```

## Basic Expectations

Fast REPL and scripting provided by [Lumo].

```sh
$ cljs               # REPL
$ cljs my_file.cljs  # Run as script
```

## Full Compiler

Compile projects using the production-level JVM ClojureScript compiler.
Full Maven dependency resolution. Errors and warnings are prettified by [Figwheel]'s
sidecar library. (User will be asked to install Java if not detected.)

```sh
$ cljs install       # install dependencies
$ cljs build <id>    # build the target <id>
$ cljs watch <id>    # build and watch for changes
$ cljs repl          # run a project REPL

$ cljs my_build_script.clj  # (advanced: run your own build script using compiler API)
```

## Plain Config

Plain data project config expected under `cljs.edn`:

```edn
{; Specify the ClojureScript version you want to use.
 :cljs-version "1.9.456"

 ; Dependencies go here (same format as lein/boot).
 :dependencies []
 :dev-dependencies []

 ; Builds
 :builds
   {:main ; <-- name of this build
     {:src "src" ; <-- source directories passed to compiler
      :compiler ; <-- options passed to compiler
       {:output-to "target/main.js"
        :optimizations :simple}}}}
```

[Lumo]:https://github.com/anmonteiro/lumo
[Figwheel]:https://github.com/bhauman/lein-figwheel
