(ns ecommerce.model)


(defn uuid []
  (java.util.UUID/randomUUID))

(defn novo-produto
  ; gerar ids dinamicamente
  ; ou usar ids passado
  ([nome slug preco]
   (novo-produto (uuid) nome slug preco))
  ([uuid nome slug preco]
  {:produto/id    uuid
   :produto/nome  nome
   :produto/slug  slug
   :produto/preco preco}))


(defn nova-categoria
  ([nome]
   (nova-categoria (uuid) nome))
  ([uuid nome]
   {:categoria/id uuid
    :categoria/nome nome}))