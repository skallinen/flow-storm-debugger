(ns flow-storm.runtime.types.fn-return-trace
  (:require [flow-storm.runtime.indexes.protocols :as index-protos]
            [flow-storm.utils :as utils]))

(def nil-idx -1)

(defprotocol FnReturnTraceP
  (set-fn-call-idx [_ idx])
  (set-idx [_ idx]))

(deftype FnReturnTrace
    [                              coord
                                   retVal
     ^:unsynchronized-mutable ^int fnCallIdx
     ^:unsynchronized-mutable ^int thisIdx]

  index-protos/ExpressionTimelineEntryP
  (get-expr-val [_] retVal)
  
  FnReturnTraceP
    
  (set-fn-call-idx [_ idx]
    (set! fnCallIdx (int idx)))
  (set-idx [_ idx]
    (set! thisIdx (int idx)))

  index-protos/CoordableTimelineEntryP
  (get-coord-vec [_] (utils/str-coord->vec coord))
  (get-coord-raw [_] coord)
  
  index-protos/TimelineEntryP

  (entry-type [_] :fn-return)
  (entry-idx [_]
    (when (not= thisIdx nil-idx)
      thisIdx))
  (fn-call-idx [_]
    (when (not= fnCallIdx nil-idx)
      fnCallIdx))

  
  index-protos/ImmutableP
  
  (as-immutable [this]
    {:type :fn-return
     :coord (index-protos/get-coord-vec this)
     :result (index-protos/get-expr-val this)
     :fn-call-idx (index-protos/fn-call-idx this)
     :idx (index-protos/entry-idx this)})
  
  #?@(:clj
      [Object
       (toString [_] (utils/format "[%d FnReturnTrace] retValType: %s" thisIdx (type retVal)))])
  )

(defn make-fn-return-trace [coord ret-val]
  (->FnReturnTrace coord ret-val nil-idx nil-idx))

(defn fn-return-trace? [x]
  (and x (instance? FnReturnTrace x)))

(deftype FnUnwindTrace
    [                              coord
                                   throwable
     ^:unsynchronized-mutable ^int fnCallIdx
     ^:unsynchronized-mutable ^int thisIdx]

  FnReturnTraceP
    
  (set-fn-call-idx [_ idx]
    (set! fnCallIdx (int idx)))
  (set-idx [_ idx]
    (set! thisIdx (int idx)))

  index-protos/CoordableTimelineEntryP
  (get-coord-vec [_] (utils/str-coord->vec coord))
  (get-coord-raw [_] coord)

  index-protos/UnwindTimelineEntryP
  (get-throwable [_]
    throwable)
  
  index-protos/TimelineEntryP

  (entry-type [_] :fn-unwind)
  (entry-idx [_]
    (when (not= thisIdx nil-idx)
      thisIdx))
  (fn-call-idx [_]
    (when (not= fnCallIdx nil-idx)
      fnCallIdx))

  
  index-protos/ImmutableP
  
  (as-immutable [this]
    {:type :fn-unwind
     :coord (index-protos/get-coord-vec this)
     :throwable (index-protos/get-throwable this)
     :fn-call-idx (index-protos/fn-call-idx this)
     :idx (index-protos/entry-idx this)})
  
  #?@(:clj
      [Object
       (toString [_] (utils/format "[%d FnUnwindTrace] throwable: %s" thisIdx (type throwable)))])
  )

(defn make-fn-unwind-trace [coord throwable]
  (->FnUnwindTrace coord throwable nil-idx nil-idx))

(defn fn-unwind-trace? [x]
  (and x (instance? FnUnwindTrace x)))

(defn fn-end-trace? [x]
  (or (fn-return-trace? x)
      (fn-unwind-trace? x)))
