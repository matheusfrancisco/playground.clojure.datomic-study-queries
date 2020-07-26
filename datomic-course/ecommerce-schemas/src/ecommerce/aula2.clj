(ns ecommerce.aula2
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [ecommerce.db :as db]
            [ecommerce.model :as model]
            [schema.core :as s]))

(s/set-fn-validation! true)

(db/apaga-banco!)
(def conn (db/abre-conexao!))
(db/cria-schema! conn)
(db/criar-dados-de-examplos conn)
(pprint (db/todas-as-categorias (d/db conn)))
(pprint (db/todos-os-produtos (d/db conn)))


(def dama (model/novo-produto "Dama" "/dama" 15.5M))
(db/adiciona-ou-altera-produtos! conn [dama])
(pprint (db/um-produto (d/db conn) (:produto/id dama)))

;update/insert ==> upsert
(db/adiciona-ou-altera-produtos! conn [(assoc dama :produto/slug "/jogo-de-dama")])
(pprint (db/um-produto (d/db conn) (:produto/id dama)))


(db/adiciona-ou-altera-produtos! conn [(assoc dama :produto/preco 150.5M)])
(pprint (db/um-produto (d/db conn) (:produto/id dama)))

; detectamos um problema que 'euma dificuldade..
; entender que updates sobrescreverão campos e
; operações anteriores

;(defn atualiza-preco []
;  (println "Atualizando preco")
;  (let [produto (db/um-produto (d/db conn) (:produto/id dama))
;        produto (assoc produto :produto/preco 999M)]
;    (db/adiciona-ou-altera-produtos! conn [produto])
;    (println "Atualizado slug")
;    produto))
;
;(defn atualiza-slug []
;  (println "Atualizando slug")
;  (let [produto (db/um-produto (d/db conn) (:produto/id dama))]
;    (Thread/sleep 3000)
;    (let [produto (assoc produto :produto/slug "/jogo-de-dama-carinho")]
;      (db/adiciona-ou-altera-produtos! conn [produto])
;      (println "Atualizado slug"))
;    produto))
;
;(defn roda-transacoes [tx]
;  (let [futuros (map #(future (%)) tx)]
;    (pprint (map deref futuros))
;    (pprint "Resultado final")
;    (pprint (db/um-produto (d/db conn) (:produto/id dama)))))
;
;(roda-transacoes [atualiza-preco atualiza-slug])



(defn atualiza-slug-inteligente []
  (println "Atualizando preco")
  (let [produto {:produto/id (:produto/id dama)
                 :produto/preco 111M}]
    (db/adiciona-ou-altera-produtos! conn [produto])
    (println "Atualizado slug")
    produto))

(defn atualiza-preco-inteligente []
  (println "Atualizando slug")
  (let [produto {:produto/id (:produto/id dama)
                 :produto/slug "/com-slug-novo"}]
    (Thread/sleep 3000)
    (db/adiciona-ou-altera-produtos! conn [produto])
    (println "Atualizado slug")
    produto))

(defn roda-transacoes [tx]
  (let [futuros (map #(future (%)) tx)]
    (pprint (map deref futuros))
    (pprint "Resultado final")
    (pprint (db/um-produto (d/db conn) (:produto/id dama)))))

(roda-transacoes [atualiza-preco-inteligente  atualiza-slug-inteligente])