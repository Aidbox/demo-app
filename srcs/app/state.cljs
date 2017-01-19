(ns app.state
  (:require [reagent.core :as r :refer [atom]]))

(defonce state (atom {}))
