(ns fakeflix-datomic.config.schema
  (:require [schema.core :as s]))

(def core-values ["String" 1 1.0 #uuid "186579ce-7224-4d7f-8bd0-4280044f6296" true])

(s/defn schema-type->core-value :- s/Any
  [schema-type :- s/Any]
  (let [checker (schema.core/checker schema-type)
        test-values (map-indexed (fn [index value]
                                   (let [result (checker value)]
                                     (if (nil? result)
                                       index
                                       nil))) core-values)
        index-of-successful-value (-> (filter #(not (nil? %)) test-values)
                                      first)]
    (get core-values index-of-successful-value)))

(s/defn schema-type->value-type :- s/Keyword
  [schema-type :- s/Any]
  (let [core-value (schema-type->core-value schema-type)]
    (cond
      (string? core-value) :db.type/string
      (int? core-value) :db.type/long
      (float? core-value) :db.type/float
      (uuid? core-value) :db.type/uuid
      (boolean? core-value) :db.type/boolean
      :else :db.type/string)))

(s/defn skeleton-attribute->entity-attribute
  [attribute :- [s/Any]]
  (let [keyword (key attribute)
        schema-type (val attribute)]
    {:db/ident       keyword
     :db/valueType   (schema-type->value-type schema-type)
     :db/cardinality :db.cardinality/one}))

(s/defn entity-schema :- [{s/Any s/Any}]
  [schema :- {s/Any s/Any}]
  (let [attributes (-> schema
                       seq)]
    (->> attributes
         (map skeleton-attribute->entity-attribute))))
