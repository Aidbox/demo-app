(ns app.views.home-page
  (:require [app.layout :refer [layout]]
            [app.styles :as s :refer [$style]]
            ))

(defn status [st]
  (cond
    (= :in-progress st)
    [:div.bs-label.bg-yellow "In progress"]
    (= :done st)
    [:div.bs-label.bg-green "Done"]
    :else
    [:div.bs-label.bg-red "Not ready"]))

(defn block [{href :href title :title desc :desc ico :ico st :status}]
  [:div.col-md-4
   [:div.dashboard-box.bg-white.content-box.dashboard-box-chart
    [:div.content-wrapper
     [:div.header
      [:a.nohover {:href href :title title}
       [:i.fa {:class ico}] (str " " title)]]
     (status st) ]
    [:div.button-pane
     [:div.size-md.float-left desc]] ]])

(defn $home-page []
  (layout
    [:div
     ($style
       [[:.nohover:hover {:text-decoration :none}]])
     [:div#page-title [:h2 "Dashboard"] ]
     [:div.row

      (block {:href "#/patients" :title "Patients" :status :done
              :ico "fa-user-plus" :desc "Patients managemt" })

      (block {:href "#/medications" :title "Medication" :status :in-progress
              :ico "fa-medkit" :desc "Medication managemt" })

      (block {:href "#/practitioners" :title "Practitioners"
              :ico "fa-user-md" :desc "Practitioners managemt" })

      
      ]
[:div.row

      (block {:href "#/terminology" :title "Terminology"
              :ico "fa-book" :desc "Terminology" })

      (block {:href "#/chat" :title "Chat"
              :ico "fa-comments-o" :desc "Chat" })

      (block {:href "#/users" :title "Users"
              :ico "fa-users" :desc "Users managemt" })]

     ]))
