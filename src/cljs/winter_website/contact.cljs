(ns winter-website.contact
  (:require
    [ajax.core :refer [POST]]
    [reagent.core :as r]
    [reagent.session :as session]
    ))

(defn anti-forgery-token
  []
  (-> js/document .-head (.querySelector "[name=CSRF]") .-content))

(defn contact
  []
  (let [my-name (r/atom (session/get :name ""))
        email   (r/atom "")
        message (r/atom "")]
    (fn []
      [:div.main
       [:div.main-content

        [:div.main-email-wrapper
         [:div.main-email
          [:div.email-meta
           [:h4 "New Message"]]
          [:div.email-title
           [:h1 "Hi Winter!"]]
          [:div
           [:div
            [:label {:for "name"} "Name"]
            [:input {:id "name" :value @my-name
                     :on-change (fn [e] (reset! my-name (.-value (.-target e))))}]
            ]
           [:div
            [:label {:for "email"} "Email"]
            [:input {:id "email" :value @email
                     :on-change (fn [e] (reset! email (.-value (.-target e))))}]
            ]
           [:div
            [:label {:for "message"} "Your message"]
            [:textarea {:id "message" :value @message
                        :on-change (fn [e] (reset! message (.-value (.-target e))))}]
            ]
           [:div
            [:button.fixed {:on-click #(POST "/send-message"
                                          {:params {:name  @my-name
                                                    :email @email
                                                    :message @message}
                                           :headers {"X-CSRF-Token" (anti-forgery-token)}})}
             "Send"]

            ]
           ]]]]])))
