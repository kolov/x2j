(ns x2j.core
  (:use [cheshire.core :as j]
        [clojure.xml :as x]
        )
  (:gen-class
   :name net.kolov.x2j.Converter
   :methods [
             #^{:static true} [x2j [java.lang.String] java.lang.String]
             #^{:static true} [j2x [java.lang.String] java.lang.String]
             #^{:static true} [j2x [java.lang.String java.lang.String] java.lang.String]
             ])
              
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
; Start JSON 2 XML 

(declare node2x)
(defn isAttr [name] (.startsWith name "@"))
(defn isText [name] (.startsWith name "#"))
(defn isSubnode [name] (and (not (isAttr name)) (not (isText name ))))
(defn make-attrs [v] (->
                      (if (map? v)
                        (reduce
                         merge
                         (map #( hash-map % (v %)) (filter isAttr (keys v))))
                        nil)
                      check-empty ))
                        
(defn make-content-map [v]  (vec (flatten
                                  (map #(node2x  (find v % )) (filter isSubnode (keys v))))))
(defn make-content [v] (->
                         (if (map? v) (make-content-map v) (to-vec v))
                         check-empty))

(defn make-xml-node [k v] {:tag k :attrs (make-attrs v) :content (make-content v)})
(defn node2x [me] (let [ k (key me) v (val me)]
                    (if (vector? v)
                       (vec (map #(make-xml-node k %) v))
                      (make-xml-node k v))))
(defn parsed2x
  ([m name]  (let [ me (find m name)]
               (if (nil? me)
                 (throw (IllegalArgumentException. (str "No such element: " name))))
               (node2x me)))
                                                                
  ( [m] (if (= (count m) 1)
          (node2x (first m))
          (throw (IllegalArgumentException. "More than 1 entry, specyfy name")))))

(defn j2x 
  ([s] (let [parsed (j/parse-string s)] (parsed2x parsed)))
  ([s name] (let [parsed (j/parse-string s)] (parsed2x parsed name))))

(defn -j2x
  ([#^java.lang.String s] (clojure.xml/emit (j2x s)))
  ([#^java.lang.String s #^java.lang.String name] (clojure.xml/emit (j2x s name))))