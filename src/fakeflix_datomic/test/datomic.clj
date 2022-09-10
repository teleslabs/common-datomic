(ns fakeflix-datomic.test.datomic
  (:require [fakeflix-datomic.config.datomic :as config]
            [schema.core :as s]))

(s/defn with-datomic-test
  [schemas :- [s/Any]]
  (config/start-datomic "unit-test" "unit-test" :mem schemas)
  @config/connection)
