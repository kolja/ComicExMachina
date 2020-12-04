(defproject clj-electron "0.1.0-SNAPSHOT"
  :license {:name "The MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :source-paths ["src"]
  :description "electron app (clojure + rum) starting template"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "1.0.0-alpha2"]
                 [binaryage/oops "0.7.0"]
                 [ring/ring-core "1.7.1"]]
  :plugins [[lein-cljsbuild "1.1.8"]
            [lein-figwheel "0.5.19"]
            [lein-cooper "1.2.2"]]

  :clean-targets ^{:protect false} ["resources/main.js"
                                    "resources/public/js/ui-core.js"
                                    "resources/public/js/ui-core.js.map"
                                    "resources/public/js/ui-out"]
  :cljsbuild
  {:builds
   [{:source-paths ["node_modules/canvas" "src/electron"]
     :id "electron-dev"
     :compiler {:output-to "resources/main.js"
                :output-dir "resources/public/js/electron-dev"
                :optimizations :simple
                ;:install-deps true
                :foreign-libs [{:file "node_modules/canvas/index.js"
                                ;:provides "canvas"
                                :module-type :es6
                                }]
                ;:npm-deps {:canvas "2.6.1"}
                :pretty-print true
                :cache-analysis true}}
    {:source-paths ["src/ui" "src/dev"]
     :id "frontend-dev"
     :figwheel true
     :compiler {:output-to "resources/public/js/ui-core.js"
                :output-dir "resources/public/js/ui-out"
                :source-map true
                :asset-path "js/ui-out"
                :optimizations :none
                :cache-analysis true
                :main "dev.core"}}
    {:source-paths ["src/electron"]
     :id "electron-release"
     :compiler {:output-to "resources/main.js"
                :output-dir "resources/public/js/electron-release"
                :externs ["cljs-externs/common.js"]
                :optimizations :advanced
                :install-deps true
                :foreign-libs [{:file "node_modules/canvas/index.js"
                                :provides "canvas"
                                :module-type :es6}]
                :npm-deps {:canvas "2.6.1"}
                :cache-analysis true
                :infer-externs true}}
    {:source-paths ["src/ui"]
     :id "frontend-release"
     :compiler {:output-to "resources/public/js/ui-core.js"
                :output-dir "resources/public/js/ui-release-out"
                :source-map "resources/public/js/ui-core.js.map"
                :externs ["cljs-externs/common.js"]
                :optimizations :advanced
                :cache-analysis true
                :infer-externs true
                :process-shim false
                :main "ui.core"}}]}
  :figwheel {:http-server-root "public"
             :css-dirs ["resources/public/css"]
             :ring-handler tools.figwheel-middleware/app
             :server-port 3449})
