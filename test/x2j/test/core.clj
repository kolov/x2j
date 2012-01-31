(ns x2j.test.core
  (:use [x2j.core])
  (:use [clojure.test])
  (:use [clojure.string]))

(defn dehyd [^String s] (replace (replace s " " "") "\"" "'"))

(defn === [a b] "Compare ignoring whitespace. \" is same as '"
  (= (dehyd a) (dehyd b)))

(deftest eq1
  (is (=== "a" "a "))
  (is (=== "a" " a"))
  (is (=== "a'b" "a\"b"))
  (is (=== " a \" b  " "a     ' b"))
  )



(defmacro test-j-x
  "Test that XML produces the expected JSON"
  [name [j x]]
  ( list `deftest name `(is (=== ~j (x2j ~x)))))

         
(deftest a  
  (is (=== "{'a':null}" (x2j "<a/>"))))

(test-j-x a1 [ "{ 'a':null}", "<a/>"])
(test-j-x a2 [ "{ 'a':'x'}", "<a>x</a>"])