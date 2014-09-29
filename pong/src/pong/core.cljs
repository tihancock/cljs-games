(ns pong.core
  (:require [monet.canvas :as canvas]
            [monet.geometry :as geometry]
            [goog.dom :as dom]
            [goog.events :as events]))

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

(def monet-canvas (canvas/init canvas-dom "2d"))

(def canvas-width (.-width canvas-dom))
(def canvas-height (.-height canvas-dom))

(def background (canvas/entity {:x 0 :y 0 :w canvas-width :h canvas-height}
                               nil
                               (fn [ctx val]
                                 (-> ctx
                                     (canvas/fill-style "black")
                                     (canvas/fill-rect val)))))

(def left-bat (canvas/entity {:x 20 :y (- (/ canvas-height 2) 20) :w 10 :h 40}
                             (fn [{:keys [x y w h]}]
                               (let [not-at-top    (> y 0)
                                     not-at-bottom (< y canvas-height)
                                     new-y (cond
                                            (and (:up-left   @keys-down) not-at-top)    (- y 1)
                                            (and (:down-left @keys-down) not-at-bottom) (+ y 1)
                                            :else                                       y)]
                                 {:x x
                                  :y new-y
                                  :w w
                                  :h h}))
                             (fn [ctx val]
                               (-> ctx
                                   (canvas/fill-style "white")
                                   (canvas/fill-rect val)))))

(def right-bat (canvas/entity {:x (- canvas-width 30) :y (- (/ canvas-height 2) 20) :w 10 :h 40}
                             (fn [{:keys [x y w h]}]
                               (let [not-at-top    (> y 0)
                                     not-at-bottom (< y canvas-height)
                                     new-y (cond
                                            (and (:up-right   @keys-down) not-at-top)    (- y 1)
                                            (and (:down-right @keys-down) not-at-bottom) (+ y 1)
                                            :else                                        y)]
                                 {:x x
                                  :y new-y
                                  :w w
                                  :h h}))
                             (fn [ctx val]
                               (-> ctx
                                   (canvas/fill-style "white")
                                   (canvas/fill-rect val)))))

(def ball (canvas/entity {:x 0 :y 0 :w 10 :h 10 :horizontal 1 :vertical 1}
                         (fn [{:keys [x y w h horizontal vertical] :as val}]
                           (let [new-horizontal (cond
                                                 (geometry/collision? (canvas/get-entity monet-canvas :right-bat) val) (* -1 horizontal)
                                                 (geometry/collision? (canvas/get-entity monet-canvas :left-bat)  val) (* -1 horizontal)
                                                 (>= x canvas-width)                  -1
                                                 (<= x 0)                             1
                                                 :else                                horizontal)
                                 new-vertical (cond
                                                 (>= y canvas-height) -1
                                                 (<= y 0)             1
                                                 :else                vertical)]
                             {:x (+ x new-horizontal)
                              :y (+ y new-vertical)
                              :w w
                              :h h
                              :horizontal new-horizontal
                              :vertical new-vertical}))
                         (fn [ctx val]
                           (-> ctx
                               (canvas/fill-style "white")
                               (canvas/fill-rect val)))))

(canvas/add-entity monet-canvas :background background)
(canvas/add-entity monet-canvas :ball ball)
(canvas/add-entity monet-canvas :left-bat left-bat)
(canvas/add-entity monet-canvas :right-bat right-bat)
