(ns ^:figwheel-no-load winter-website.dev
  (:require
    [winter-website.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
