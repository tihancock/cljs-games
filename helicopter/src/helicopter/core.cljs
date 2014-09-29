(ns helicopter.core
  (:require [monet.canvas :as canvas]
            [monet.geometry :as geometry]
            [goog.dom :as dom]
            [goog.events :as events]))

(def score (atom 0))

;; events
(def keycode->key
  {32 :space})

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

(def helicopter (let [initial-state {:x (- (/ canvas-width 2) 20) :y (- (/ canvas-height 2) 20) :w 40 :h 40}]
                  (canvas/entity 
                   initial-state
                   (fn [{:keys [x y w h] :as val}]
                     (if (or 
                          (geometry/collision? 
                           (canvas/get-entity monet-canvas :bar) val)
                          (< y 0)
                          (> y canvas-height)) 
                       (do (swap! score (constantly 0))
                           initial-state)
                       (do (swap! score inc)
                           (let [direction (if (:space @keys-down) -8 8)] 
                             {:x x
                              :y (+ y direction)
                              :w w
                              :h h}))))
                   (fn [ctx val]
                     (-> ctx
                         (canvas/fill-style "white")
                         (canvas/fill-rect val)
                         (canvas/stroke-style "white")
                         (canvas/font-style "30px Arial")
                         (canvas/text {:text @score :x (* 0.5 canvas-width) :y (* 0.1 canvas-height)}))))))

(defn create-bar []
  (let [height (* 0.9 (.random js/Math) canvas-height)
        y      (if (> 0.5 (.random js/Math))
                 (- canvas-height height)
                 0)]
    {:x (- canvas-width 40) :y y :w 40 :h height}))

(def bar (canvas/entity 
          (create-bar)
          (fn [{:keys [x y w h] :as val}]
            (if (<= x 0) 
              (create-bar)
              {:x (- x 20) :y y :w w :h h}))
          (fn [ctx val]
            (-> ctx
                (canvas/fill-style "white")
                (canvas/fill-rect val)))))

(canvas/add-entity monet-canvas :background background)
(canvas/add-entity monet-canvas :helicopter helicopter)
(canvas/add-entity monet-canvas :bar bar)
