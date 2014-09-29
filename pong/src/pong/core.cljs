(ns pong.core
  (:require [monet.canvas :as canvas]
            [monet.geometry :as geometry]
            [goog.dom :as dom]
            [goog.events :as events]))

;; score
(def left-score  (atom 0))
(def right-score (atom 0))

;; events
(def keycode->key
  {65 :up-left
   90 :down-left
   38 :up-right
   40 :down-right})

(def keys-down (atom #{}))

(def w (dom/getWindow))
(events/listen w (.-KEYDOWN events/EventType) (fn [e]
                                                (if-let [key (keycode->key (.-keyCode e))]
                                                  (swap! keys-down conj key))))
(events/listen w (.-KEYUP events/EventType) (fn [e]
                                                (if-let [key (keycode->key (.-keyCode e))]
                                                  (swap! keys-down disj key))))

;; drawing
(def canvas-dom (.getElementById js/document "screen"))

(def canvas-width (.-innerWidth js/window))
(def canvas-height (.-innerHeight js/window))

(set! (.-width canvas-dom) canvas-width)
(set! (.-height canvas-dom) canvas-height)

(def monet-canvas (canvas/init canvas-dom "2d"))

(def background (canvas/entity {:x 0 :y 0 :w canvas-width :h canvas-height}
                               nil
                               (fn [ctx val]
                                 (-> ctx
                                     (canvas/fill-style "black")
                                     (canvas/fill-rect val)))))

(def left-bat (canvas/entity {:x 80 :y (- (/ canvas-height 2) 80) :w 40 :h 160}
                             (fn [{:keys [x y w h]}]
                               (let [not-at-top    (> y 0)
                                     not-at-bottom (< (+ y 160) canvas-height)
                                     new-y (cond
                                            (and (:up-left   @keys-down) not-at-top)    (- y 4)
                                            (and (:down-left @keys-down) not-at-bottom) (+ y 4)
                                            :else                                       y)]
                                 {:x x
                                  :y new-y
                                  :w w
                                  :h h}))
                             (fn [ctx val]
                               (-> ctx
                                   (canvas/fill-style "white")
                                   (canvas/fill-rect val)))))

(def right-bat (canvas/entity {:x (- canvas-width 120) :y (- (/ canvas-height 2) 80) :w 40 :h 160}
                             (fn [{:keys [x y w h]}]
                               (let [not-at-top    (> y 0)
                                     not-at-bottom (< (+ y 160) canvas-height)
                                     new-y (cond
                                            (and (:up-right   @keys-down) not-at-top)    (- y 4)
                                            (and (:down-right @keys-down) not-at-bottom) (+ y 4)
                                            :else                                        y)]
                                 {:x x
                                  :y new-y
                                  :w w
                                  :h h}))
                             (fn [ctx val]
                               (-> ctx
                                   (canvas/fill-style "white")
                                   (canvas/fill-rect val)))))

(defn initial-ball-state
  ([] (initial-ball-state :left))
  ([last-winner]
     (let [direction (if (= last-winner :left) -1 1)]
       {:x (- (/ canvas-width 2) 20) :y (- (/ canvas-height 2) 20) :w 40 :h 40 :horizontal (* direction 4) :vertical 4})))

(def ball (canvas/entity (initial-ball-state)
                         (fn [{:keys [x y w h horizontal vertical] :as val}]
                           (let [new-horizontal (cond
                                                 (geometry/collision? (canvas/get-entity monet-canvas :right-bat) val) (* -1 horizontal)
                                                 (geometry/collision? (canvas/get-entity monet-canvas :left-bat)  val) (* -1 horizontal)
                                                 :else                                                                  horizontal)
                                 new-vertical (cond
                                               (>= (+ y 40) canvas-height) -4
                                               (<= y 0)                     4
                                               :else                        vertical)]
                             (cond 
                              (< x 0)            (do (swap! right-score inc)
                                                     (initial-ball-state :right))
                              (> x canvas-width) (do (swap! left-score inc)
                                                     (initial-ball-state :left))
                              :else              {:x (+ x new-horizontal)
                                                  :y (+ y new-vertical)
                                                  :w w
                                                  :h h
                                                  :horizontal new-horizontal
                                                  :vertical new-vertical})))
                         (fn [ctx val]
                           (-> ctx
                               (canvas/fill-style "white")
                               (canvas/fill-rect val)
                               (canvas/stroke-style "white")
                               (canvas/font-style "30px Arial")
                               (canvas/text {:text (str @left-score "|" @right-score) :x (* 0.5 canvas-width) :y (* 0.1 canvas-height)})))))

(canvas/add-entity monet-canvas :background background)
(canvas/add-entity monet-canvas :ball ball)
(canvas/add-entity monet-canvas :left-bat left-bat)
(canvas/add-entity monet-canvas :right-bat right-bat)
