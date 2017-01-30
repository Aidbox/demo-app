(ns app.oauth
  (:require-macros [cljs.core.async.macros :as m :refer [go alt!]])
  (:require [reagent.session :as session]
            [reagent.cookies :as cookies]
            [app.http :as http]
            [app.utils :as utils :refer [redirect]]
            [cljs.core.async :refer [<!]]
            [app.http :as http]))

(defn logout [] (session/clear!) (cookies/clear!))

(defn get-user []
  (go
    (let [{user :body :as res} (<! (http/GET {:url "user"})) ]
      (when (:success res)
        (cookies/set! :user user)
        (session/reset! {:access-tokens (cookies/get :access_token)
                         :user user })))))

(defn ensure-authorized []
  (if-let [at (cookies/get :access_token)]
    (get-user)
    (redirect "/login") ))


