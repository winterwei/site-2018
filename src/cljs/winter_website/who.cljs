(ns winter-website.who
  (:require
    [reagent.core :as reagent :refer [atom]]
    [reagent.session :as session]
    [dommy.core :as dommy :refer-macros [sel sel1]]))


(defn who
  []
  [:div.wrapper
   [:div.main-left
    [:nav.menu
     [:ul.main-nav
      [:li
       [:a {:href "/" } "WW"]]
      [:li
       [:a {:href "/work"} "Work"]]
      [:li
       [:a {:href "/words"} "Words"]]
      [:li
       [:a {:href "/about"} "Who"]]
      ]]
    [:div.copy
     [:div.text
      [:h1 "More Than Just Reading Documents"]
      [:p "Desining the experience of teaching an AI what key clauses look like in contracts for domain experts by providing a highly interactive interface where reading, evaluating results, annotating texts, and training are easy and intuitive."]
      ]
     [:div.button-wrapper
      [:a.buton {:href ""} "Read Case Study"]]
     ]]


   [:div.main-right
    [:div.image-wrapper
    [:img {:src "img/img-1.png"}]


    ]]]




  )