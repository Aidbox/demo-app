(ns app.fhir
  (:require-macros [cljs.core.async.macros :as m :refer [go alt!]])
  (:require [app.http :as http]))

(defn mk-ref [{rt :resourceType id :id :as res}] (str "fhir/" rt "/" id ))

(defn value-set [scope id]
  (go
    (let [res (<! (http/GET {:url (str "fhir/ValueSet/" id "/$expand" ) }))]
      (when (:success res)
        (reset! scope (:body res))))) )



(defn delete [& [res cb]]
  (go
    (let [res (<! (http/DELETE {:url  (mk-ref res)}))]
      (when (and (:success res) cb) (cb)))))

