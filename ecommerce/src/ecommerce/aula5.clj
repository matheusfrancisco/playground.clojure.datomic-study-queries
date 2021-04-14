(ns ecommerce.aula5
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]))



(db/delete-db)

(def conn (db/open-connection))

(db/create-schema conn)

;;não importa como voce extrai o momento da transacao
(let [notebook (model/new-product "Notebook" "/notebook" 3550.89M)
      celular (model/new-product "Celular" "/celular" 3550.89M)
      resultado @(d/transact conn [notebook celular])]
  (pprint resultado))

;; meu snapshot
(def snapshot-past (d/db conn))

(let [calc {:product/name "Calculadora"}
      celular-barato (model/new-product "Celular Barato" "/celular-barato" 0.9M)
      resultado @(d/transact conn [calc celular-barato])]
  (pprint resultado))

(pprint (db/all-product-with-pull-generic (d/db conn)))
(pprint (count (db/all-product-with-pull-generic (d/db conn))))

;rodando query num banco filtrado com dados do passado
(pprint (count (db/all-product-with-pull-generic snapshot-past)))

;;(pprint (count (db/all-product-with-pull-generic (d/db-as-of (d/db conn) #inst "2020-01-18t17:35:34.200"))))

;(db/delete-db)
