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
  const [sortOption, setSortOption] = React.useState("name-asc");

  return (
    <div>
      <div className="flex justify-center items-center h-screen bg"></div>
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
          <label htmlFor="sort" className="font-medium">Filter:</label>
          <select
          id="sort"
          value={sortOption}
          onChange={(e) => setSortOption(e.target.value)}
          className="border rounded-md p-2"
          >
            <option value="name-asc">A → Z</option>
            <option value="name-desc">Z → A</option>
            <option value="price-asc">Low → High</option>
            <option value="price-desc">High → Low</option>
          </select>
        </div>

        <ProductGrid sortOption={sortOption} />

      </div>

      {/* Footer */}
      <Footer />
    </div>
  );
}

export default Home;
