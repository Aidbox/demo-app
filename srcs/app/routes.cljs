(ns app.routes
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [app.layout :refer [layout]]

            [app.views.home-page :as hp]
            [app.views.users :as u]
            [app.views.patients :as pt]


            ))

(secretary/set-config! :prefix "#")

(defroute "/" [] (hp/home-page))
(defroute "/users"  [] (u/users))
(defroute "/user/:id" [id] (u/user id))
(defroute "/patients"  [] (pt/patients))
(defroute "/patient/:action/:id" {:as params} (pt/patient params))

;; Catch all
(defroute "*" [] (layout [:h1 "404 Page not found"]))

