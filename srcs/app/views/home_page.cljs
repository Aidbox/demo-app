(ns app.views.home-page
  (:require [app.layout :refer [layout]]))

(defn $home-page []
  (layout
    [:div
     [:div#page-title [:h2 "Dashboard"] ]
     [:p "Some page content"]]))
