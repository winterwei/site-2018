(ns winter-website.home
  (:require
    [clojure.string :as string]
    [reagent.core :as r]
    [reagent.session :as session]
    [accountant.core :as accountant]))

(def chat-input (r/atom ""))

(defn set-name-navigate!
  [e]
  (session/put! :name @chat-input)
  (accountant/navigate! "/hello"))

(defn home
  []

  [:div.page-wrapper
   [:div.page-left
    [:div.page-left__center
     [:div.avatar
      [:img {:src "/img/winter-avatar.svg"}]]

     [:div.intro
      [:div.title
       [:h1 "Winter"]]
      [:div.detail
       [:h3 "Designer, illustrator, developer*"]]
      ]]
    ]
   [:div.page-right
    [:div.page-right__center
     [:div.chat
      [:div.message-block
       [:div.message-block-left
        [:div.message.message-winter
         [:p.msg "Hi! I'm Winter."]]
        [:div.message.message-winter
         [:p.msg "It always seem a bit one-sided when you visit someone's website and learn everything about them, but they don't even know your name. Don't you think? "]
         ]
        [:div.message.message-winter
         [:p.msg "Mind telling me your name?"]
         ]]]

      [:div.message-block
       [:div.message-block-right
        [:div.message.message-guest
         [:button.button_message {:on-click (fn [e]
                                              (session/put! :name "Stranger")
                                              (accountant/navigate! "/hello"))} "I'm just a stranger."]
         [:div.message-guest-option
          ]]]]
      [:div.message-block
       ]]
     [:div.type
      [:input.messsage-input {:placeholder "Type your message..."
               :value @chat-input
               :on-change (fn [e]
                            (reset! chat-input (-> e .-target .-value)))
               :on-key-up (fn [e]
                            (if (and (= 13 (.-keyCode e)) (not (string/blank? @chat-input)))
                              (set-name-navigate! e)))}
       ]
      [:button.cta {:disabled (string/blank? @chat-input)
                :on-click set-name-navigate!} "Send"]]

     ]]])

