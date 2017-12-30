(defproject winter-website "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Proprietary"}

  :dependencies [[cljs-ajax "0.7.3"]
                 [cljsjs/snapsvg "0.5.1-0"]
                 [compojure "1.6.0"]
                 [hiccup "1.0.5"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946" :scope "provided"]
                 [prismatic/dommy "1.1.0"]
                 [ring-server "0.5.0"]
                 [reagent "0.7.0"]
                 [reagent-utils "0.2.1"]
                 [ring "1.6.3"]
                 [ring/ring-defaults "0.3.1"]
                 [ring-middleware-format "0.7.2"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.2.3" :exclusions [org.clojure/tools.reader]]
                 [yogthos/config "0.9"]]

  :plugins [[lein-environ "1.0.2"]
            [lein-cljsbuild "1.1.5"]
            [lein-asset-minifier "0.2.7"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler winter-website.handler/app
         :uberwar-name "winter-website.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "winter-website.jar"

  :main winter-website.server

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  {:assets
   {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild
  {:builds {:min
            {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
             :compiler
             {:output-to "target/cljsbuild/public/js/app.js"
              :output-dir "target/uberjar"
              :optimizations :advanced
              :pretty-print  false}}
            :app
            {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
             :figwheel {:on-jsload "winter-website.core/mount-root"}
             :compiler
             {:main "winter-website.dev"
              :asset-path "/js/out"
              :output-to "target/cljsbuild/public/js/app.js"
              :output-dir "target/cljsbuild/public/js/out"
              :source-map true
              :optimizations :none
              :pretty-print  true}}
            }
   }


  :figwheel
  {:http-server-root "public"
   :server-port 3449
   :nrepl-port 7002
   :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"
                      ]
   :css-dirs ["resources/public/css"]
   :ring-handler winter-website.handler/app}

  :hooks [leiningen.sassc leiningen.autoprefixer]

  :sassc [{:src "src/sass/site.scss"
           :output-to "resources/public/css/site.css"
           :import-path "src/sass"}]

  :autoprefixer [{:src "resources/public/css/main.css"
                  :browsers "last 3 versions, IE > 10"}]

  :profiles {:dev {:repl-options {:init-ns winter-website.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[binaryage/devtools "0.9.8"]
                                  [com.cemerick/piggieback "0.2.2"]
                                  [figwheel-sidecar "0.5.14"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [pjstadig/humane-test-output "0.8.3"]
                                  [prone "1.1.4"]
                                  [ring/ring-mock "0.3.2"]
                                  [ring/ring-devel "1.6.3"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-auto "0.1.3"]
                             [lein-figwheel "0.5.11"]
                             [lein-sassc "0.10.4"]]

                   :auto {"sassc" {:paths ["src/sass"]
                                   :file-pattern  #"\.(scss)$"}}

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true}}

             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true}})
