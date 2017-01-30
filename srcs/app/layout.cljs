(ns app.layout
  (:require [app.styles :as s :refer [$style]]
            [app.oauth :as oauth]
            [reagent.session :as session]
            [reagent.core :as r :refer [atom]]
            [reagent.session :as session]))

(session/put! :toggle-profile :none)

(defn toggle-profile []
  (js/console.log (session/get :toggle-profile))
  (if (= (session/get :toggle-profile) :block)
    (session/put! :toggle-profile :none)
    (session/put! :toggle-profile :block)))

(defn close-profile [e]
  (if (= (session/get :toggle-profile) :block)
    (do
     (session/put! :toggle-profile :none))))

(defn logout []
  (oauth/logout)
  (toggle-profile))


(defn profile-dropdown []
  (fn []
    (let [display (or (session/get :toggle-profile) :none)]
      [:div.dropdown-menu.float-left {:style {:display display}}
          [:div.box-sm
           [:div.login-box.clearfix
            [:div.user-img
             [:a.change-img  {:title "", :href "#"} "Change photo"]
             [:img {:alt "" :src "/img/user.png"}]]
            [:div.user-info
             [:span "Thomas Barnes" [:i "UX/UI developer"]]
             [:a  {:title "Edit profile", :href "#"} "Edit profile"]
             [:a {:title "View notifications", :href "#"} "View notifications"]]
            [:div.divider]
            [:ul.reset-ul.mrg5B
             [:li [:a {:href "#/profile"} [:i.glyph-icon.float-right.icon-caret-right] "Profile"]]
             [:li [:a {:href "#"} [:i.glyph-icon.float-right.icon-caret-right] "View lockscreen example"]]
             [:li [:a {:href "#"} [:i.glyph-icon.float-right.icon-caret-right] "View account details"]]]
            [:div.pad5A.button-pane-alt.text-center
             [:a.btn.display-block.font-normal.btn-danger {:href "javascript:void(0)" :on-click logout}
              [:i.glyph-icon.icon-power-off] " Logout"]]]]])))

(def header
  (fn []
    [:header#page-header.bg-gradient-9
     ($style [[:#header-logo
               [:a.logo-content-big {:$color :white :text-indent 0}]
               [:a.logo-content-big:hover {:$color :white :text-indent 0}]]
              [:#header-nav-right [:.glyph-icon {:margin-top "10px"}] ]
              [:.user-info {:height "80px"}]
              ])
     [:div#header-logo.logo-bg
      [:a.logo-content-big {:href "#/" :title "Aidbox demo app"}
       [:img {:src "/img/logo-icon.png"}] ]]

     [:div#header-nav-left
      (if (session/get :user)
        [:div.user-account-btn.dropdown.open
         [:a.div.user-profile.clearfix {:href "javascript:void(0)" :title "Profile" :on-click toggle-profile}
          [:img {:width "28" :src "/img/user.png" :alt "My account"}]
          [:span (session/get-in [:user :data :name])]
          [:i.glyph-icon.icon-angle-down] ]
         [ profile-dropdown]

         ])]

     [:div#header-nav-right
      (if (not (session/get :user))
        [:a.header-btn {:href "#/login" :title "Sign in"}
         [:i.fa.fa-sign-in.glyph-icon]])] ]))

(def sidebar
  [:aside#page-sidebar
   [:div.scroll-sidebar
    [:ul#sidebar-menu
     [:li.header "FHIR"]
     [:li [:a {:href "#/patients" :title "Patients"}
           [:i.glyph-icon.fa.fa-user-plus]
           [:span "Patients"]]]
     ]]])

(def footer
  [:footer#page-footer ])

(defn layout [content]
  [:div#page-wrapper 
   ($style (s/default-style))
   [header]
   sidebar
   [:div#page-content-wrapper
    [:div#page-content
     [:div.container content]
     [:div.fix] ]]
   footer ])

(defn login [content]
  [:div.center-vertical
   ($style (s/default-style))
   [:div.center-content.row
    [:div.col-md-4.col-sm-5.col-xs-11.col-lg-3.center-margin
     content] ] ])
