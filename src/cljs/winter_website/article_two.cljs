(ns winter-website.article-two)

(defn image-two
  []
  [:div.article-headline.article-two
   [:img {:src "img/mindfully-edible-logo.svg"}]])

(defn article-two
  []
  [:div.article-content.article-two
   [:h1 "Beautifully Mindful"]
   [:div [:img {:src "img/mindfully-edible-logo.svg" :width "600px"}]]
   [:h2 "Challenge"]
   [:p "Designing the experience of teaching an AI what key clauses look like in contracts for domain
        experts by providing a highly interactive interface where reading, evaluating results, annotating
        text, and training are easy and intuitive."]
   [:p "Designing the experience of teaching an AI what key clauses look like in contracts for domain
        experts by providing a highly interactive interface where reading, evaluating results, annotating
        text, and training are easy and intuitive."]
   [:p "Designing the experience of teaching an AI what key clauses look like in contracts for domain
        experts by providing a highly interactive interface where reading, evaluating results, annotating
        text, and training are easy and intuitive."]])