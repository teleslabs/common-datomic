(ns common-datomic.test.datomic
  (:require [common-datomic.config.datomic :as config]
            [schema.core :as s]))

(s/defn with-datomic-test
  [schemas :- [s/Any]]
  (config/start-datomic "unit-test" "unit-test" :mem schemas)
  @config/connection)
