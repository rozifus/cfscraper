(ns cfscraper.unit
  (:import (com.gargoylesoftware.htmlunit 
             WebClient BrowserVersion NicelyResynchronizingAjaxController))
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
(def artists-url "http://ignition.customsforge.com/search/artists")

(def artist-list-xpath "//*[@id='artists-column']/a/@href")

(def login-form-id "login")
(def username-field-name "ips_username")
(def password-field-name "ips_password")
(def login-submit-value "Sign In")

(def ^:dynamic *current-url* base-url)

(defn make-client
  ([]
     (make-client (BrowserVersion/getDefault)))
  ([version]
     (println (str "Browser: " (.getNickname version)))
     (let [client (WebClient. version)]
       (.setAjaxController client (NicelyResynchronizingAjaxController.))
       (-> client (.getOptions) (.setThrowExceptionOnScriptError false))
       client)))

(defn get-page
  [client url]
  (.getPage client url))

(defn get-login-form [page]
  (.getElementById page "login"))

(defn login-via-form [login-form]
  (let [username-field (.getInputByName login-form username-field-name)
        password-field (.getInputByName login-form password-field-name)
        submit-button (.getInputByValue login-form login-submit-value)]
    (.setValueAttribute username-field (:username config))
    (.setValueAttribute password-field (:password config))
    (.click submit-button)))

(defn log-in [client]
  (if-let [login-form (get-login-form (get-page client base-url))]
      (login-via-form login-form)))

(defn get-artist-url-list [client]
  (let [page (get-page client artists-url)]
    (map #(.getNodeValue %) (.getByXPath page artist-list-xpath))))

(defn main []
  (let [client (make-client BrowserVersion/FIREFOX_38)]
    (log-in client)
    (let [artist-pages (get-artist-url-list client)]
      (println artist-pages))))
  
