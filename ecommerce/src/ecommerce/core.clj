(ns ecommerce.core
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]))



;(db/delete-db)
(def conn (db/open-connection))

(db/create-schema conn)
(let [notebook (model/new-product "Notebook" "/notebook" 3550.89M)
      celular (model/new-product "Celular" "/celular" 5550.89M)
      calc {:product/name "Calculadora"}
      celular-barato (model/new-product "Celular Barato" "/celular-barato" 0.9M)
      resultado @(d/transact conn [notebook celular calc celular-barato])]
  (pprint resultado))


(pprint (db/all-product-by-minimal-price (d/db conn) 1000))
(pprint (db/all-product-by-minimal-price (d/db conn) 5000))
(pprint (db/all-product-by-minimal-price-restritiva (d/db conn) 5000))

(d/transact conn [[:db/add 17592186045418 :product/keywords "desktop"]
                  [:db/add 17592186045418 :product/keywords "computador"]])

(pprint (db/all-product-with-pull-generic (d/db conn)))

(d/transact conn [[:db/retract 17592186045418 :product/keywords "desktop"]])

(pprint (db/all-product-with-pull-generic (d/db conn)))

(pprint (db/all-product-by-keywords (d/db conn) "computador"))

;;update
(let [cheap-mobile (model/new-product "Cheap Mobile", "/cheap_mobile", 8888.88M)
      result @(d/transact conn [cheap-mobile])
      entity-id (first (vals (:tempids result)))]
  (pprint result)
  (pprint @(d/transact conn [[:db/add entity-id :product/price 0.1M]]))
  (pprint @(d/transact conn [[:db/retract entity-id :product/slug "/cheap_mobile"]])))



;(db/delete-db)
