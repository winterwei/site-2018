(ns winter-website.middleware
  (:require [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.format :refer [wrap-restful-format]]))

(defn wrap-middleware [handler]
  (-> handler
      (wrap-defaults site-defaults)
      wrap-restful-format
      wrap-exceptions
      wrap-reload))
