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
              :db/doc          "Product price with money precision"}
             {:db/ident        :product/keywords
              :db/valueType    :db.type/string
              :db/cardinality  :db.cardinality/many}])


(defn create-schema [conn]
  (d/transact conn schema))

(defn all-product [db]
  (d/q '[:find ?entidade
         :where [?entidade :product/name]] db))

;;pull explicito atributo a atributo
(defn all-product-with-pull [db]
  (d/q '[:find (pull ?entidade [:product/name :product/slug :product/price])
         :where [?entidade :product/name]] db))

;;pull explicito  generico
(defn all-product-with-pull-generic [db]
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :product/name]] db))

;;explicit
(defn all-product-explicit [db]
  (d/q '[:find ?entidade ?price ?slug ?name
         :keys product/id product/price product/slug product/name
         :where [?entidade :product/name ?price]
                [?entidade :product/slug ?slug]
                [?entidade :product/name ?name]] db))

(defn all-product-explicit [db]
  (d/q '[:find ?entidade ?price ?slug
         :where [?entidade :product/name ?price]
                [?entidade :product/slug ?slug]] db))
;;extrair a query em uma def ou let
;porem ..-q é notacao hungara indica o TIPO não parece ser legal
;em clojure
;vai encontrar em exemplos e documentacao
;nao é recomendado ainda menos abreviada
(def all-product-by-slug-q
  '[:find ?entidade
    :where [?entidade :product/slug "/notebook"]])


(defn all-product-by-slug-fixo [db]
  (d/q all-product-by-slug-q db))

;;nao estou usando notacao hungara e extract
; eh comum no sql string "selec * from slug=::SLUG::"
;conexao.query(sql, {::SLUG:: "/notebook"})
(defn all-product-by-slug [db slug]
  (d/q '[:find ?entidade
         :in $ ?slug-searched
         :where [?entidade :product/slug ?slug-searched]]
       db slug))

; entity => entidade => ?produto => ?p
(defn all-slugs [db]
  (d/q '[:find ?slug
         :where [_ :product/slug ?slug]] db))

(defn all-price [db]
  (d/q '[:find ?price
         :where [_ :product/price ?price]] db))

;(defn all-price-and-name [db]
;  (d/q '[:find ?price ?name
;         :where [?produto :product/price ?price]
;                [?produto :product/name ?name]] db))
;
;(defn all-price-and-name [db]
;  (d/q '[:find ?price ?name
;         :keys price, p-name
;         :where [?produto :product/price ?price]
;                [?produto :product/name ?name]] db))
;
(defn all-price-and-name [db]
  (d/q '[:find ?price ?name
         :keys product/price, product/name
         :where [?produto :product/price ?price]
                [?produto :product/name ?name]] db))


;;using predicate in query
(defn all-product-by-minimal-price [db price-minimal-req]
  (d/q '[:find ?price ?name
         :in $, ?price-minimal
         :keys product/price, product/name
         :where [?produto :product/price ?price]
                [?produto :product/name ?name]
                [(> ?price ?price-minimal)]]
       db, price-minimal-req))

;;condicao + restritivas
;;definindo o plano de acao
;;plano de acao somo nos que definimos
(defn all-product-by-minimal-price-restritiva [db price-minimal-req]
  (d/q '[:find ?price ?name
         :in $, ?price-minimal
         :keys product/price, product/name
         :where [?produto :product/price ?price]
                [(> ?price ?price-minimal)]
                [?produto :product/name ?name]]
       db, price-minimal-req))

(defn all-product-by-keywords [db keywords]
  (d/q '[:find (pull ?product [*])
         :in $ ?keywords
         :where [?product :product/keywords ?keywords]]
       db keywords))

;; se eu tenho 10 mil
;> price 5000, so 10 produtos
;; passa por 10mil
;; 10 produto
;[(> price 5000)]
;[(< quantidade 10)]

;;passa por 10 mil
;;[(<quantidade 10)]
;;[(>price 5000)]


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
