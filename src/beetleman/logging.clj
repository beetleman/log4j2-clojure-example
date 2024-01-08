(ns beetleman.logging
  (:require [beetleman.logging.slf4j :as slf4j]
            [clojure.tools.logging :as log]
            [clojure.tools.logging.impl :as log.impl]))

(def ^:dynamic *context* nil)

(defprotocol Wrapped
  (wraped? [_]))

(extend-protocol Wrapped
  Object
  (wraped? [_] false))

(defn- wrap [factory]
  ;; some caching or performance improvement can be added here
  (reify
    Wrapped
    (wraped? [_] true)

    log.impl/LoggerFactory
    (name [_]
      (log.impl/name factory))
    (get-logger [_ logger-ns]
      (let [logger (log.impl/get-logger factory logger-ns)]
        (if *context*
          (reify log.impl/Logger
            (enabled? [_ level]
              (log.impl/enabled? logger level))
            (write! [_ level throwable message]
              (slf4j/with-context *context*
                (log.impl/write! logger level throwable message))))
          logger)))))

(defn- wrap-fn [f]
  (let [factory log/*logger-factory*]
    (if (wraped? factory)
      (f)
      (binding [log/*logger-factory* (wrap factory)]
        (f)))))

(defn with-context-fn [ctx f]
  (binding [*context* (merge *context* ctx)]
    (wrap-fn f)))

(defmacro with-context [ctx & body]
  `(with-context-fn ~ctx (fn [] ~@body)))
