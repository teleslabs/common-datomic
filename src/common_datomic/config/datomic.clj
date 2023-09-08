(ns common-datomic.config.datomic
  (:require [common-datomic.config.schema :as schema]
            [datomic.api :as d]
            [schema.core :as s])
  (:import (datomic Connection)))

(def connection (atom nil))

(s/defn transact-schemas!
  [connection :- Connection
   entity-schemas :- [{s/Keyword s/Keyword}]]
  (d/transact connection entity-schemas))

(s/defn create-connection!
  [uri :- s/Str]
  (swap! connection (fn [_] (d/connect uri))))

(s/defn create-schemas :- [{s/Keyword s/Keyword}]
  [schemas :- [{s/Any s/Any}]]
  (->> schemas
       (map schema/entity-schema)
       (reduce into)))

(s/defn clean-mem-database
  [uri :- s/Str
   type :- s/Keyword]
  (if (= type :mem)
    (d/delete-database uri)))

(s/defn start-datomic
  ([host :- s/Str
    db-name :- s/Str
    password :- s/Str
    type :- s/Keyword
    schemas :- [{s/Any s/Any}]]
   (let [uri (str "datomic:" (name type)  "://" host "/" db-name "?password=" password)
         entity-schemas (create-schemas schemas)]
     (clean-mem-database uri type)
     (d/create-database uri)
     (create-connection! uri)
     (transact-schemas! @connection entity-schemas)))
  ([db-name :- s/Str
    password :- s/Str
    type :- s/Keyword
    schemas :- [{s/Any s/Any}]]
   (start-datomic "localhost:4334" db-name password type schemas)))
