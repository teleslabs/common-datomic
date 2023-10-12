(ns common-datomic.datomic
  (:require [common-datomic.config.datomic :as config]
            [datomic.api :as d]
            [schema.core :as s]))

(defn remove-datomic-meta
  [entity]
  (if (map? entity)
    (-> entity
        (dissoc :db/id)
        (dissoc :db/ident)
        (dissoc :db/doc)
        (dissoc :db/cardinality)
        (dissoc :db/valueType))
    entity))

(defn entity->model
  [entity]
  (->> entity
       first
       (clojure.walk/prewalk remove-datomic-meta)))

(s/defn assoc-db-id
  [entity]
  (if (map? entity)
    (assoc entity :db/id (rand-int 1000))
    entity))

(s/defn insert!
  [entity :- {s/Keyword s/Any}]
  (let [entity-with-db-id (clojure.walk/postwalk assoc-db-id entity)]
    @(d/transact @config/connection [entity-with-db-id])))

(s/defn query-inputs
  [partial-query
   inputs]
  (let [final-query (partial partial-query (first inputs))
        rest-inputs (rest inputs)]
    (if (empty? rest-inputs)
      final-query
      (query-inputs final-query rest-inputs))))

(s/defn db-query :- (s/maybe [{s/Keyword s/Any}])
  ([query :- [s/Any]]
   (let [db (d/db @config/connection)
         result (d/q query db)]
     (map entity->model result)))
  ([query :- [s/Any]
    & inputs :- [s/Any]]
   (let [db (d/db @config/connection)
         partial-query (partial d/q query db)
         complete-query (query-inputs partial-query (first inputs))
         result (complete-query)]
     (map entity->model result))))

(s/defn entities :- (s/maybe [{s/Keyword s/Any}])
  ([query :- [s/Any]]
   (db-query query))
  ([query :- [s/Any]
    & inputs :- [s/Any]]
   (db-query query inputs)))
