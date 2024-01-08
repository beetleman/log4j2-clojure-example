(ns beetleman.logging.slf4j
  (:import (org.slf4j MDC)))

;; https://github.com/pyr/unilog/blob/master/src/unilog/context.clj

(defn push-context
  "Add a key to the current Mapped Diagnostic Context"
  [k v]
  (MDC/put (name k) (str v)))

(defn pull-context
  "Remove a key to the current Mapped Diagnostic Context"
  [k]
  (MDC/remove (name k)))

(defn set-context
  "Sets the current Mapped Diagnostic Context"
  [ctx]
  (MDC/setContextMap ctx))

(defmacro with-context
  "Execute body with the Mapped Diagnostic Context updated from
   keys found in the ctx map."
  [ctx & body]
  `(if-not (map? ~ctx)
     (throw (ex-info "with-context expects a map" {}))
     (let [copy# (MDC/getCopyOfContextMap)]
       (try
         (doseq [[k# v#] ~ctx]
           (push-context k# v#))
         ~@body
         (finally
           (set-context (or copy# {})))))))
