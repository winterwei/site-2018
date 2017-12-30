(ns winter-website.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [reagent.session :as session]
    [secretary.core :as secretary :include-macros true]
    [accountant.core :as accountant]
    [winter-website.menu :as menu]
    [winter-website.main :as main]
    [winter-website.home :as home]
    [winter-website.about :as about]
    [winter-website.projects :as projects]
    [winter-website.contact :as contact]
    [winter-website.placeholder :as placeholder]))

(defn app []
  (let [page (session/get :page)]
    [:div.container
     [:div.menu-icon
      [:i.material-icons.md-36 {:on-click (fn [_] (session/update! :click-menu not))}
       (if (session/get :click-menu) "close" "menu")]]
     [menu/menu page]
     (case page
       :about [about/about]
       :main  [main/main]
       :projects [projects/projects]
       :contact [contact/contact])]))

;; -------------------------
;; Routes

(defn current-page []
  [(session/get :template)])

(secretary/defroute "/" []
  (session/put! :template #'home/home))

(secretary/defroute "/hello" []
  (session/put! :template #'app)
  (session/put! :page :main))

(secretary/defroute "/about" []
  (session/put! :template #'app)
  (session/put! :page :about))

(secretary/defroute "/projects" []
  (session/put! :template #'app)
  (session/put! :page :projects))

(secretary/defroute "/contact" []
  (session/put! :template #'app)
  (session/put! :page :contact))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [placeholder/page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
