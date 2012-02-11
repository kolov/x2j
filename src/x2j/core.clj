(ns x2j.core
  (:use [cheshire.core :as j]
        [clojure.xml :as x]
        )
  (:gen-class
   :name net.kolov.x2j.Converter
       :methods [#^{:static true} [x2j [java.lang.String] java.lang.String]])
)

(defn xml-parse-string [#^java.lang.String x]
  (x/parse (java.io.ByteArrayInputStream. (.getBytes x))))

(declare build-node)

(defn decorate-kwd "attach @ at the beginning of a keyword"
  [kw] (keyword (str "@" (subs (str kw) 1))))
(defn decorate-attrs "prepends @ to keys in a map"
  [m] (zipmap (map decorate-kwd (keys m)) (vals m)))

(defn to-vec [x]  (if (vector? x) x (vector x)))
(defn merge-to-vector "merge 2 maps, putting values of repeating keys in a vector"
   [m1 m2] (merge-with #(into (to-vec %1) (to-vec %2)) m1 m2))
(defn contentMap? "Check if a node contand is a map i.e. has child nodes"
   [content] (map? (first content)))

(defn parts [{ attrs :attrs content :content}]
  (merge (decorate-attrs attrs)
        (cond (contentMap? content) (reduce merge-to-vector (map build-node content))
              (nil? content) nil
              :else (hash-map  "#text" (first content)))
        ))
(defn check-text-only [m] (if (= (keys m) '("#text")) (val (first m)) m))
(defn check-empty  [m] (if (empty? m) nil m))

(defn build-node [node] (hash-map (:tag node) (-> (parts node) check-empty check-text-only)))

(defn x2j [x] (j/generate-string (build-node (xml-parse-string x))))

(defn -x2j [x] (x2j x))

; Developing jx2 below, not finished

(def J "{ \"_id\" : { \"$oid\" : \"4f2aed38036422710e1ce932\"} , \"person\" : { \"hobbies\" : {\"hobby\" : [ \"books\" , \"tv\"]} , \"id\" : { \"#text\" : \"34234234324\" , \"@type\" : \"passport\"} , \"address\" : { \"street\" : \"Main Street\" , \"city\" : \"Atlanta\"} , \"name\" : \"Joe\"}}")

(declare node2x)
(defn isAttr [name] (.startsWith name "@"))
(defn isText [name] (.startsWith name "#"))
(defn isSubnode [name] (and (not (isAttr name)) (not (isText name )))) 
(defn make-attrs [node] ( map #( hash-map %1 (node %1)) (filter isAttr (keys node))))
(defn make-content-map [node] ( map #(do (println "subnode: [" (find node % )"]") ) (filter isSubnode (keys node))))
(defn make-content [v] ( if (map? v) (make-content-map v) v) )
                       
(defn node2x [me] (let [ k (key me) v (val me)]
                   {:tag k :attrs (make-attrs v) :content (make-content v)}))
(defn j2x ( [s name] ( node2x ( find (j/parse-string s) name)))
          ( [s] (node2x (j/parse-string s))))