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

(s/defn find-entities! :- (s/maybe [{s/Keyword s/Any}])
  [query :- [s/Any]]
  (let [result (d/q query (d/db @config/connection))]
    (map entity->model result)))
