(ns winter-website.handler
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [winter-website.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.util.response :as response]))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   [:meta {:name "CSRF" :content *anti-forgery-token*}]
   [:link {:href "https://fonts.googleapis.com/icon?family=Material+Icons"
           :rel "stylesheet"}]

   (include-js "https://use.typekit.net/vjr3txl.js")
   [:script "try{Typekit.load({ async: true });}catch(e){}"]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))

   ])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))


(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/hello" [] (loading-page))
  (GET "/about" [] (loading-page))
  (GET "/projects" [] (loading-page))
  (GET "/contact" [] (loading-page))
  (POST "/send-message" request
    (println (:params request))
    (response/response "success"))

  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
