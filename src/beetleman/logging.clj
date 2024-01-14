(ns beetleman.logging
  (:require [beetleman.logging.context :as context]
            [clojure.tools.logging :as log]
            [clojure.tools.logging.impl :as log.impl]))

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
        (reify log.impl/Logger
          (enabled? [_ level]
            (log.impl/enabled? logger level))
          (write! [_ level throwable message]
            (context/restore
              (log.impl/write! logger level throwable message))))))))

(defn- wrap-fn [f]
  (let [factory log/*logger-factory*]
    (if (wraped? factory)
      (f)
      (binding [log/*logger-factory* (wrap factory)]
        (f)))))

(defn with-context-fn [ctx f]
  (context/apply ctx
    (wrap-fn f)))

(defmacro with-context [ctx & body]
  `(with-context-fn ~ctx (fn [] ~@body)))
