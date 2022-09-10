(ns fakeflix-datomic.datomic
  (:require [datomic.api :as d]
            [fakeflix-datomic.config.datomic :as config]
            [schema.core :as s]))

(defn entity->model
  [entity]
  (-> entity
      first
      (dissoc :db/id)))

(s/defn insert!
  [entity :- {s/Keyword s/Any}]
  (d/transact @config/connection [entity]))

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
   (let [db     (d/db @config/connection)
         result (d/q query db)]
     (map entity->model result)))
  ([query :- [s/Any]
    & inputs :- [s/Any]]
   (let [db             (d/db @config/connection)
         partial-query  (partial d/q query db)
         complete-query (query-inputs partial-query (first inputs))
         result         (complete-query)]
     (map entity->model result))))

(s/defn entities :- (s/maybe [{s/Keyword s/Any}])
  ([query :- [s/Any]]
   (db-query query))
  ([query :- [s/Any]
    & inputs :- [s/Any]]
   (db-query query inputs)))
