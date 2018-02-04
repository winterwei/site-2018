(ns winter-website.app
  (:require
    [cljsjs.snapsvg]
    [reagent.core :as r]
    [reagent.ratom :as ratom]
    [reagent.session :as session]
    [accountant.core :as accountant]
    [dommy.core :as dommy :refer-macros [by-id sel]]
    [winter-website.article-one :refer [article-one]]
    [winter-website.article-two :refer [article-two]]))

(defn linear [pct] pct)
(defn ease-in-quad [pct] (* pct pct))
(defn ease-out-quad [pct] (- (* pct (- pct 2.0))))
(defn ease-in-out-quad
  [pct]
  (if (< pct 0.5)
    (/ (* pct pct) 2.0)
    (/ (- (+ (* pct (- pct 2.0)) 0.5)) 2.0)))

(defn animate!
  [data ks target ts easing-fn]
  (let [ks       (if (keyword? ks) [ks] ks)
        start-ts (.now js/performance)
        state    (atom {})
        tick-fn  (fn tick-fn [now]
                   (let [tick  (min now (:end-time @state))
                         pct   (/ (double (- now (:start-time @state)))
                                  (double (- (:end-time @state) (:start-time @state))))
                         value (+ (:value-start @state)
                                  (* (:value-change @state) (easing-fn pct)))]
                     (swap! data assoc-in ks value)
                     (if-not (> now (:end-time @state))
                       (.requestAnimationFrame js/window tick-fn))))
        value-start (get-in @data ks)]
    (swap! state assoc
           :start-time   start-ts
           :end-time     (+ start-ts ts)
           :value-start  value-start
           :value-change (- target value-start))
    (.requestAnimationFrame js/window tick-fn)))


(def medium-width 720)
(def small-width 720)

(defn morph!
  [id attrs & [ts]]
  (-> js/Snap
      (.select id)
      (.animate (clj->js attrs) (or ts 2000) (.-easeinout js/mina) (fn[_] (println "WTF")))))

(defn morph-bubble!
  [_ _ old-state new-state]
  (when old-state
    (let [scale-factor (/ (:scale new-state) (:scale old-state))
          transform    (str "scale(" (:scale new-state) "," (:scale new-state) ") translate(0," (:translate-y new-state) ")")]
      (println (:scale old-state) (:scale new-state) scale-factor (:translate-y new-state) transform)
      (.log js/console (.select js/Snap "#bubblePath"))
      (morph! "#bubblePath" {:transform transform} 200)
      #_(swap! a update :foo not))))

;; Mac kinetic scrolling on the touchpad means that you'll get scroll events
;; even after the user has stopped moving their fingers. To combat this causing
;; rough scrolls, we block scrolling until no instruction has been received for
;; 50 ms.
(defn start-kinetic-scroll!
  [data]
  (.setTimeout js/window (fn [_] (swap! data assoc :allow-scroll? true :kinetic-timeout-id nil)) 50))

(defn update-kinetic-scroll!
  [data]
  (when-let [timeout-id (:kinetic-timeout-id @data)]
    (.clearTimeout js/window timeout-id)
    (swap! data assoc :kinetic-timeout-id (start-kinetic-scroll! data))))

(defn block-scroll!
  "Block the scroll for t ms."
  [data t]
  (let [kinetic-timeout-id (start-kinetic-scroll! data)]
    (swap! data assoc
           :in-animation? true
           :allow-scroll? false
           :kinetic-timeout-id kinetic-timeout-id))
  (.setTimeout js/window (fn [_] (swap! data assoc :in-animation? false)) t))

(defn next-section!
  [data]
  (session/update! :section #(min 4 (inc %))))

(defn prev-section!
  [data]
  (session/update! :section #(max 1 (dec %))))

(defn wheel-handler-fn
  [data]
  (fn [e]
    (update-kinetic-scroll! data)
    (when (and (not (:in-animation? @data)) (:allow-scroll? @data) (not (session/get :article)))
      (cond
        (> (.-deltaY e) 0)
        (do
          (next-section! data)
          (block-scroll! data 1000))
        (< (.-deltaY e) 0)
        (do (prev-section! data)
            (block-scroll! data 1000))))))

(defn bubble-left [viewport-width] (/ (- viewport-width 450) 2))

(defn resize-handler-fn
  [data]
  (fn [_]
    (let [viewport-width (.-innerWidth js/window)]
      (swap! data assoc
             :viewport-width viewport-width
             :viewport-height (.-innerHeight js/window)
             :project-height (:height (dommy/bounding-client-rect (by-id "projects")))
             :mobile?        (<= viewport-width medium-width))
      (cond
        (<= viewport-width small-width)
        #_(morph-bubble! data 0.5 -180 1)
        #_(morph! "#bubblePath" {:transform (str "scale(0.5,0.5) translate(0,-180)")} 1)
        (swap! data assoc-in [:bubble :scale] 0.5)
        :else
        (swap! data assoc-in [:bubble :scale] 0.95)
        #_(morph! "#bubblePath" {:transform "scale(0.95,0.95) translate(0,-180)"} 1)
        #_(morph-bubble! data 0.95 -180 1)))))

(defn projects-transform
  [height section]
  (str "translate3d(0px," (* (- height) (dec section)) "px,0px)"))

(def defaults
  {:bubble {:scale  1.0, :translate-y -180}
   :in-animation? false
   :allow-scroll? true
   :mobile? false
   :viewport-width nil
   :project-height nil})

(defn set-show-article!
  []
  (.setTimeout js/window (fn [_] (session/put! :show-article? true)) 20))

(defn navigate-to-article!
  [data article-number]
  #_(morph-bubble! data 1.5 -230)
  #_(morph! "#bubblePath" {:transform "scale(1.5,1.5) translate(0,-230)"})
  (animate! data [:bubble :scale] 2.0 2000 ease-in-out-quad)
  (set-show-article!)
  (case article-number
    1 (accountant/navigate! "/more-than-just-reading-documents")
    2 (accountant/navigate! "/more-than-just-reading-foo")))

(defn bubble
  [_]
  (fn [data]
    (let [transform (str "scale(" (:scale data) "," (:scale data) ") translate(0," (:translate-y data) ")")]
      [:svg {:width "0%" :height "0%"}
       [:defs
        [:clipPath {:id (str "bubblePath" (:scale data)) :transform transform}
         [:path {:d "M874.82438,409.605553 C877.675243,542.374587 845.870573,640.169688 775.515049,717.972342 C694.184131,807.912655 518.680572,841.319057 437.349654,835.323036 C222.088118,819.450713 52.0465713,678.432867 31.55118,559.506075 C10.1483069,435.302786 55.5223979,365.920258 84.6303053,247.712989 C109.6203,146.243184 171.637265,68.6889369 306.364071,23.290493 C392.831678,-5.83303706 582.889191,-31.5302695 713.874774,102.951913 C788.151305,179.229865 871.39992,250.282712 874.82438,409.605553 Z"}]]]])))

(defn app
  []
  (let [init-article (session/get :article)
        data      (r/atom defaults)
        section   (ratom/reaction (session/get :section))
        height    (ratom/reaction (:project-height @data))
        article   (ratom/reaction (session/get :article))
        show-article? (ratom/reaction (session/get :show-article?))
        mobile?   (ratom/reaction (:mobile? @data))
        viewport-width (ratom/reaction (:viewport-width @data))
        bubble-data (ratom/reaction (:bubble @data))
        wheel-fn  (wheel-handler-fn data)
        resize-fn (resize-handler-fn data)]
    (when init-article
      (set-show-article!))
    (r/create-class
      {:component-did-mount
       (fn []
         (println "mounted")
         ;; On creation, we invoke a resize event to set initial parameters.
         (resize-fn)
         (dommy/listen! js/window :wheel wheel-fn)
         (dommy/listen! js/window :resize resize-fn)

         #_(when @article
           (morph-bubble! data 1.2 -230 2)
           #_(morph! "#bubblePath" {:transform "scale(1.2,1.2) translate(0,-230)"} 2)))
       :reagent-render
       (fn []
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
           [:div#projects {:class (if @article "" "hidden")}
            [:div.autoscroll {:style {:transform (projects-transform @height @section)}}
             [:div.copy {:style {:height @height}}
              [:div.text
               [:h1 "More Than Just Reading Documents"]
               [:p "Desining the experience of teaching an AI what key clauses look like in contracts for domain experts by providing a highly interactive interface where reading, evaluating results, annotating texts, and training are easy and intuitive."]
               ]
              [:div.button-wrapper
               [:a.button {:href "" :on-click (fn [_] (navigate-to-article! data 1))}
                "Read Case Study"]]
              ]
             [:div.copy {:style {:height @height}}
              [:div.text
               [:h1 "More Than Just Reading Foo"]
               [:p "Desining the experience of teaching an AI what key clauses look like in contracts for domain experts by providing a highly interactive interface where reading, evaluating results, annotating texts, and training are easy and intuitive."]
               ]
              [:div.button-wrapper
               [:a.button {:href "" :on-click (fn [_] (navigate-to-article! data 2))} "Read Case Study"]]
              ]
             [:div#project3.copy {:style {:height @height}}
              [:div.text
               [:h1 "More Than Just Reading Bar"]
               [:p "Desining the experience of teaching an AI what key clauses look like in contracts for domain experts by providing a highly interactive interface where reading, evaluating results, annotating texts, and training are easy and intuitive."]
               ]
              [:div.button-wrapper
               [:a.button {:href ""} "Read Case Study"]]
              ]
             [:div#project4.copy {:style {:height @height}}
              [:div.text
               [:h1 "More Than Just Reading Baz"]
               [:p "Desining the experience of teaching an AI what key clauses look like in contracts for domain experts by providing a highly interactive interface where reading, evaluating results, annotating texts, and training are easy and intuitive."]
               ]
              [:div.button-wrapper
               [:a.button {:href ""} "Read Case Study"]]
              ]]]]

          [:div.image-wrapper {:class (str "project" @section)
                               :style {:left (cond
                                               @article "0"
                                               @mobile? (bubble-left @viewport-width)
                                               :else    "50%")
                                       :clip-path (str "url(#bubblePath" (:scale @bubble-data) ")")}}
           [:img {:src "img/doc-viewer-screen.png" :class (if @article "hidden")}]]

          [bubble @bubble-data]
          #_[:svg {:width "0%" :height "0%"}
           [:defs
            [:clipPath {:id "bubblePath"}
             [:path {:d "M874.82438,409.605553 C877.675243,542.374587 845.870573,640.169688 775.515049,717.972342 C694.184131,807.912655 518.680572,841.319057 437.349654,835.323036 C222.088118,819.450713 52.0465713,678.432867 31.55118,559.506075 C10.1483069,435.302786 55.5223979,365.920258 84.6303053,247.712989 C109.6203,146.243184 171.637265,68.6889369 306.364071,23.290493 C392.831678,-5.83303706 582.889191,-31.5302695 713.874774,102.951913 C788.151305,179.229865 871.39992,250.282712 874.82438,409.605553 Z"}]]]]

          (when @article
            [:div.article {:class (if @show-article? "normal" "below")}
             (case @article
               1 (article-one)
               2 (article-two))
             ])

          ])})))
