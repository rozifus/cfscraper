(ns cfscraper.unit
  (:import (com.gargoylesoftware.htmlunit WebClient BrowserVersion))
  (:use [clojure.string :only [upper-case]]))

(def ^:dynamic *client*)

(def base-url "http://customsforge.com/")
(def login-url "http://customsforge.com/index.php?app=core&module=global&section=login")

(def ^:dynamic *current-url* base-url)

(defn make-client
  ([]
     (make-client (BrowserVersion/getDefault)))
  ([version]
       (println (str "Browser: " (.getNickname version)))
       (new WebClient version)))

(defn get-page
  [client url]
  (.getPage client url))

(defn get-nodes-by-xpath
  [node xpath]
  (.getByXPath node xpath))

(defn get-first-node-by-xpath
  [node xpath]
  (.getFirstByXPath node xpath))

(defn get-node-anchors
  [node]
  (get-nodes-by-xpath node "//a"))

(defn get-nodes-anchors
  [nodes]
  (flatten (map #(get-node-anchors %) nodes))) 

(defn get-node-attributes
  [node]
  (let [attrs (.getAttributes node)
        length (.getLength attrs)
	items (map #(.item attrs %) (range 0 length))
	hash (reduce #(merge %1 {(keyword (.getName %2)) (.getValue %2)}) {} items)]
    hash))

(defn has-login-button? [page]
  (.getFirstByXPath page (str "//a[@href='" login-url "']")))

(defn main []
  (let [client (make-client BrowserVersion/FIREFOX_38)
        page (get-page client base-url)]
    (if (has-login-button? page)
      (println (get-page client login-url)))))

