import React, { useEffect, useState } from "react";
import Footer from "../components/Footer";
import ProductGrid from "../components/ProductGrid";
import { useDispatch } from "react-redux";
import { fetchProducts, setSelectedCategory } from "../features/Products/productSlice";

const categories = [
  "All",
  "Dell",
  "Lenovo",
  "Macbook",
  "Others",
];

function Home() {
  const dispatch = useDispatch();
  const [sortOption, setSortOption] = useState("name-asc");

  useEffect(() => {
    dispatch(fetchProducts());
  }, [dispatch]);

  const handleSortChange = (e) => {
    console.log("Sort option changed to:", e.target.value);
    setSortOption(e.target.value);
  };

  return (
    <div>
      <div className="bg flex justify-center items-center"></div>
      <div className="flex flex-wrap items-center justify-between gap-2 mx-2 my-2">
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

        <div className="flex items-center gap-2 mx-2 my-2">
          <label htmlFor="sort" className="font-medium">Sort:</label>
          <select
            id="sort"
            value={sortOption}
            onChange={handleSortChange}
            className="border rounded-md p-2"
          >
            <option value="name-asc">A → Z</option>
            <option value="name-desc">Z → A</option>
            <option value="price-asc">Low → High</option>
            <option value="price-desc">High → Low</option>
          </select>
        </div>
      </div>

      <ProductGrid sortOption={sortOption} />

      {/* Footer */}
      <Footer />
    </div>
  );
}

export default Home;