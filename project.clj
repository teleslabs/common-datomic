(defproject org.clojars.teleslabs/common-datomic "1.1.1"
  :description "Common Datomic library"
  :url "https://pedroteles.dev"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[com.github.clojure-lsp/lein-clojure-lsp "1.3.9"]]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [prismatic/schema "1.2.1"]
                 [org.clojars.pedroso/fakeflix-schema "1.0.1"]
                 [com.datomic/datomic-free "0.9.5697"]]
  :aliases {"diagnostics"  ["clojure-lsp" "diagnostics"]
            "format"       ["clojure-lsp" "format" "--dry"]
            "format-fix"   ["clojure-lsp" "format"]
            "clean-ns"     ["clojure-lsp" "clean-ns" "--dry"]
            "clean-ns-fix" ["clojure-lsp" "clean-ns"]
            "lint"         ["do" ["diagnostics"] ["format"] ["clean-ns"]]
            "lint-fix"     ["do" ["format-fix"] ["clean-ns-fix"]]})
