(defproject clj-fiddle "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [lib-noir "0.7.6"]
                 [compojure "1.1.6"]
                 [ring-server "0.3.1"]
                 [ring/ring-json "0.2.0"]
                 [com.taoensso/timbre "2.7.1"]
                 [com.postspectacular/rotor "0.1.0"]
                 [com.taoensso/tower "2.0.0"]
                 [markdown-clj "0.9.35"]
                 [environ "0.4.0"]]
  :aot :all
  :plugins [[lein-ring "0.8.7"]
            [lein-environ "0.4.0"]]
  :ring {:handler clj-fiddle.handler/app
         :init    clj-fiddle.handler/init
         :destroy clj-fiddle.handler/destroy}
  :profiles
  {:production {:ring {:open-browser? false
                       :stacktraces?  false
                       :auto-reload?  false}}
   :dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.2.1"]]
         :env {:selmer-dev true}}}
  :min-lein-version "2.0.0"
  :repl-options {:init-ns clj-fiddle.repl
                 :init (start-server)})
