(ns winter-website.article-one)

(defn image-one
  []
  [:div.article-headline.article-one
   [:img {:src "img/doc-viewer-screen.png"}]])

(defn article-one
  []
  [:div.article-content.article-one
   [:h1 "More Than Just" [:br] "Reading Documents"]
   [:div [:img {:src "img/doc-viewer-screen.png"}]]
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
