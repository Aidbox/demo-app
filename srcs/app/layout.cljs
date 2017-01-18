(ns app.layout
  (:require [app.styles :as s :refer [$style]]))

(def header
  [:header#page-header.bg-gradient-9
   ($style [[:#header-logo
             [:a.logo-content-big {:$color :white :text-indent 0}]
             [:a.logo-content-big:hover {:$color :white :text-indent 0}]
             ]])
   [:div#header-logo.logo-bg
     [:a.logo-content-big {:href "#/" :title "Aidbox demo app"} "Aidbox demo app " ]]
   [:div#header-nav-left "Breadcrumbs and headers"]
   [:div#header-nav-right "User info and settings"] ])

(def sidebar
   [:aside#page-sidebar
	  [:div.scroll-sidebar
     [:ul#sidebar-menu
      [:li.header "OVERVIEW"]
      #_[:li [:a.sfActive {:href "#/"}
            [:i.glyph-icon.fa.fa-tachometer]
            [:span "Dashboard"]]]
      [:li [:a.sf-with-ul {:href "#/patients"}
            [:i.glyph-icon.fa.fa-user-plus]
            [:span "Patients"]]]
      [:li [:a.sf-with-ul {:href "#/medicaments"}
            [:i.glyph-icon.fa.fa-linode]
            [:span "Medicaments"]]]
      ]]])

(def footer
   [:footer#page-footer ])

(defn layout [content]
  [:div#page-wrapper
   ($style s/default-style)
   header
   sidebar
   [:div#page-content-wrapper
    [:div#page-content
     [:div.container content]]]
   footer
   ])
