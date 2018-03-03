(ns winter-website.app
  (:require
    [cljsjs.snapsvg]
    [reagent.core :as r]
    [reagent.ratom :as ratom]
    [reagent.session :as session]
    [accountant.core :as accountant]
    [dommy.core :as dommy :refer-macros [by-id sel]]
    [winter-website.playing-cello :as playing-cello]
    [winter-website.article-one :refer [article-one]]
    [winter-website.article-two :refer [article-two]]))


(def article-scale 1.2)
(def home-scale 0.95)

;; Cubic spline computations Inspired by
;; http://greweb.me/2012/02/bezier-curve-based-easing-functions-from-concept-to-implementation/

(defn A [x1 x2] (+ 1.0 (* 3.0 (- x1 x2))))
(defn B [x1 x2] (- (* 3.0 x2) (* 6.0 x1)))
(defn C [x] (* 3.0 x))

(defn slope
  "Returns dx/dt given t, x1, and x2, or dy/dt given t, y1 and y2."
  [t x1 x2]
  (+ (* 3.0 (A x1 x2) t t)
     (* 2.0 (B x1 x2) t)
     (C x1)))

(defn bezier
  "Returns x(t) given t, x1, and x2 or y(t) given t, y1, and y2."
  [t x1 x2]
  (* (+ (* (+ (* (A x1 x2) t) (* (B x1 x2))) t) (C x1)) t))

(defn key-spline-fn
  "Return a function that computes a bezier easing value for x in [0,1]."
  [mx1 my1 mx2 my2]
  ;; Linear
  (if (and (= mx1 my1) (= mx2 my2))
    (fn [x] x)
    ;; Use Newton Raphson method.
    (let [t-for-x (fn [x] (loop [t x, i 0]
                            (if (< i 4)
                              (let [sl (slope t mx1 mx2)]
                                (if (= 0.0 sl)
                                  t
                                  (recur (- t (/ (- (bezier t mx1 mx2) x) sl)) (inc i))))
                              t)))]
      (fn [x]
        (bezier (t-for-x x) my1 my2)))))

(def ease-in (key-spline-fn 0.42 0.0 1.0 1.0))
(def ease-out (key-spline-fn 0.0 0.0 0.58 1.0))
(def ease-in-out (key-spline-fn 0.455 0.03 0.515 0.955))
(def ease-in-out-back (key-spline-fn 0.68 -0.55 0.265 1.55))

(defn animate!
  [data ks target ts easing-fn]
  (let [ks       (if (keyword? ks) [ks] ks)
        start-ts (.now js/performance)
        state    (atom {})
        tick-fn  (fn tick-fn [now]
                   (let [pct   (/ (double (- now (:start-time @state)))
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

(defn bubble-left [viewport-width] (/ (- viewport-width 280) 2))

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
        (swap! data assoc-in [:bubble :scale] 0.5)
        :else
        (if (session/get :article)
          (swap! data assoc-in [:bubble :scale] article-scale)
          (swap! data assoc-in [:bubble :scale] home-scale))))))

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
  (animate! data [:bubble :scale] article-scale 2000 ease-in-out-back)
  (set-show-article!)
  (case article-number
    1 (accountant/navigate! "/more-than-just-reading-documents")
    2 (accountant/navigate! "/more-than-just-reading-foo")))

(defn navigate-to-home!
  [data]
  (animate! data [:bubble :scale] home-scale 2000 ease-in-out-back)
  (accountant/navigate! "/"))

(defn bubble-line
  [scale x y]
  (let [transform (str scale " translate(" x "," y ")")]
    [:g {:stroke "url(#lineGradient)" :transform transform :fill "none"}
     [:path {:d "M832.82769,409.766212 C835.624826,539.994212 804.419538,635.917636 735.389913,712.231244 C655.591733,800.450212 483.395661,833.217257 403.597481,827.335993 C192.3843,811.767445 25.5389058,673.448506 5.44656416,556.797825 C-15.5529568,434.971632 28.9660276,366.917 57.5253761,250.972071 C82.0444168,151.444272 142.892629,75.3743161 275.080413,30.8447419 C359.918478,2.27860001 546.394224,-22.9268193 674.911292,108.981542 C747.78803,183.799628 829.467766,253.492613 832.82769,409.766212 Z"}]]))

(defn bubble-lines
  [_]
  (fn [data]
    (let [scale (str "scale(" (:scale data) "," (:scale data) ")")]
      [:svg {:width "835px" :height "822px" :viewBox "0 0 835 822"}
       [:defs
        [:linearGradient
         {:id "lineGradient" :x1 "56273.0023%" :y1 "26745.9004%" :x2 "41362.4259%" :y2 "111950%"}
         [:stop {:stop-color "#43445C" :offset "0%"}]
         [:stop {:stop-color "#A2706D" :offset "100%"}]
         [:stop {:stop-color "#FFFFFF" :offset "100%"}]]]
       (bubble-line scale 23 (+ (:translate-y data) 0))
       (bubble-line scale 10 (+ (:translate-y data) 25))
       (bubble-line scale 20 (+ (:translate-y data) 20))
       (bubble-line scale 25 (+ (:translate-y data) 10))])))

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
  (let [init-article   (session/get :article)
        data           (r/atom defaults)
        section        (ratom/reaction (session/get :section))
        height         (ratom/reaction (:project-height @data))
        article        (ratom/reaction (session/get :article))
        show-article?  (ratom/reaction (session/get :show-article?))
        mobile?        (ratom/reaction (:mobile? @data))
        viewport-width (ratom/reaction (:viewport-width @data))
        bubble-data    (ratom/reaction (:bubble @data))
        wheel-fn       (wheel-handler-fn data)
        resize-fn      (resize-handler-fn data)]
    (when init-article
      (set-show-article!))
    (r/create-class
      {:component-did-mount
       (fn []
         (println "mounted")
         ;; On creation, we invoke a resize event to set initial parameters.
         (resize-fn)
         (dommy/listen! js/window :wheel wheel-fn)
         (dommy/listen! js/window :resize resize-fn))
       :reagent-render
       (fn []
         [:div.wrapper
          [:div.main-left
           [:div#projects {:class (if @article "" "hidden")}
            [:div.autoscroll {:style {:transform (projects-transform @height @section)}}
             [:div.copy {:style {:height @height}}
                [:div.text
                 [:h1 "Hello." [:br] "I'm Winter."]
                 [:h2 "Principal Designer at Kira Systems."]
                 [:p "This is a paragraph where I talk about how amazing I am, humbly bragging about my achievements and all the blahs. And I'm dreading writing it."]
                 ]
                #_[:div.button-wrapper
                 [:a.button {:href "" :on-click (fn [_] (navigate-to-article! data 1))}
                  "Read Case Study"]]
                ]
             [:div.copy {:style {:height @height}}
              [:div.text
               [:h1 "More Than Just Reading Documents"]
               [:p "Designing the experience of teaching an AI what key clauses look like in contracts for domain experts by providing a highly interactive interface where reading, evaluating results, annotating texts, and training are easy and intuitive."]
               ]
              [:div.button-wrapper
               [:a.button {:href "" :on-click (fn [_] (navigate-to-article! data 1))}
                "Read Case Study"]]
              ]
             [:div.copy {:style {:height @height}}
              [:div.text
               [:h1 "More Than Just Reading Foo"]
               [:p "Designing the experience of teaching an AI what key clauses look like in contracts for domain experts by providing a highly interactive interface where reading, evaluating results, annotating texts, and training are easy and intuitive."]
               ]
              [:div.button-wrapper
               [:a.button {:href "" :on-click (fn [_] (navigate-to-article! data 2))} "Read Case Study"]]
              ]
             #_[:div#project3.copy {:style {:height @height}}
              [:div.text
               [:h1 "More Than Just Reading Bar"]
               [:p "Desining the experience of teaching an AI what key clauses look like in contracts for domain experts by providing a highly interactive interface where reading, evaluating results, annotating texts, and training are easy and intuitive."]
               ]
              [:div.button-wrapper
               [:a.button {:href ""} "Read Case Study"]]
              ]
             #_[:div#project4.copy {:style {:height @height}}
              [:div.text
               [:h1 "More Than Just Reading Baz"]
               [:p "Desining the experience of teaching an AI what key clauses look like in contracts for domain experts by providing a highly interactive interface where reading, evaluating results, annotating texts, and training are easy and intuitive."]
               ]
              [:div.button-wrapper
               [:a.button {:href ""} "Read Case Study"]]
              ]]]]

          [:div.image-wrapper {:style {:left (cond
                                               @article "0"
                                               @mobile? (bubble-left @viewport-width)
                                               :else    "50%")}}
           [bubble-lines @bubble-data]
           [:div.bubble {:style {:clip-path (str "url(#bubblePath" (:scale @bubble-data) ")")}}
            [:div.project-image {:class (if (= @section 1) "active" "inactive")}
             [playing-cello/page]]
            [:div.project-image {:class (if (= @section 2) "active" "inactive")}
             [:img {:src "img/doc-viewer-screen.png" :class (if @article "hidden")}]]
            ]]

          [bubble @bubble-data]

          (when @article
            [:div.article {:class (if @show-article? "normal" "below")}
             (case @article
               1 (article-one)
               2 (article-two))
             ])

          ])})))
