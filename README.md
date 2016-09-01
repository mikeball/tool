# npm cljs

> __NOTE__: This is still experimental.

A minimal ClojureScript build tool using a [standard config file], `cljs.edn`.
It provides a layer over the [Quick Start] scripts to provide
dependency management and a central config.

[standard config file]:https://github.com/cljs/config-spec/

```
npm install -g git+https://github.com/cljs/tool.git
```

```
cljs install
cljs repl
cljs build <id>
cljs watch <id>
cljs <script_id>
```

[Quick Start]:https://github.com/clojure/clojurescript/wiki/Quick-Start

## Implementation

- `src/` - top-level tool implemented in ClojureScript on Node.js
- `target/cdr.jar` - minimal java tool for resolving dependencies ([source](https://github.com/cljs/dep-resolver))
- `target/script/` - clojure "scripts" for accessing cljs compiler
- `target/cljs-<version>.jar` - cljs uberjar for fast starting production compiler (auto-downloaded)

## Setup

After installing `cljs` using npm as mentioned, you can use it to build a local
copy.

```
$ cljs build tool
```

From there, you can use the local copy to build itself again if you like:

```
$ npm install
$ ./cljs build tool
```
