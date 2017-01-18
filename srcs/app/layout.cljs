(ns app.layout
  (:require [app.styles :as s :refer [$style]]))

(def header
  [:header#page-header.bg-gradient-9
   ($style [[:#header-logo
             [:a.logo-content-big {:$color :white :text-indent 0}]
             [:a.logo-content-big:hover {:$color :white :text-indent 0}]]
            [:#header-nav-right [:.glyph-icon {:margin-top "10px"}] ]
            ])
   [:div#header-logo.logo-bg
     [:a.logo-content-big {:href "#/" :title "Aidbox demo app"} "Aidbox demo app " ]]
   [:div#header-nav-left "Breadcrumbs and headers"]
   [:div#header-nav-right
     [:a.header-btn {:href "#/login" :title "Sign in"}
      [:i.fa.fa-sign-in.glyph-icon]]] ])

(def sidebar
   [:aside#page-sidebar
	  [:div.scroll-sidebar
     [:ul#sidebar-menu
      [:li.header "FHIR"]
      [:li [:a.sf-with-ul {:href "#/patients" :title "Patients"}
            [:i.glyph-icon.fa.fa-user-plus]
            [:span "Patients"]]]
      ]]])

(def footer
   [:footer#page-footer ])

(defn layout [content]
  [:div#page-wrapper
   ($style (s/default-style))
   header
   sidebar
   [:div#page-content-wrapper
    [:div#page-content
     [:div.container content]]]
   footer
   ])

(defn login [content]
  [:div.center-vertical
   ($style (s/default-style))
   [:div.center-content.row
    [:div.col-md-4.col-sm-5.col-xs-11.col-lg-3.center-margin
      content] ] ])
