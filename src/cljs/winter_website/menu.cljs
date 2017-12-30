(ns winter-website.menu
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]))

(defn menu
  [active]
  [:div.sidebar {:class (if (session/get :click-menu) "show")}
   [:div.menu-avatar
    [:a {:href "/"}
     [:img {:src "/img/winter-avatar-menu.svg"}]]
    ]
   [:nav.menu
    [:ul.main-nav

     [:li
      [:a {:href "/hello" :class (if (= active :main) "active")}
       [:i.material-icons.md-18 "mail"] "Hello"]]
     [:li
      [:a {:href "/projects" :class (if (= active :projects) "active")}
       [:i.material-icons.md-18 "favorite"] (str "Projects")]
      #_[:ul.sub-nav
         [:li
          [:a {:href "#"} "Product"]]
         [:li
          [:a {:href "#"} "Web"]]
         [:li
          [:a {:href "#"} "Branding"]]
         [:li
          [:a {:href "#"} "Illustration"]]
         [:li
          [:a {:href "#"} "Misc."]]]]
     [:li
      [:a {:href "/about" :class (if (= active :about) "active")} [:i.material-icons.md-18 "star"] (str "About")]]
     [:li
      [:a {:href "/contact" :class (if (= active :contact) "active")}
       [:i.material-icons.md-18 "create"] "Compose"]]
     ]]]

  )
