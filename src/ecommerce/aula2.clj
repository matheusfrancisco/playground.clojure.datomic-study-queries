(ns ecommerce.aula2
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]))


(def conn (db/open-connection))

(db/create-schema conn)
;;datomic suporta somente um dos indentificadores

(let [calc {:product/name "Calculadora"}]
  (d/transact conn [calc]))


(let [celular-barato (model/new-product "Celular Barato" "/celular-barato" 3550.89M)
      resultado @(d/transact conn [celular-barato])
      ;id-entidade (first (vals (:tempids resultado)))]
      id-entidade (-> resultado :tempids vals first)]
  (pprint resultado)
  (pprint @(d/transact conn [[:db/add id-entidade :product/price 0.1M]]))
  (pprint @(d/transact conn [[:db/retract id-entidade :product/slug "/celular-barato"]]))
  (pprint (first (vals (:tempids resultado)))))

(db/delete-db)

