(ns ecommerce.aula3
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]))

(def conn (db/abre-conexao!))

(db/cria-schema! conn)

(def eletronicos (model/nova-categoria "Eletrônicos"))
(def esporte (model/nova-categoria "Esporte"))

(pprint @(db/adiciona-categorias! conn [eletronicos, esporte]))

;;todas as categorias
(def categorias (db/todas-as-categorias! (d/db conn)))
(pprint categorias)


(def computador (model/novo-produto (model/uuid) "Computador Novo", "/computador-novo", 2500.10M))
(def  celular (model/novo-produto (model/uuid) "Celular Caro", "/celular", 888888.10M))
(def  calculadora {:produto/nome "Calculadora com 4 operações"})
(def celular-barato (model/novo-produto "Celular Barato", "/celular-barato", 0.1M))
(def xadrez (model/novo-produto "Trabuleiro de  xadrez" "/tabuleir-de-xadrez" 30M ))

(pprint @(db/adiciona-produtos! conn [computador, celular, calculadora, celular-barato, xadrez]))

;(pprint (db/todos-os-produtos (d/db conn)))
(def produtos (db/todos-os-produtos (d/db conn)))
(pprint produtos)


(db/atribui-categorias! conn [computador celular celular-barato] eletronicos)
(db/atribui-categorias! conn [xadrez] esporte)


(pprint (d/transact conn [[:db/add
                   [:produto/id (:produto/id computador)]
                   :produto/categoria
                   [:categoria/id (:categoria/id eletronicos)]]]))

(pprint (db/um-produto (d/db conn) (:produto/id computador)))

(pprint (db/um-produto (d/db conn) (:produto/id celular)))

(pprint (db/um-produto (d/db conn) (:produto/id celular-barato)))

(pprint (db/um-produto (d/db conn) (:produto/id xadrez)))

(def produtos (db/todos-os-produtos (d/db conn)))
(pprint produtos)


(db/apaga-banco!)
