(ns scratch
  (:require [beetleman.logging :as b.log]
            [clojure.core.async :as a]
            [clojure.tools.logging :as log]))

(log/info "hello")

(b.log/with-context {:x 22}
  (future
    (b.log/with-context {:y 11}
      (future
        (a/go
          (a/<! (a/timeout 200))

          (log/info "hello from nested threads and async")

          )))))
