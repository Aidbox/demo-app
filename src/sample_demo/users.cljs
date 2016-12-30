(ns sample-demo.users
  (:require [sample-demo.layout :refer [layout]])
  )

(defn users []
  (layout
    [:div#users "USERS" ]))

(defn user [id]
  (layout
    [:div#users "USER with id " id ]))
