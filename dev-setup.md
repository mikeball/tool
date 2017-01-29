
## Implementation

- `src/` - top-level tool implemented in ClojureScript on Node.js
- `target/cdr.jar` - minimal java tool for resolving dependencies ([source](https://github.com/cljs/dep-resolver))
- `target/script/` - clojure "scripts" for accessing cljs compiler
- `target/cljs-<version>.jar` - cljs uberjar for fast starting production compiler (auto-downloaded)

## Development Setup

After running `npm install cljs/tool -g`, you can use it to build a local copy.

```
$ cljs build tool
```

From there, you can use the local copy to build itself again if you like:

```
$ npm install
$ ./cljs build tool
```
