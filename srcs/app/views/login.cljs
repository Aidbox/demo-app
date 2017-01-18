(ns app.views.login
  (:require [app.layout :as l]))

(defn login []
  (l/login
    [:div
     [:h3.text-center.pad25B.font-gray.text-transform-upr.font-size-23 "Aidbox sample app" ]
     [:div.content-box.bg-default
      [:div.content-box-wrapper.pad20A

       ]]]
    )
  )
