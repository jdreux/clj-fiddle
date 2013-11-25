(ns clj-fiddle.routes.api
  (:use compojure.core)
  (:require [noir.response :refer [json]]
            [clojail.core :refer [sandbox]]
            [clojail.testers :refer [secure-tester-without-def blanket]]
            [clojure.stacktrace :refer [root-cause]])
  (:import java.io.StringWriter
           java.util.concurrent.TimeoutException))



(defn eval-form [form sbox]
  (with-open [out (StringWriter.)]
    (let [result (sbox form {#'*out* out})]
      {:expr form
       :result [out result]})))

(defn eval-string [expr sbox]
  (println "evaling: " expr)
  (let [form (binding [*read-eval* false] (read-string expr))]
    (eval-form form sbox)))

(def try-clojure-tester
  (conj secure-tester-without-def (blanket "tryclojure" "noir")))

(defn make-sandbox []
  (sandbox try-clojure-tester
           :timeout 2000
           :init '(do (require '[clojure.repl :refer [doc source]])
                      (future (Thread/sleep 600000)
                              (-> *ns* .getName remove-ns)))))

(defroutes api-routes
  (GET "/" [] (json {:ok true}))
  (POST "/eval" [code] (json (with-open [out (StringWriter.)]
                               (println "Evaling" code)
                               (try
                                 (let [sbox (make-sandbox);(sandbox secure-tester-without-def)
                                       ;form (binding [*read-eval* false] (read-string code))
                                       ;result (sbox form)
                                       result (eval-string code sbox)]
                                       ;result (sbox form {#'*out* out})]
                                   (println "got results" out result)
;                                   {:result [out (pr-str result)] :error false})
                                   {:result (pr-str result)})
                                   (catch TimeoutException _
                                     {:error true :message "Execution Timed Out!"})
                                   (catch Exception e
                                     (println e)
                                     {:error true :message (str (root-cause e))}))))))
