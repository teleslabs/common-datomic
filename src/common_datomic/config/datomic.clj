(ns common-datomic.config.datomic
  (:require [common-datomic.config.schema :as schema]
            [datomic.api :as d]
            [schema.core :as s])
  (:import (datomic Connection)))

(def max-connection-attempts 3)
(def dummy-aws-access-key-id "dummy")
(def dummy-aws-access-secret-key "dummy")
(def connection (atom nil))

(s/defn transact-schemas!
  [connection :- Connection
   entity-schemas :- [{s/Keyword s/Keyword}]]
  (d/transact connection entity-schemas))

(s/defn create-database!
  ([uri :- s/Str]
   (create-database! uri 0))
  ([uri :- s/Str
    attempts :- s/Int]
   (let [attempt (inc attempts)]
     (try
       (d/create-database uri)
       (catch Exception e
         (if (< attempt max-connection-attempts)
           (do
             (println "Connection Failed, retrying in 10s...")
             (Thread/sleep 10000)
             (create-database! uri attempt))
           (throw e)))))))

(s/defn create-connection!
  [uri :- s/Str]
  (swap! connection (fn [_] (d/connect uri))))

(s/defn create-schemas :- [{s/Keyword s/Keyword}]
  [schemas :- [{s/Any s/Any}]]
  (->> schemas
       (map schema/entity-schema)
       (reduce into)))

(s/defn clear-database
  [uri :- s/Str]
  (d/delete-database uri))

(s/defn start-datomic
  ([host :- s/Str
    db-name :- s/Str
    password :- s/Str
    type :- s/Keyword
    schemas :- [{s/Any s/Any}]]
   (let [uri (str "datomic:" (name type) "://" host "/" db-name "?password=" password)
         entity-schemas (create-schemas schemas)]
     (clear-database uri)
     (d/create-database uri)
     (create-connection! uri)
     (transact-schemas! @connection entity-schemas)))
  ([db-name :- s/Str
    password :- s/Str
    type :- s/Keyword
    schemas :- [{s/Any s/Any}]]
   (start-datomic "localhost:4334" db-name password type schemas)))

; TODO: create schema for datomic config
(s/defn start-datomic-dev
  [{:keys [host db-name password]}
   schemas :- [{s/Any s/Any}]]
  (let [uri (str "datomic:dev://" host "/" db-name "?password=" password)
        entity-schemas (create-schemas schemas)]
    (create-database! uri)
    (create-connection! uri)
    (transact-schemas! @connection entity-schemas)))

(s/defn start-datomic-mem
  [{:keys [host db-name password]}
   schemas :- [{s/Any s/Any}]]
  (let [uri (str "datomic:mem://" host "/" db-name "?password=" password)
        entity-schemas (create-schemas schemas)]
    (clear-database uri)
    (create-database! uri)
    (create-connection! uri)
    (transact-schemas! @connection entity-schemas)))

(s/defn start-datomic-ddb-local
  [{:keys [host table db-name aws-access-key-id aws-access-secret-key]
    :or   {aws-access-key-id     dummy-aws-access-key-id
           aws-access-secret-key dummy-aws-access-secret-key}}
   schemas :- [{s/Any s/Any}]]
  (let [uri (str "datomic:ddb-local://" host "/" table "/" db-name "?aws_access_key_id=" aws-access-key-id "&aws_secret_key=" aws-access-secret-key)
        entity-schemas (create-schemas schemas)]
    (create-database! uri)
    (create-connection! uri)
    (transact-schemas! @connection entity-schemas)))

(s/defn start-datomic-mysql
  [{:keys [host db-name user password]}
   schemas :- [{s/Any s/Any}]]
  (let [uri (str "datomic:sql://" db-name "?jdbc:mysql://" host "/" db-name "?user=" user "&password=" password)
        entity-schemas (create-schemas schemas)]
    (create-database! uri)
    (create-connection! uri)
    (transact-schemas! @connection entity-schemas)))
