import { createSlice } from "@reduxjs/toolkit";
import Products from "../../productsContent";

const initialState = {
  items: Products,
  filteredItems: Products,
  searchTerm: "",
  selectedCategory: "All",
};

// Search Product and Filter Category
const filterProducts = (state) => {
  return state.items.filter((product) => {
    const matachesSearch = product.title
      .toLowerCase()
      .includes(state.searchTerm.toLowerCase()); // Search Product
    const matchCategory =
      state.selectedCategory === "All" ||
      product.category === state.selectedCategory;
    return matachesSearch && matchCategory;
  });
};

// create product slice
const productSlice = createSlice({
  name: "products",
  initialState,
  reducers: {
    setSearchTerm: (state, action) => {
      // Search Product Funcation
      state.searchTerm = action.payload;
      state.filteredItems = filterProducts(state);
    },
    setSelectedCategory: (state, action) => {
      // Filter Category Funcation
      state.selectedCategory = action.payload;
      state.filteredItems = filterProducts(state);
    },
  },
});

export const { setSearchTerm, setSelectedCategory } = productSlice.actions;
export default productSlice.reducer;
