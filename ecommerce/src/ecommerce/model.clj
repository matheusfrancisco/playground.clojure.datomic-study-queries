(ns ecommerce.model)

(defn new-product
  [product-name slug price]
  {:product/name product-name
   :product/slug slug
   :product/price price})
