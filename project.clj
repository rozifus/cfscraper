(defproject cfscraper "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [enlive "1.1.6"]
                 [net.sourceforge.htmlunit/htmlunit "2.19"]]
  :main ^:skip-aot cfscraper.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
