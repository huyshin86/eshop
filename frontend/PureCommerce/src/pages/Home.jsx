import React from "react";

import Footer from "../components/Footer";
import ProductGrid from "../components/ProductGrid";
import { useDispatch, useSelector } from "react-redux";
import { setSelectedCategory } from "../features/Products/productSlice";

const categories = [
  "All",
  "Dell",
  "Lenovo",
  "Macbook",
  "Others",
];

function Home() {
  const dispatch = useDispatch();

  return (
    <div>
      <div className="flex justify-center items-center h-screen bg"></div>
      <div className="mx-auto container my-10 px-4">
        <div className="flex gap-4">
          {categories.map((cat) => {
            return (
              <button
                className="bg-gray-300 py-2 px-4 rounded-md text-black active:scale-105 hover:bg-zinc-400 transition-all ease-in"
                key={cat}
                onClick={() => dispatch(setSelectedCategory(cat))}
              >
                {cat}
              </button>
            );
          })}
        </div>
        <ProductGrid />
      </div>

      {/* Footer */}
      <Footer />
    </div>
  );
}

export default Home;
