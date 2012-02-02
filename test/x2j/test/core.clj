(ns x2j.test.core
  (:use [x2j.core])
  (:use [clojure.test])
  (:use [cheshire.core :as j])
  )




(defn === [js x] (let [converted (j/parse-string x)]
                   (do (println converted "--" js)
                       (= (j/parse-string js) converted))))
      
(defmacro test-j-x
  "Test that XML produces the expected JSON"
  [name [j x]]
  ( list `deftest name `(is (=== ~j (x2j ~x)))))

(deftest test1 (is (=== "{ \"a\":null}" "{ \"a\":null}")))
#_ Case 1
(test-j-x a1 [ "{ \"a\":null}", "<a/>"])
#_ Case 2
(test-j-x a2 [ "{ \"a\":\"x\"}", "<a>x</a>"])
#_ Case 3
(test-j-x t3 [ "{\"a\":{\"@attr1\":\"attrval1\"}}", "<a attr1=\"attrval1\"/>"])
#_ Case 4
(test-j-x case4 [ "{\"e\": { \"@name\": \"value\", \"#text\": \"text\" }}" "<e name=\"value\">text</e>"])
(test-j-x t4 [ "{\"a\":{\"b\":\"c\"}}" , "<a><b>c</b></a>"])

(test-j-x case5 [ "{\"e\": { \"a\": \"text\", \"b\": \"text\" }}"
                  "<e> <a>text</a> <b>text</b> </e>"])

(test-j-x case5 [ "{\"e\": { \"a\": [ \"text\",  \"text\"] }}"
                  "<e> <a>text</a> <a>text</a> </e>"])