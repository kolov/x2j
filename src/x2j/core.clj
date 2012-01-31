(ns x2j.core
  (:use [cheshire.core :as j]
        [clojure.xml :as x]
        )
)

(defn xml-parse-string [#^java.lang.String x]
  (x/parse (java.io.ByteArrayInputStream. (.getBytes x))))

(declare build-node)

(defn decorate-kwd "attach @ at the beginning of a keyword"
  [kw] (keyword (str "@" (subs (str kw) 1))))
(defn decorate-attrs " prepends @ to keys in a map("
  [m] (zipmap (map decorate-kwd (keys m)) (vals m)))

(defn build-content-seq   [attrs content]   
  (merge  (if attrs (decorate-attrs attrs) {})
          (cond  (map? (first content))  (reduce merge (map build-node content))
                 (nil? content) {}
                 :else (hash-map  "#text" (first content))
                 )))
  
(defn build-content [{ attrs :attrs content :content}]                           
 (if (and (nil? attrs) (nil? content)) nil             
  (build-content-seq  attrs content)))

(defn build-node [node] (hash-map (:tag node) (build-content node))) 

(defn x2j [x] (j/generate-string (build-node (xml-parse-string x))))

(defn see [x] (println (x2j x)))


