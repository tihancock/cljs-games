(ns pong.core
  (:require [monet.canvas :as canvas]))

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

(def ball (canvas/entity {:x 0 :y 0 :w 10 :h 10 :horizontal 1 :vertical 1}
                         (fn [{:keys [x y w h horizontal vertical]}]
                           (let [new-horizontal (cond
                                                 (>= x canvas-width) -1
                                                 (<= x 0)            1
                                                 :else               horizontal)
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
(canvas/add-entity monet-canvas :foreground ball)
