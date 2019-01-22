(defproject erdh "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/java.jdbc "0.7.8"]
                 [mysql/mysql-connector-java "8.0.13"]
                 [com.layerware/hugsql "0.4.9"]
                 [io.forward/yaml "1.0.9"]
                 [org.clojure/tools.cli "0.4.1"]]
  :repl-options {:init-ns erdh.core}
  :main erdh.core
  :profiles {:uberjar {:aot :all}})
