import { useEffect, useState } from "react";
import Footer from "../components/Footer";
import ProductGrid from "../components/ProductGrid";
import { useDispatch, useSelector } from "react-redux";
import { fetchProducts, setSelectedCategory, clearError } from "../features/Products/productSlice";
import { AlertCircle, X } from 'lucide-react';

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

  const { error, showError, searchTerm } = useSelector((state) => state.product);

  // Check if search term exists to hide background
  const hasSearchTerm = searchTerm && searchTerm.trim().length > 0;

  useEffect(() => {
    dispatch(fetchProducts());
  }, [dispatch]);

  useEffect(() => {
    // Apply background class to body based on search term
    if (hasSearchTerm) {
      document.body.classList.remove('page-with-bg');
    } else {
      document.body.classList.add('page-with-bg');
    }
    
    // Cleanup function to remove class when component unmounts
    return () => {
      document.body.classList.remove('page-with-bg');
    };
  }, [hasSearchTerm]);

  const handleSortChange = (e) => {
    console.log("Sort option changed to:", e.target.value);
    setSortOption(e.target.value);
  };

  return (
    <div className={`min-h-screen ${hasSearchTerm ? 'bg-white' : ''}`}>
      {/* Show error alert in the middle */}
      {showError && error && (
        <div className="fixed top-4 left-1/2 -translate-x-1/2 z-50">
          <div className="mx-auto max-w-md bg-red-50 border border-red-200 rounded-md p-4 shadow-lg">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <AlertCircle className="text-red-500 mr-2" size={20} />
                <span className="text-red-700">{error}</span>
              </div>
              <button
                onClick={() => dispatch(clearError())}
                className="text-red-500 hover:text-red-700"
              >
                <X size={16} />
              </button>
            </div>
          </div>
        </div>
      )}
      
      {/* Hero section - only show when no search term */}
      {!hasSearchTerm && (
        <div className="bg flex justify-center items-center"></div>
      )}
      
      {/* Content area */}
      <div className={hasSearchTerm ? 'bg-white' : 'bg-white bg-opacity-90 backdrop-blur-sm'}>
        <div className="container mx-auto px-4">
          <div className={`flex flex-wrap items-center justify-between gap-2 my-2 ${!hasSearchTerm ? 'pt-4' : 'pt-8'}`}>
            <div className="flex gap-4">
              {categories.map((cat) => {
                return (
                  <button
                    className="bg-gray-300 py-2 px-4 rounded-md text-black active:scale-105 hover:bg-zinc-400 transition-all ease-in shadow-md"
                    key={cat}
                    onClick={() => dispatch(setSelectedCategory(cat))}
                  >
                    {cat}
                  </button>
                );
              })}
            </div>

            <div className="flex items-center gap-2 my-2">
              <label htmlFor="sort" className="font-medium">Sort:</label>
              <select
                id="sort"
                value={sortOption}
                onChange={handleSortChange}
                className="border rounded-md p-2 shadow-sm"
              >
                <option value="name-asc">A → Z</option>
                <option value="name-desc">Z → A</option>
                <option value="price-asc">Low → High</option>
                <option value="price-desc">High → Low</option>
              </select>
            </div>
          </div>
          <ProductGrid sortOption={sortOption} />
        </div>

        {/* Footer */}
        <Footer />
      </div>
    </div>
  );
}

export default Home;