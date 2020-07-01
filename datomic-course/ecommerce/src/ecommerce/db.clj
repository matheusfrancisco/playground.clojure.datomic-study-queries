(ns ecommerce.db
  (:use clojure.pprint)
  (:require [datomic.api :as d]))

(def db-uri "datomic:dev://localhost:4334/ecommerce")

(defn abre-conexao! []
  (d/create-database db-uri)
  (d/connect db-uri))

(defn apaga-banco! []
  (d/delete-database db-uri))

; Produtos
; id?
; nome String 1 ==> Computador Novo
; slug String 1 ==> /computador_novo
; preco ponto flutuante 1 ==> 3500.10]
; categoria_id integer ==> 3

; id_entidade atributo valor
; 15 :produto/nome Computador Novo     ID_TX     operacao
; 15 :produto/slug /computador_novo    ID_TX     operacao
; 15 :produto/preco 3500.10    ID_TX     operacao
; 15 :produto/categoria 37
; 17 :produto/nome Telefone Caro    ID_TX     operacao
; 17 :produto/slug /telefone    ID_TX     operacao
; 17 :produto/preco 8888.88    ID_TX     operacao



(def schema [{:db/ident       :produto/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O nome de um produto"}
             {:db/ident       :produto/slug
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O caminho para acessar esse produto via http"}
             {:db/ident       :produto/preco
              :db/valueType   :db.type/bigdec
              :db/cardinality :db.cardinality/one
              :db/doc         "O preço de um produto com precisão monetária"}
             {:db/ident       :produto/palavra-chave
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/many}
             {:db/ident       :produto/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}
             {:db/ident       :produto/categoria
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/one}


             ;Categorias
             {:db/ident       :categoria/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}
             {:db/ident       :categoria/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}

             ;Transacoes
             {:db/ident :tx-data/ip
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one}
             ])

(defn cria-schema! [conn]
  (d/transact conn schema))

; pull explicito atributo a atributo
;(defn todos-os-produtos [db]
;  (d/q '[:find (pull ?entidade [:produto/nome :produto/preco :produto/slug])
;         :where [?entidade :produto/nome]] db))

; pull generico, vantagem preguica, desvantagem pode trazer mais do que eu queira
(defn todos-os-produtos [db]
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :produto/nome]] db))

; no sql eh comum fazer:
; String sql = "meu codigo sql";
; conexao.query(sql)

; esse aqui eh similar ao String sql
; eh comum e voce pode querer extrair em um def ou let
; porem... -q é notacao hungara... indica o TIPO... humm.. não parece ser legal
; em clojure
; vc vai encontrar esse padro em alguns exemplos e documentacao
; nao recomendamos notacao hungara dessa maneira, ainda menos abreviada ;)
(def todos-os-produtos-por-slug-fixo-q
  '[:find ?entidade
    :where [?entidade :produto/slug "/computador-novo"]])

(defn todos-os-produtos-por-slug-fixo [db]
  (d/q todos-os-produtos-por-slug-fixo-q db))

; não estou usando notacao hungara e extract
; eh comum no sql: String sql = "select * from where slug=::SLUG::"
; conexao.query(sql, {::SLUG:: "/computador-novo})
(defn todos-os-produtos-por-slug [db slug]
  (d/q '[:find ?entidade
         :in $ ?slug-procurado                              ; proposital diferente da variável de clojure para evitar erros
         :where [?entidade :produto/slug ?slug-procurado]]
       db slug))


; ?entity => ?entidade => ?produto => ?p
; se não vai usar... _
(defn todos-os-slugs [db]
  (d/q '[:find ?slug
         :where [_ :produto/slug ?slug]] db))

; estou sendo explicito nos campos 1 a 1
(defn todos-os-produtos-por-preco [db preco-minimo-requisitado]
  (d/q '[:find ?nome, ?preco
         :in $, ?preco-minimo
         :keys produto/nome, produto/preco
         :where [?produto :produto/preco ?preco]
         [(> ?preco ?preco-minimo)]
         [?produto :produto/nome ?nome]]
       db, preco-minimo-requisitado))


; eu tenho 10mil...se eu tenho 1000 produtos com preco > 5000, so 10 produtos com quantidade < 10
; passar por 10 mil
;[(> preco  5000)]                                           ; => 5000 datom
;[(< quantidade 10)]                                         ; => 10 datom
;
;; passar por 10 mil
;[(< quantidade 10)]                                         ; => 10 datom
;[(> preco  5000)]                                           ; => 10 datom
;
; em geral vamos deixar as condicoes da mais restritiva pra menos restritiva...
; pois o plano de ação somos nós quem tomamos

(defn todos-os-produtos-por-palavra-chave [db palavra-chave-buscada]
  (d/q '[:find (pull ?produto [*])
         :in $ ?palavra-chave
         :where [?produto :produto/palavra-chave ?palavra-chave]]
       db palavra-chave-buscada))


(defn um-produto-por-db-id [db db-id]
  (d/pull db '[*] db-id))


(defn um-produto [db produto-id]
  (d/pull db '[*] [:produto/id produto-id]))


(defn todas-as-categorias [db]
  (d/q '[:find (pull ?categoria [*])
         :where [?categoria :categoria/id]] db))


(defn db-adds-de-atribuicao-de-categorias [produtos categoria]
  (reduce (fn [db-adds produto] (conj db-adds [:db/add
                                               [:produto/id (:produto/id produto)]
                                               :produto/categoria
                                               [:categoria/id (:categoria/id categoria)]]))
          []
          produtos))

(defn atribui-categorias! [conn produtos categoria]
  (let [a-transacionar (db-adds-de-atribuicao-de-categorias produtos categoria)]
    (d/transact conn a-transacionar)))


(defn adiciona-produtos!
  ([conn produtos]
    (d/transact conn produtos))
  ([conn produtos ip]
   (let [db-add-ip [:db/add "datomic.tx" :tx-data/ip ip]]
     (d/transact conn (conj produtos db-add-ip)))))

;como esses dois estão genéricos poderiam ser um só
;mas vamos manter dois pois se você usa schema fica mais fácil de trabalhar
(defn adiciona-categorias!
  [conn categorias]
  (d/transact conn categorias))


(defn todos-os-nomes-de-produtos-e-categorias [db]
  (d/q '[:find ?nome-do-produto, ?nome-da-categoria
         :keys produto/nome, categoria/nome
         :where [?produto :produto/nome ?nome-do-produto]
                [?produto :produto/categoria ?categoria]
                [?categoria :categoria/nome ?nome-da-categoria]]
       db))


;exemplo com forward navigation
;?produto :produto/categoria >>>>>
;(defn todos-os-produtos-da-categoria [db nome-da-categoria]
;  (d/q '[:find (pull ?produto [:produto/nome :produto/slug {:produto/categoria [:categoria/nome]}])
;         :in $ ?nome
;         :where [?categoria :categoria/nome ?nome]
;                [?produto :produto/categoria ?categoria]]
;       db nome-da-categoria))


;;exemplo com backward navigation
;>>>>>> :produto/_categoria ?categoria
(defn todos-os-produtos-da-categoria [db nome-da-categoria]
  (d/q '[:find (pull ?categoria [:categoria/nome {:produto/_categoria
                                                  [:produto/nome :produto/slug]}])
         :in $ ?nome
         :where [?categoria :categoria/nome ?nome]]
       db nome-da-categoria))

(defn resumo-dos-produtos [db]
  (d/q '[:find  (min ?preco) (max ?preco) (count ?preco)
         :keys min max total
         :with ?produto
         :where [?produto :produto/preco ?preco]]
       db))

(defn resumo-dos-produtos-por-categoria [db]
  (d/q '[:find ?nome (min ?preco) (max ?preco) (count ?preco)
         :keys categoria min max total
         :with ?produto
         :where [?produto :produto/preco ?preco]
                [?produto :produto/categoria ?categoria]
                [?categoria :categoria/nome ?nome]]
       db))

;uma variacao que faz duas queries soltas
;(defn todos-os-produtos-mais-caros [db]
;  (let [preco-mais-alto (ffirst (d/q '[:find (max ?preco)
;         :where [_ :produto/preco ?preco]]
;       db))]
;    (d/q '[:find (pull ?produto [*])
;           :in $ ?preco
;           :where [?produto :produto/preco ?preco]]
;         db preco-mais-alto)))

;duas queries de uma vez só
(defn todos-os-produtos-mais-caros [db]
  (d/q '[:find (pull ?produto [*])
         :where [(q '[:find (max ?preco)
                      :where [_ :produto/preco ?preco]]
                    $) [[?preco]]]
                [?produto :produto/preco ?preco]]
       db))


(defn todos-os-produtos-mais-barato [db]
  (d/q '[:find (pull ?produto [*])
         :where [(q '[:find (min ?preco)
                      :where [_ :produto/preco ?preco]]
                    $) [[?preco]]]
         [?produto :produto/preco ?preco]]
       db))

(defn todos-os-produtos-do-ip [db ip]
  (d/q '[:find (pull ?produto [*])
         :in $ ?ip-buscado
         :where [?transacao :tx-data/ip ?ip-buscado]
                [?produto :produto/id _ ?transacao]]
       db ip))
