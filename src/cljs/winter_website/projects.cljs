(ns winter-website.projects
  (:require
    [reagent.core :as reagent :refer [atom]]
    [reagent.session :as session]))

(defn projects
  []
  [:div.main
   [:div.main-content
    [:div.main-email-wrapper
     [:div.main-email
      [:div.email-meta
       [:h4 "Aug 7, 2017"]]
      [:div.email-title
       [:h1 "Projects " (session/get :name) "!"]]
      [:div.email-body
       [:div.para-wrapper
        [:p "Dear " (session/get :name) ","]]
       [:div.para-wrapper
        [:p "Thanks for visiting my site. This site is not all about me. It's about what I wanted to communicate to you. Treat it as an inbox of letters I wrote to you. If you find my letters interesting, I look forward to hearing from you. "]]
       [:div.signature
        [:p "Xoxo,"]
        [:p "Winter"]
        ]
       ]]]]

   ])
