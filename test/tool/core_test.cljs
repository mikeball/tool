(ns tool.core-test
  (:require [cljs.test :refer-macros [deftest is are]]))


(deftest booleans-are-equal
  (is (= true true)))
