(ns winter-website.about
  (:require
    [reagent.core :as reagent :refer [atom]]
    [reagent.session :as session]
    [dommy.core :as dommy :refer-macros [sel sel1]]))

(defn about
  []
  [:div.main {:on-scroll (fn [e] (.log js/console (.-scrollTop (.-target e)))
                           (.log js/console (pr-str (dommy/bounding-client-rect (sel1 :#spacer)))))}
   [:div.main-content
    [:div.main-email-wrapper
     [:div.main-email
      #_[:div.email-meta
         [:h4 "Aug 7, 2017"]]
      [:div.email-title
       [:h1 "About"]]
      [:div.email-body
       [:div.para-wrapper
        [:p "Dear " (session/get :name) ","]]
       [:div.para-wrapper
        [:p "Thanks for visiting my site. This site is not all about me. It's about what I wanted to communicate to you. Treat it as an inbox of letters I wrote to you. If you find my letters interesting, I look forward to hearing from you. "]]
       [:div.signature
        [:p "Xoxo,"]
        [:p "Winter"]
        ]
       ]]]
    [:div.main-email-wrapper
     [:div.main-email
      #_[:div.email-meta
         [:h4 "Aug 7, 2017"]]
      [:div.email-title
       [:h1 "About"]]
      [:div.email-body
       [:div.para-wrapper
        [:p "Dear " (session/get :name) ","]]
       [:div.para-wrapper
        [:p "Thanks for visiting my site. This site is not all about me. It's about what I wanted to communicate to you. Treat it as an inbox of letters I wrote to you. If you find my letters interesting, I look forward to hearing from you. "]]
       [:div.signature
        [:p "Xoxo,"]
        [:p "Winter"]
        ]
       ]]]
    [:div.main-email-wrapper
     [:div.main-email
      #_[:div.email-meta
         [:h4 "Aug 7, 2017"]]
      [:div.email-title
       [:h1 "About"]]
      [:div.email-body
       [:div.para-wrapper
        [:p "Dear " (session/get :name) ","]]
       [:div.para-wrapper
        [:p "Thanks for visiting my site. This site is not all about me. It's about what I wanted to communicate to you. Treat it as an inbox of letters I wrote to you. If you find my letters interesting, I look forward to hearing from you. "]]
       [:div.signature
        [:p "Xoxo,"]
        [:p "Winter"]
        ]
       ]]]
    [:div#spacer.spacer {:style {:height "900px" :background-color "pink"}}]]


   ])