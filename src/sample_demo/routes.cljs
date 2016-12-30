(ns sample-demo.routes
  (:require [secretary.core :as secretary :refer-macros [defroute]]

            [sample-demo.home-page :as hp]
            [sample-demo.users :as u]

))

(secretary/set-config! :prefix "#")


(defroute "/" [] (hp/home-page))
(defroute "/users"  [] (u/users))
(defroute "/user/:id" [id] (u/user id))

;; Catch all
(defroute "*" [] [:h1 "Page not found"])

