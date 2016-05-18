# Dependency Retriever

If cljs.edn is to allow `:dependencies` and `:dev-dependencies` vectors, it
might make sense to have some build-tool agnostic way to retrieve them.
[Planck] and the [CLJS Quick Start] already rely on either of the following
commands for downloading dependencies and resolving their classpath:

```
$ lein classpath
$ boot show -c
```

Both tools use a Clojure library [Pomegranate], which wraps the Java library
[Aether], the standard interface to Maven repositories.  Knowing this, we
implement a minimal implementation of the commands above with a small library.

[Planck]:http://planck-repl.org/dependencies.html
[CLJS Quick Start]:https://github.com/clojure/clojurescript/wiki/Quick-Start#dependencies
[Pomegranate]:https://github.com/cemerick/pomegranate/
[Aether]:http://www.eclipse.org/aether/

## Usage

```
$ lein run

Retrieve dependencies from the given dependencies vector.

Results:
  stderr: download progress
  stdout: resulting JAR filepaths (one per line)

Options:
  -h, --help
```

### Example

```
$ lein run -- '[[rum "0.8.3"][hiccups "0.3.0"]]'
```

Download progress printed to stderr:

```
Retrieving rum/rum/0.8.3/rum-0.8.3.pom from http://clojars.org/repo/
Retrieving hiccups/hiccups/0.3.0/hiccups-0.3.0.pom from http://clojars.org/repo/
Retrieving rum/rum/0.8.3/rum-0.8.3.jar from http://clojars.org/repo/
Retrieving hiccups/hiccups/0.3.0/hiccups-0.3.0.jar from http://clojars.org/repo/
```

Resulting JAR filepaths are printed to stdout (one per line):

```
/Users/swilliam/.m2/repository/org/clojure/clojurescript/0.0-2069/clojurescript-0.0-2069.jar
/Users/swilliam/.m2/repository/sablono/sablono/0.7.1/sablono-0.7.1.jar
/Users/swilliam/.m2/repository/hiccups/hiccups/0.3.0/hiccups-0.3.0.jar
/Users/swilliam/.m2/repository/org/clojure/google-closure-library-third-party/0.0-20130212-95c19e7f0f5f/google-closure-library-third-party-0.0-20130212-95c19e7f0f5f.jar
/Users/swilliam/.m2/repository/com/google/protobuf/protobuf-java/2.4.1/protobuf-java-2.4.1.jar
/Users/swilliam/.m2/repository/cljsjs/react/15.0.1-1/react-15.0.1-1.jar
/Users/swilliam/.m2/repository/org/clojure/tools.reader/0.8.0/tools.reader-0.8.0.jar
/Users/swilliam/.m2/repository/com/google/javascript/closure-compiler/v20130603/closure-compiler-v20130603.jar
/Users/swilliam/.m2/repository/org/mozilla/rhino/1.7R4/rhino-1.7R4.jar
/Users/swilliam/.m2/repository/org/json/json/20090211/json-20090211.jar
/Users/swilliam/.m2/repository/rum/rum/0.8.3/rum-0.8.3.jar
/Users/swilliam/.m2/repository/com/google/code/findbugs/jsr305/1.3.9/jsr305-1.3.9.jar
/Users/swilliam/.m2/repository/args4j/args4j/2.0.16/args4j-2.0.16.jar
/Users/swilliam/.m2/repository/com/google/guava/guava/14.0.1/guava-14.0.1.jar
/Users/swilliam/.m2/repository/org/clojure/google-closure-library/0.0-20130212-95c19e7f0f5f/google-closure-library-0.0-20130212-95c19e7f0f5f.jar
/Users/swilliam/.m2/repository/org/clojure/clojure/1.5.1/clojure-1.5.1.jar
/Users/swilliam/.m2/repository/org/clojure/data.json/0.2.3/data.json-0.2.3.jar
/Users/swilliam/.m2/repository/cljsjs/react-dom/15.0.1-1/react-dom-15.0.1-1.jar
```

## Usage outside Leiningen

This is intended to be used as a standalone JAR:

```
$ lein uberjar
$ java -jar target/dep-retriever-0.1.0-standalone.jar
```
