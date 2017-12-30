(ns winter-website.middleware
  (:require
    [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
    [ring.middleware.format :refer [wrap-restful-format]]))

(defn wrap-middleware [handler]
  (-> handler
      (wrap-defaults site-defaults)
      wrap-restful-format))
