(ns ecommerce.db
  (:use clojure.pprint)
  (:require [datomic.api :as d]))


(def db-uri "datomic:dev://localhost:4334/hello")

(defn open-connection []
  (d/create-database db-uri)
  (d/connect db-uri))

(defn delete-db []
  (d/delete-database db-uri))


(def schema [{:db/ident       :product/name
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "Product Name"}
             {:db/ident       :product/slug
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "Path to open this product by http"}
             {:db/ident        :product/price
              :db/valueType    :db.type/bigdec
              :db/cardinality  :db.cardinality/one
              :db/doc          "Product price with money precision"}])


(defn create-schema [conn]
  (d/transact conn schema))

(defn all-product [db]
  (d/q '[:find ?entidade
         :where [?entidade :product/name]] db))


;;extrair a query em uma def ou let
;porem ..-q é notacao hungara indica o TIPO não parece ser legal
;em clojure
;vai encontrar em exemplos e documentacao
;nao é recomendado ainda menos abreviada
(def all-product-by-slug-q
  '[:find ?entidade
    :where [?entidade :product/slug "/notebook"]])


(defn db/all-product-by-slug-fixo [db]
  (d/q all-product-by-slug-q db))

;;nao estou usando notacao hungara e extract
; eh comum no sql string "selec * from slug=::SLUG::"
;conexao.query(sql, {::SLUG:: "/notebook"})
(defn db/all-product-by-slug [db slug]
  (d/q '[:find ?entidade
         :in $ ?slug-searched
         :where [?entidade :product/slug ?slug-searched]]
       db slug))
;Product
; id?
; name String 1 ==> Notebook
; slug String 1 ==> /notebook
; preco float 1 ==> 345.49


; id_entidade atributo valor
; 15 :product/nome Computador Novo ID_TX operacao (true or false)
; 15 :product/slug /computador_novo ID_TX operacao (true or false)
; 15 :produdc/preco 4545.45 ID_TX operacao (true or false)
; 17 :produdc/nome Telefone ID_TX operacao (true or false)
; 17 :produdc/slug /telefone ID_TX operacao (true or false)
; 17 :produdc/preco 8888.88 ID_TX operacao (true or false)
