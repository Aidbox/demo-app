(ns sample-demo.core
  (:require [reagent.core :as reagent :refer [atom]]
            [sample-demo.routes]
            [goog.events]
            [secretary.core :as secretary])
  (:import goog.History
           goog.history.EventType))

(def application
  (js/document.getElementById "app"))

(defonce app-state
  (atom
    {:app-name "Sample demo"}))


(defn render [path]
  (reagent/render-component [#(secretary/dispatch! (.-token path))] application))

(let [h (History.)]
  (doto h
    (goog.events/listen EventType.NAVIGATE render)
    (.setEnabled true)))

