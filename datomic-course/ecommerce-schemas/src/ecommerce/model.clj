(ns ecommerce.model
  (:require [schema.core :as s]))

(defn uuid [] (java.util.UUID/randomUUID))

(def Categoria
  {:categoria/id java.util.UUID
   :categoria/nome s/Str})

(def Produto
  {(s/optional-key :produto/nome)          s/Str
   (s/optional-key :produto/slug)          s/Str
   (s/optional-key :produto/preco)         BigDecimal
   :produto/id            java.util.UUID
   (s/optional-key :produto/palavra-chave) [s/Str]
   (s/optional-key :produto/categoria)     Categoria
   (s/optional-key :produto/estoque)       s/Int
   (s/optional-key :produto/digital)       s/Bool})



;; a "desvantagem" é copy paste nas chaves
;(s/defn novo-produto :- Produto
;  [produto]
;  (if (get produto :produto/id)
;    produto
;    (assoc produto :produto/id (uuid))))
;


(s/defn novo-produto :- Produto
  ([nome slug preco]
   (novo-produto (uuid) nome slug preco))
  ([uuid nome slug preco]
   (novo-produto uuid nome slug preco 0))
  ([uuid nome slug preco estoque]
   ;será que faz sentido aridade múltipla?
   ;pois ai entramos no problema de polimorfismo e múltiplos contrutores
   ;de outras linguagens
   ;poderíamos optar por um mapa.
   ; e se for por mapa, será que faz sentido um novo produto?
   {:produto/id    uuid
    :produto/nome  nome
    :produto/slug  slug
    :produto/preco preco
    :produto/estoque estoque
    :produto/digital false}))

(defn nova-categoria
  ([nome]
   (nova-categoria (uuid) nome))
  ([uuid nome]
   {:categoria/id   uuid
    :categoria/nome nome}))