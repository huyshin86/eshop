import React from "react";
import { useSelector } from "react-redux";
import ProductCard from "./ProductCard";
import { useMemo } from "react";

export default function ProductGrid({ sortOption }) {
  const products = useSelector((state) => state.product.filteredItems);

  const sortedProducts = useMemo(() => {
    if (!products || products.length === 0) {
      return [];
    }

    const arr = [...products];

    switch (sortOption) {
      case "name-asc":
        return arr.sort((a, b) => {
          const nameA = (a.name || "").trim().toLowerCase();
          const nameB = (b.name || "").trim().toLowerCase();
          return nameA.localeCompare(nameB);
        });
      case "name-desc":
        return arr.sort((a, b) => {
          const nameA = (a.name || "").trim().toLowerCase();
          const nameB = (b.name || "").trim().toLowerCase();
          return nameB.localeCompare(nameA);
        });
      case "price-asc":
        return arr.sort((a, b) => {
          const priceA = parseFloat(a.price) || 0;
          const priceB = parseFloat(b.price) || 0;
          return priceA - priceB;
        });
      case "price-desc":
        return arr.sort((a, b) => {
          const priceA = parseFloat(a.price) || 0;
          const priceB = parseFloat(b.price) || 0;
          return priceB - priceA;
        });
      default:
        return arr;
    }
  }, [products, sortOption]);

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-16 my-24">
      {sortedProducts.map((product) => (
        <ProductCard key={product.id} product={product} />
      ))}
    </div>
  );
}