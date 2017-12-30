(ns winter-website.prod
  (:require [winter-website.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
