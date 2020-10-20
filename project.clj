(defproject fluxo "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [bidi "2.1.6"]
                 [cljs-ajax "0.8.1"]
                 [day8.re-frame/http-fx "0.2.1"]
                 [kibu/pushy "0.3.8"]
                 [prismatic/dommy "1.1.0"]
                 [re-frame "1.1.1"]
                 [reagent "1.0.0-alpha2"]
                 [thheller/shadow-cljs "2.11.4"]
                 [devcards "0.2.6"]
                 [day8.re-frame/test "0.1.5"]]

  :plugins [[lein-shadow "0.3.1"]
            [lein-shell "0.5.0"]
            [lein-cljfmt "0.7.0"]]

  :min-lein-version "2.9.0"

  :jvm-opts ["-Xmx1G"]

  :source-paths ["src/clj"
                 "src/cljs"]

  :test-paths   ["test/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"
                                    "test/js"]

  :shadow-cljs {:nrepl {:port 8777}

                :builds {:app {:target     :browser
                               :output-dir "resources/public/js/compiled"
                               :asset-path "/js/compiled"
                               :modules    {:main {:init-fn  fluxo.core/init
                                                   :preloads [devtools.preload]}}

                               :devtools {:http-root "resources/public"
                                          :http-port 8280}}

                         :browser-test {:target    :browser-test
                                        :ns-regexp "-test$"
                                        :runner-ns shadow.test.browser
                                        :test-dir  "target/browser-test"
                                        :devtools  {:http-root "target/browser-test"
                                                    :http-port 8290}}

                         :cards {:asset-path       "/js/compiled"
                                 :modules          {:main {:init-fn fluxo.cards/init
                                                           :preloads [devtools.preload]}}
                                 :compiler-options {:devcards true}
                                 :output-dir       "resources/public/js/compiled"
                                 :target           :browser
                                 :devtools         {:http-root "resources/public"
                                                    :http-port 8280}
                                 }

                         :karma-test {:target    :karma
                                      :ns-regexp "-test$"
                                      :output-to "target/karma-test.js"}}}

  :shell {:commands {"karma" {:windows         ["cmd" "/c" "karma"]
                              :default-command "karma"}

                     "open" {:windows ["cmd" "/c" "start"]
                             :macosx  "open"
                             :linux   "xdg-open"}}}

  :aliases {"watch"        ["with-profile" "dev" "do"
                            ["shadow" "watch" "app" "browser-test" "karma-test"]]

            "cards"        ["with-profile" "dev" "do"
                            ["shadow" "watch" "cards"]]

            "release"      ["with-profile" "prod" "do"
                            ["shadow" "release" "app"]]

            "build-report" ["with-profile" "prod" "do"
                            ["shadow" "run" "shadow.cljs.build-report" "app" "target/build-report.html"]
                            ["shell" "open" "target/build-report.html"]]

            "ci"           ["with-profile" "prod" "do"
                            ["shadow" "compile" "karma-test"]
                            ["shell" "karma" "start" "--single-run" "--reporters" "junit,dots"]]}

  :profiles {:dev {:dependencies [[binaryage/devtools "1.0.2"]]
                   :source-paths ["dev"]}

             :prod {}}

  :prep-tasks [])
