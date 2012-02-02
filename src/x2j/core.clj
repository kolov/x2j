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
(defn decorate-attrs " prepends @ to keys in a map("
  [m] (zipmap (map decorate-kwd (keys m)) (vals m)))

(defn to-vec [x]  (if (vector? x) x (vector x)))
(defn merge-to-vector [m1 m2] (merge-with #(into (to-vec %1) (to-vec %2)) m1 m2))
(defn contentMap? [content] (map? (first content)))
(defn build-content-seq   [attrs content]   
  (merge  (if attrs (decorate-attrs attrs) {})
          (cond  (contentMap? content)
                    (reduce merge-to-vector (map build-node content))
                    (nil? content)
                    {}
                 :else (hash-map  "#text" (first content))
                 )))
  
(defn build-content [{ attrs :attrs content :content}]                           
  (cond (and (nil? attrs) (nil? content)) nil
        (and (nil? attrs) (not (contentMap? content))) (first content)
  :else (build-content-seq  attrs content)))

(defn build-node [node] (hash-map (:tag node) (build-content node))) 

(defn x2j [x] (j/generate-string (build-node (xml-parse-string x))))

(defn -x2j [x] (x2j x))
(defn see [x] (println (x2j x)))


