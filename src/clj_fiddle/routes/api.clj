(ns clj-fiddle.routes.api
  (:use compojure.core)
  (:require [noir.response :refer [json]]
            [clojail.core :refer [sandbox]]
            [clojail.testers :refer [secure-tester-without-def blanket]]
            [clojure.stacktrace :refer [root-cause]])
  (:import java.io.StringWriter
           java.util.concurrent.TimeoutException))

(defroutes api-routes
  (GET "/" [] (json {:ok true}))
  (POST "/eval" [code] (json (with-open [out (StringWriter.)]
                               (try
                                 (let [sbox (sandbox secure-tester-without-def :timeout 2000)
                                       form (binding [*read-eval* false] (read-string code))
                                       result (sbox form {#'*out* out})
                                       response {:result (pr-str result) :stdout (.toString out)}]
                                   response)
                                   (catch TimeoutException _
                                     {:error true :message "Execution Timed Out!"})
                                   (catch Exception e
                                     {:error true :message (str (root-cause e))}))))))
