(ns leiningen.autoprefixer
  (:require
    [clojure.java.shell :as shell]
    [clojure.string :as string]
    [robert.hooke :as hooke]
    [leiningen.sassc :as sassc]))

(defn autoprefixer
  [project & args]
  (doseq [config (:autoprefixer project)]
    (let [cmd ["postcss"
               (:src config)
               "--use" "autoprefixer"
               "-r"
               "--autoprefixer.browsers" (:browsers config)]]
      (println (string/join " " cmd))
      (apply shell/sh cmd))))

(defn autoprefixer-hook [task & args]
  (apply task args)
  (autoprefixer (first args)))

(defn activate []
  (hooke/add-hook #'sassc/sassc #'autoprefixer-hook))
