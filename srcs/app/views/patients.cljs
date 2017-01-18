(ns app.views.patients
  (:require-macros [cljs.core.async.macros :as m :refer [go alt!]])
  (:require [app.layout :refer [layout]]
            [app.http :as http]
            [app.fhir :as fhir]
            [app.chloroform :as f :refer [form]]
            [reagent.core :as r :refer [atom]]
            [cljs.core.async :refer [<!]]
            [app.styles :as s :refer [$style]]))

(defonce pts (atom []))

(defn- *patients [scope]
  (go
    (let [result (<! (http/GET {:url "fhir/Patient" }))]
      (when (:success result)
        (reset! scope (mapv :resource (get-in result [:body :entry])))))))

(defn- *patient [scope id]
  (go
    (let [result (<! (http/GET {:url (str "fhir/Patient/" id) }))]
      (when (:success result)
        (swap! scope assoc :data (:body result))))))

(defn create [scope] (fhir/post scope))
(defn save  [scope] (fhir/put scope))

(defn delete [p]
  (if (js/confirm (str "Delete patient " (:id p) "?"))
    (fhir/delete p (fn []  (*patients pts)))))

(defn pt [p]
  [:tr
   [:td (str (get-in p [:name 0 :family 0]) " " (get-in p [:name 0 :given 0]))]
   [:td (:gender p)]
   [:td (:birthDate p)]
   [:td
    [:a.btn.btn-default.btn-sm.mrg5A {:href (str "#/patient/show/" (:id p)) :title "Edit"} [:i.fa.fa-edit.glyph-icon]  ]
    [:button.btn.btn-danger.btn-sm.mrg5A {:title "Delete patient"
                                    :on-click #(delete p) } [:i.fa.fa-trash.glyph-icon]] ]
   ])

(defn patients []
  (*patients pts)
  (fn []
    (layout
      [:div
       ($style
         [[:.flex-row {:$flex-row []}]])
       [:div.flex-row
        [:div#page-title
         [:h2 "Patients"]
         [:p "Patients list"] ]
        [:a.btn.btn-primary {:href "#/patient/new"} "Create"]]
       #_[:div.panel
          [:div.panel-body
           [:form.form
            [:input.form-control {:placeholder "Search"}] ]
           ]]
       [:div.panel
        [:div.panel-body
         (if (empty? @pts)
           [:div.text-center
            [:h4.mrg10A "No patients"]
            [:a.btn.btn-primary {:href "#/patient/new"} "Create new patient"]
            ]
           [:table.table.table-hover
            [:thead
             [:tr
              [:th "Name"] [:th "Gender"] [:th "Birthdate"] [:th "Action"]] ]
            [:tbody.tbody
             (for [p @pts] ^{:key (:id p)} [pt p])]]
           )]]])))

(defn patient-form [f action]
  (let [genders (atom {})
        submit (if (= action :new) create save ) ]
    (fhir/value-set genders "administrative-gender")
    (fn []
      [form f
       {:class "form-horizontal"
        :on-submit submit }
       [:div.pt-form
        [:$row  {:name :name :label "Patient name" :as :human-name
                 :$required true
                 :fields {:given {:label "Given"}
                          :family {:label "Family"} }}]

        [:$row  {:name :gender :label "Gender" :as :select
                 :value-fn :code :text-fn :display
                 :collection (get-in @genders [:expansion :contains]) }]

        [:$row  {:name :birthDate
                 :label "Birth date"
                 :as :date }]
        [:button.btn.btn-primary {:type "submit"} "Create"]
        ]])))

(def empty-patient
  {:resourceType "Patient"
   :name [{:given [""] :family [""]}]})

(defn new-patient []
  (let [f (atom {:data empty-patient})]
    (fn []
      (layout
        [:div
         [:div#page-title
          [:h2 "Create patient"] ]
         [:div.panel
          [:div.panel-body
           [patient-form f :new]
           ]]]))))

(defn show-patient [id]
  (let [f (atom {:data empty-patient}) ]
    (*patient f id)
    (fn []
      (layout
        [:div
         [:div#page-title
          [:h2 "Patient " id] ]
         [:div.panel
          [:div.panel-body
           [patient-form f :show]]]]))))

(defn patient [{id :id action :action :as params}]
  (js/console.log params)
  (cond
    (= action "new")
      (new-patient)
    (= action "show")
      (show-patient id)
    :else
      (new-patient)) )
