(ns cfscraper.login
  (:gen-class)
  (:require [net.cgrand.enlive-html :as html]))

(def ^:dynamic *base-url* 
  "http://customsforge.com/index.php?app=core&module=global&section=login")

(def fake-agent "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36")

(defn set-http-agent [http-agent]
  (System/setProperty "http.agent" http-agent)) 

(defn print-agent []
  (println (System/getProperty "http.agent")))

(defn fetch-url [url]
  (try
    (html/html-resource (java.net.URL. url))
    (catch Exception e (str "Exception: " (.getMessage e)))))

(defn connect-url [url]
  (let [conn (.openConnection (java.net.URL. url))]
    (do 
      (.setRequestProperty conn "User-Agent" fake-agent)
      (.connect conn)
      conn)))

(defn cf-login-form []
  (let [res (html/html-resource (.getInputStream (connect-url *base-url*)))]
    (html/select res [:#login])))

(defn print-form []
  (println (cf-login-form)))

(defn main []
  (do
    (cf-login-form)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (main))
