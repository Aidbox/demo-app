(ns app.config
  (:require [reagent.core :as reagent :refer [atom]]))

(defonce config
  (atom
    {:app-name "Sample demo"
     :box-url  "http://demo.miac.local/" }))

