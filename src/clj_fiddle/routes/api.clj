(ns clj-fiddle.routes.api
  (:use compojure.core)
  (:require [noir.response :refer [json]]))

(defroutes api-routes
  (GET "/" [] (json {:ok true}))
  (POST "/eval" [code] (json {:result (pr-str (eval (read-string code)))})))
