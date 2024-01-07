(ns scratch
  (:require [clojure.tools.logging :as log]))

(log/info "hello")
(log/error (ex-info "Foo" {}) "hello")

(clojure.tools.logging.impl/name clojure.tools.logging/*logger-factory*)
