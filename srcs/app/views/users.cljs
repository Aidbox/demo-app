(ns app.views.users
  (:require [app.layout :refer [layout]]))

(defn users []
  (layout
    [:div#users "USERS" ]))

(defn user [id]
  (layout
    [:div#users "USER with id " id ]))
