(ns picture-matching.core
  (:require [reagent.core :as r]))

(def app-state
  (r/atom {:images ["resources/1.jpg"
                    "resources/2.jpg"
                    "resources/3.jpg"
                    "resources/4.jpg"
                    "resources/5.jpg"
                    "resources/6.jpg"
                    "resources/7.jpg"
                    "resources/8.jpg"]}))

(def positions
  [[0 0 25 25]
   [25 0 50 25]
   [50 0 75 25]
   [75 0 100 25]
   [0 25 25 50]
   [25 25 50 50]
   [50 25 75 50]
   [75 25 100 50]
   [0 50 25 75]
   [25 50 50 75]
   [50 50 75 75]
   [75 50 100 75]
   [0 75 25 100]
   [25 75 50 100]
   [50 75 75 100]
   [75 75 100 100]])

(defn double-images [images]
  (concat images images))

(defn image [i l t r b]
  [:div {:style {"position" "absolute"
                 "left" (str l "%")
                 "top" (str t "%")
                 "right" (str r "%")
                 "bottom" (str b "%")}}
   [:img {:src i}]])

(defn image-grid []
  [:div
   [:ul
    (let [randomised-images (-> (:images @app-state)
                                double-images
                                shuffle)]
      (map (fn [i [l t r b]] [image i l t r b]) randomised-images positions))]])

(defn start []
  (r/render-component
   [image-grid]
   (.getElementById js/document "root")))
