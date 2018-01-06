(ns winter-website.home
  (:require
    [clojure.string :as string]
    [reagent.core :as r]
    [reagent.ratom :as ratom]
    [reagent.session :as session]
    [accountant.core :as accountant]
    [dommy.core :as dommy :refer-macros [by-id sel]]))

(defn next-section!
  [data]
  (swap! data update :section #(min 4 (inc %))))

(defn prev-section!
  [data]
  (swap! data update :section #(max 1 (dec %))))

(defn wheel-handler-fn
  [data]
  (fn [e]
    (println (.-deltaY e))
    (cond
      (> (.-deltaY e) 0)
      (next-section! data)
      (< (.-deltaY e) 0)
      (prev-section! data))))

(defn projects-transform
  [height section]
  (str "translate3d(0px," (* (- height) (dec section)) "px,0px)"))

(defn home
  []
  (let [data     (r/atom {:section 1})
        section  (ratom/reaction (:section @data))
        height   (ratom/reaction (:project-height @data))
        wheel-fn (wheel-handler-fn data)]
    (r/create-class
      {:component-did-mount
       (fn []
         (swap! data assoc :project-height (:height (dommy/bounding-client-rect (by-id "projects"))))
         (dommy/listen! js/window :wheel wheel-fn))
       :reagent-render
       (fn []
         (println @section)
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
              [:a {:href "/who"} "Who"]]
             ]]
           [:div#projects
            [:div.autoscroll {:style {:transform (projects-transform @height @section)}}
             [:div.copy {:style {:height @height}}
              [:div.text
               [:h1 "More Than Just Reading Documents"]
               [:p "Desining the experience of teaching an AI what key clauses look like in contracts for domain experts by providing a highly interactive interface where reading, evaluating results, annotating texts, and training are easy and intuitive."]
               ]
              [:div.button-wrapper
               [:a.buton {:href ""} "Read Case Study"]]
              ]
             [:div.copy {:style {:height @height}}
              [:div.text
               [:h1 "More Than Just Reading Foo"]
               [:p "Desining the experience of teaching an AI what key clauses look like in contracts for domain experts by providing a highly interactive interface where reading, evaluating results, annotating texts, and training are easy and intuitive."]
               ]
              [:div.button-wrapper
               [:a.buton {:href ""} "Read Case Study"]]
              ]
             [:div#project3.copy {:style {:height @height}}
              [:div.text
               [:h1 "More Than Just Reading Bar"]
               [:p "Desining the experience of teaching an AI what key clauses look like in contracts for domain experts by providing a highly interactive interface where reading, evaluating results, annotating texts, and training are easy and intuitive."]
               ]
              [:div.button-wrapper
               [:a.buton {:href ""} "Read Case Study"]]
              ]
             [:div#project4.copy {:style {:height @height}}
              [:div.text
               [:h1 "More Than Just Reading Baz"]
               [:p "Desining the experience of teaching an AI what key clauses look like in contracts for domain experts by providing a highly interactive interface where reading, evaluating results, annotating texts, and training are easy and intuitive."]
               ]
              [:div.button-wrapper
               [:a.buton {:href ""} "Read Case Study"]]
              ]]]]

          [:div.image-wrapper {:class (str "project" @section)}
           [:img {:src "img/doc-viewer-screen.png"}]]
          [:svg {:width "0%" :height "0%"}
           [:defs
            [:clipPath {:id "bubblePath" :transform "scale(0.95,0.95) translate(0,-180)"}
             [:path {:d "M874.82438,409.605553 C877.675243,542.374587 845.870573,640.169688 775.515049,717.972342 C694.184131,807.912655 518.680572,841.319057 437.349654,835.323036 C222.088118,819.450713 52.0465713,678.432867 31.55118,559.506075 C10.1483069,435.302786 55.5223979,365.920258 84.6303053,247.712989 C109.6203,146.243184 171.637265,68.6889369 306.364071,23.290493 C392.831678,-5.83303706 582.889191,-31.5302695 713.874774,102.951913 C788.151305,179.229865 871.39992,250.282712 874.82438,409.605553 Z"}]]]]
          ])})))
