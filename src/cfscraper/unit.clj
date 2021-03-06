(ns cfscraper.unit
  (:import (com.gargoylesoftware.htmlunit 
             WebClient BrowserVersion WebWindowListener
             NicelyResynchronizingAjaxController))
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
(def search-url "http://ignition.customsforge.com")
(def artists-url "http://ignition.customsforge.com/search/artists")
(def download-link-base-url "http://customsforge.com/process.php?id=")

(def artist-list-xpath "//*[@id='artists-column']/a/@href")
(def song-list-xpath "//*/div[@id='groups-content']/div/div[2]/table/tbody/tr")
(def search-button-xpath "//*[@id='search']")
(def next-page-enabled-xpath 
  "//*[@id='pag-next' and contains(concat(' ', @class, ' '), ' open ')]")
(def song-in-search-xpath "//*[@id='search_table']/tbody/tr")
(def song-id-xpath "td/@id")

(def copy-com-unique-xpath "//main[@id='copy-file-browser-main']")

(def copy-com-download-button-xpath 
  "//div[@class='actions']/a[@class='button button-primary']")

(def copy-com-download-button-xpath 
  "//a[contains(@class, 'button') and contains(@class, 'button-primary')]")

(def copy-com-download-button-xpath 
  "a")



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

(defn parint [v]
  (do
    (println v)
    v))

(defn get-page
  [client url]
  (.getPage client url))

(defn get-web-window-listener []
  (proxy [WebWindowListener] []
    (webWindowOpened [event] 
      (println event)))) 

(defn get-login-form [page]
  (.getElementById page "login"))

(defn login-via-form [login-form]
  (let [username-field (.getInputByName login-form username-field-name)
        password-field (.getInputByName login-form password-field-name)
        submit-button (.getInputByValue login-form login-submit-value)]
    (.setValueAttribute username-field (:username config))
    (.setValueAttribute password-field (:password config))
    (.click submit-button)))

(defn log-in []
  (if-let [login-form (get-login-form (get-page *client* base-url))]
      (login-via-form login-form)))

(defn get-artist-url-list [client]
  (let [page (get-page client artists-url)]
    (parint (map #(.getValue %) (.getByXPath page artist-list-xpath)))))

(defn get-songs [client artist-url]
  (let [x (parint artist-url)
        artist-page (get-page client artist-url)]
    (.getByXPath artist-page song-list-xpath)))

(defn get-search-page [client]
  (get-page client search-url))

(defn get-song-rows [page]
  (.getByXPath page song-in-search-xpath))

(defn get-song-id-for-row [row]
  (.getValue (.getFirstByXPath row song-id-xpath)))

(defn get-song-data [row]
  {:id (.getValue (.getFirstByXPath row song-id-xpath))})

(defn download-from-copy-com [song-data page]
  (let [button (.getFirstByXPath page copy-com-download-button-xpath)]
    (println (.asText page))
    (System/exit 0)))

(defn page-is-copy-com? [page]
  (.getFirstByXPath page copy-com-unique-xpath))

(defn page-is-dropbox? [page]
  false)

(defn download-from-dropbox [song-data page]
  false)

(defn download-from-fileservice [song-data page]
  (cond
    (page-is-copy-com? page) (download-from-copy-com song-data page)
    (page-is-dropbox? page) (download-from-dropbox song-data page)
    :else (println (str "UNKNOWN FILESERVICE! id=" (:id song-data)))))

(defn download-song [song-data]
  (let [url (str download-link-base-url (:id song-data))
        page (get-page *client* url)]
    (download-from-fileservice song-data page)))

(defn dl-songs [page]
  (let [song-rows (get-song-rows page)
        song-data (map get-song-data song-rows)]
    (doall (map download-song song-data))))

(defn click-next-button? [page]
  (let [button (.getFirstByXPath page next-page-enabled-xpath)]
    (.click button)
    button))

(defn main []
  (binding [*client* (make-client BrowserVersion/FIREFOX_38)]
    (log-in)
    (loop [page (get-page *client* search-url)]
      (dl-songs page)
      (if (click-next-button? page)
        (recur page)
        (println "DONE")))))

  
