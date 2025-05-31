import { Link } from "react-router-dom";
import React from "react";

function ProductCard({ product }) {
  return (
    <Link to={`/product/${product.id}`}>
      <div className="shadow-lg rounded-md cursor-pointer">
        <img
          src={product.imageUrl}
          alt={product.name ?? "Product Image"}
          className="w-full overflow-hidden"
        />

        <div className="bg-gray-50 p-4">
          <h2 className="text-lg font-semibold my-4">
            {(product.name?.substring(0, 25) ?? "No title") + "..."}
          </h2>
          <p className="text-sm text-zinc-500 border-b-2 pb-4">
            {(product.description?.substring(0, 70) ?? "No description") + "..."}
          </p>
          <div className="flex justify-between mt-4 items-center">
            <p className="text-xl font-semibold">${product.price}</p>
            <p>View Details</p>
          </div>
        </div>
      </div>
    </Link>
  );
}

export default ProductCard;