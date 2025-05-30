import React from "react";
import { useSelector } from "react-redux";
import ProductCard from "./ProductCard";
import { useMemo } from "react";

export default function ProductGrid({ sortOption }) {
  const products = useSelector((state) => state.product.filteredItems);

  const sortedProducts = useMemo(() => {
    const arr = [...products];
    switch (sortOption) {
      case "name-asc":
        return arr.sort((a, b) => a.title.localeCompare(b.title));
      case "name-desc":
        return arr.sort((a, b) => b.title.localeCompare(a.title));
      case "price-asc":
        return arr.sort((a, b) => a.price - b.price);
      case "price-desc":
        return arr.sort((a, b) => b.price - a.price);
              default:
        return arr;
    }
  }, [products, sortOption]);

  return (
    <div className="grid grid-col-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4   gap-16 my-24">
      {sortedProducts.map((product) => (
        <ProductCard key={product.id} product={product} />
      ))}
    </div>
  );
}
