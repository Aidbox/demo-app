(ns app.views.login
  (:require-macros [cljs.core.async.macros :as m :refer [go alt!]])
  (:require [app.layout :as l]
            [app.http :as http]
            [reagent.session :as session]
            [reagent.cookies :as cookies]
            [reagent.core :as r :refer [atom]]
            [app.styles :as s :refer [$style]]
            [app.chloroform :as f :refer [form]])
  (:import goog.History
           goog.history.EventType))


(def history (History.))

(defn redirect [path]
   (. history (setToken path)))

(defn logout [] (session/clear!) (cookies/clear!))

(defn login [{r :body :as res}]
  (logout)
  (session/reset! r)
  (js/console.log r)
  (cookies/set! :access_token (:access_token r))
  (cookies/set! :user (:user r))
  (redirect "/"))

(defn *login [f]
  (go
    (let [res (<! (http/POST {:url "signin" :data f})) ]
      (when (:success res)
        (login res)))))

(defn login-form []
  (let [f (atom {})]
    (fn []
      [form f {:id "login-form" :class "form"
               :on-submit *login }
       ($style [[:#login-form [:.pass [:.input-group-addon {:width "39px"}]]]])
       [:div.form-group
        [:div.input-group
         [:span.input-group-addon
          [:i.fa.fa-envelope-o]]
         [:$input {:name :email :as :email :required true :placeholder "Email"}]]]
       [:div.form-group.pass
        [:div.input-group
         [:span.input-group-addon
          [:i.fa.fa-unlock-alt]]
         [:$input {:name :password :as :password :required true :placeholder "Password"}]]]
       [:button.btn.btn-block.btn-primary {:type :submit} "Login"] ])))

(defn $login []
  (l/login
    [:div
     [:h3.text-center.pad25B.font-gray.text-transform-upr.font-size-23 "Aidbox sample app" ]
     [:div.content-box.bg-default
      [:div.content-box-wrapper.pad20A
       [login-form] ]]]
    )
  )
