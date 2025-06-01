import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";

export const fetchProducts = createAsyncThunk(
  "products/fetchProducts",
  async (_, thunkAPI) => {
    try {
      const response = await fetch("/api/products");

      const result = await response.json();

      if (!response.ok) {
        const errorMessage = result?.message || result?.error || "Failed to fetch products";
        return thunkAPI.rejectWithValue(errorMessage);
      }

      return result.content;
    } catch {
      return thunkAPI.rejectWithValue("Network error");
    }
  }
);


const initialState = {
  items: [],
  filteredItems: [],
  searchTerm: "",
  selectedCategory: "All",
  loading: false,
  error: null,
  showError: false,
};

const filterProducts = (state) => {
  return state.items.filter((product) => {
    const matchesSearch = product.name
      .toLowerCase()
      .includes(state.searchTerm.toLowerCase());

    let matchesCategory = false;

    if (state.selectedCategory === "All") {
      matchesCategory = true;
    } else {
      // Filter by brand name in product name
      const productName = product.name.toLowerCase();
      const selectedBrand = state.selectedCategory.toLowerCase();

      if (selectedBrand === "dell") {
        matchesCategory = productName.includes("dell");
      } else if (selectedBrand === "lenovo") {
        matchesCategory = productName.includes("lenovo");
      } else if (selectedBrand === "macbook") {
        matchesCategory = productName.includes("macbook");
      } else if (selectedBrand === "others") {
        // "Others" means products that don't contain Dell, Lenovo, or Macbook
        matchesCategory = !productName.includes("dell") &&
                         !productName.includes("lenovo") &&
                         !productName.includes("macbook");
      }
    }

    return matchesSearch && matchesCategory;
  });
};

const productSlice = createSlice({
  name: "products",
  initialState,
  reducers: {
    setSearchTerm: (state, action) => {
      state.searchTerm = action.payload;
      state.filteredItems = filterProducts(state);
    },
    setSelectedCategory: (state, action) => {
      state.selectedCategory = action.payload;
      state.filteredItems = filterProducts(state);
    },
    clearError: (state) => {
      state.error = null;
      state.showError = false;
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchProducts.pending, (state) => {
        state.loading = true;
        state.error = null;
        state.showError = false;
      })
      .addCase(fetchProducts.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload || [];
        state.filteredItems = filterProducts(state);
        state.showError = false;
      })
      .addCase(fetchProducts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to fetch products";
        state.showError = true;
        state.items = [];
        state.filteredItems = [];
      });
  },
});

export const { setSearchTerm, setSelectedCategory, clearError } = productSlice.actions;
export default productSlice.reducer;