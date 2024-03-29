(ns ecommerce.aula6
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]))



(db/delete-db)

(def conn (db/open-connection))

(db/create-schema conn)

(let [notebook (model/new-product "Notebook" "/notebook" 3550.89M)
      celular (model/new-product "Celular" "/celular" 3550.89M)
      calc {:product/name "Calculadora"}
      celular-barato (model/new-product "Celular Barato" "/celular-barato" 0.9M)]
  (d/transact conn [notebook celular calc celular-barato]))


(pprint (db/all-product (d/db conn)))
(pprint (db/all-product-by-slug-fixo (d/db conn)))
(pprint (db/all-product-by-slug (d/db conn) "/notebook"))

(pprint (db/all-slugs (d/db conn)))

(pprint (db/all-price (d/db conn)))
(pprint (db/all-price-and-name (d/db conn)))

(pprint (db/all-product-explicit (d/db conn)))
(pprint (db/all-product-with-pull (d/db conn)))
(pprint (db/all-product-with-pull-generic (d/db conn)))

(db/delete-db)
