(ns beetleman.logging.context
  (:refer-clojure :exclude [apply])
  (:import (org.slf4j MDC)))

(def ^:dynamic *current* nil)

(defn- push-context
  [k v]
  (MDC/put (name k) (str v)))

(defn- set-context
  [ctx]
  (MDC/setContextMap ctx))

(defn- get-context
  []
  (MDC/getCopyOfContextMap))

(defn- into-context [ctx]
  (doseq [n ctx]
    (push-context (key n) (val n))))

(defn with-context-fn [ctx f]
  (if-not (map? ctx)
    (throw (ex-info "with-context expects a map" {:ctx ctx}))
    (let [copy (get-context)]
      (when (not= copy *current*)
        (into-context *current*))
      (try
        (into-context ctx)
        (binding [*current* (get-context)]
          (f))
        (finally
          (set-context (or copy {})))))))

(defmacro apply
  [ctx & body]
  `(with-context-fn ~ctx (fn [] ~@body)))

(defmacro restore
  [& body]
  `(with-context-fn {} (fn [] ~@body)))
