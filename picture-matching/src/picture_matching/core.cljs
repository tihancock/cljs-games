(ns picture-matching.core
  (:require [reagent.core :as r]))

(def images [{:id 1  :path "resources/1.jpg" :displayed true}
             {:id 2  :path "resources/1.jpg" :displayed true}
             {:id 3  :path "resources/2.jpg" :displayed true}
             {:id 4  :path "resources/2.jpg" :displayed true}
             {:id 5  :path "resources/3.jpg" :displayed true}
             {:id 6  :path "resources/3.jpg" :displayed true}
             {:id 7  :path "resources/4.jpg" :displayed true}
             {:id 8  :path "resources/4.jpg" :displayed true}
             {:id 9  :path "resources/5.jpg" :displayed true}
             {:id 10 :path "resources/5.jpg" :displayed true}
             {:id 11 :path "resources/6.jpg" :displayed true}
             {:id 12 :path "resources/6.jpg" :displayed true}
             {:id 13 :path "resources/7.jpg" :displayed true}
             {:id 14 :path "resources/7.jpg" :displayed true}
             {:id 15 :path "resources/8.jpg" :displayed true}
             {:id 16 :path "resources/8.jpg" :displayed true}])

(def app-state
  (r/atom {:images          (shuffle images)
           :current-attempt nil}))

(defn toggle-displayed
  [image]
  (assoc image :displayed (not (:displayed image))))

(defn update-images!
  [f]
  (swap! app-state (fn [state] (assoc state :images (mapv f (:images state))))))

(defn update-current-attempt!
  [ca]
  (swap! app-state #(assoc % :current-attempt ca)))

(defn image [i]
  [:img {:on-click (fn []
                     (update-images! (fn [image] (if (= (:id i) (:id image))
                                                   (toggle-displayed image)
                                                   image)))
                     (if (:current-attempt @app-state)
                       (if (not= (:path (:current-attempt @app-state)) (:path i))
                         (js/setTimeout (fn [] (update-images! (fn [image] (if (or (= (:id i) (:id image))
                                                                                    (= (:id (:current-attempt @app-state)) (:id image)))
                                                                             (toggle-displayed image)
                                                                             image)))
                                          (update-current-attempt! nil)) 1000)
                         (update-current-attempt! nil))
                       (update-current-attempt! i)))
         :style {"width" "25%"
                 "height" "25%"
                 "opacity" (if (:displayed i) 1.0 0.0)}
         :src (:path i)}])

(defn timer []
  (fn []
    (js/setTimeout (fn [] (update-images! #(assoc % :displayed false))) 2000)
    [:div]))

(defn image-grid []
  [:div
    [:ul
     (for [image-row (partition 4 (:images @app-state))]
       [:div {:style {"background" "black"}}
        (map (fn [i] [image i]) image-row)])]
   [timer]])

(defn start []
  (r/render-component
   [image-grid]
   (.getElementById js/document "root")))
