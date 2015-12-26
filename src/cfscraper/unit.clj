(ns cfscraper.unit
  (:import (com.gargoylesoftware.htmlunit WebClient BrowserVersion))
  (:use [clojure.string :only [upper-case]]))

(def ^:dynamic *client*)

(def config-location "./config.txt")
(def no-config-fail-msg
  "No config found: Copy config.example to config.txt")

(try 
  (def config (read-string (slurp config-location)))
  (catch java.io.FileNotFoundException e 
    (println no-config-fail-msg)
    (System/exit 0)))
(println config)


(def base-url "http://customsforge.com/")
(def login-url "http://customsforge.com/index.php?app=core&module=global&section=login")

(def login-form-id "login")
(def username-field-id "ips_username")
(def password-field-id "ips_password")
(def login-submit-class "input_submit")

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

(defn set-field [field-id value]
  )

(defn login-via-form [client]
  (let [login-form (.getElementById client "login")]
    (if login-form
      (let [username-field (.getElementById "1")]))))

(defn main []
  (let [client (make-client BrowserVersion/FIREFOX_38)
        page (get-page client base-url)]
    (if (has-login-button? page)
      (println (get-page client login-url)))))

