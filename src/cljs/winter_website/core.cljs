(ns winter-website.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [reagent.session :as session]
    [secretary.core :as secretary :include-macros true]
    [accountant.core :as accountant]
    [winter-website.app :as app]))

(enable-console-print!)

;; -------------------------
;; Routes


(secretary/defroute "/" []
  (session/put! :article nil)
  (when-not (session/get :section)
    (session/put! :section 1))
  (session/put! :show-article? false))

(secretary/defroute "/more-than-just-reading-documents" []
  (session/put! :article 1)
  (session/put! :section 2)
  (session/put! :show-article? false))

(secretary/defroute "/more-than-just-reading-foo" []
  (session/put! :article 2)
  (session/put! :section 3)
  (session/put! :show-article? false))


;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [app/app] (.getElementById js/document "app")))

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
