(ns app.core
  (:require [reagent.core :as reagent :refer [atom]]
            [app.routes]
            [goog.events]
            [app.oauth :as oauth]
            [secretary.core :as secretary])
  (:import goog.History
           goog.history.EventType))

(def application (js/document.getElementById "app"))

(defn render [path]
  (reagent/render-component [#(secretary/dispatch! (.-token path))] application))

(defn path-change [path]
  (oauth/ensure-authorized)
  (render path))

(let [h (History.)]
  (doto h
    (goog.events/listen EventType.NAVIGATE path-change)
    (.setEnabled true)))

