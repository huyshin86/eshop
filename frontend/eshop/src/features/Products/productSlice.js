import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";

const buildQueryString = (params) => {
  const query = new URLSearchParams();
  if (typeof params.q !== 'undefined') query.append('q', params.q);
  if (typeof params.page !== 'undefined') query.append('page', params.page);
  if (typeof params.size !== 'undefined') query.append('size', params.size);
  if (params.sortBy) query.append('sortBy', params.sortBy);
  if (params.sortDir) query.append('sortDir', params.sortDir);
  return query.toString();
};

export const fetchProducts = createAsyncThunk(
  "products/fetchProducts",
  async (params = {}, thunkAPI) => {
    try {
      let url = '/api/products';
      const state = thunkAPI.getState();
      const searchTerm = state.product.searchTerm;

      // Default query parameters
      const queryParams = {
        page: 0,
        size: 20,
        sortBy: 'productName',
        sortDir: 'asc',
        ...params,
      };

      // Use search endpoint if search term exists
      console.log("Search term:", searchTerm);
      if (searchTerm && searchTerm.trim()) {
        url = '/api/products/search';
        queryParams.q = searchTerm.trim();
      }

      const queryString = buildQueryString(queryParams);
      const response = await fetch(`${url}?${queryString}`);
      const result = await response.json();

      if (!response.ok) {
        const errorMessage = result?.message || result?.error || "Failed to fetch products";
        return thunkAPI.rejectWithValue(errorMessage);
      }

      return {
        items: result.content || [],
        pagination: {
          currentPage: result.number,
          totalPages: result.totalPages,
          totalElements: result.totalElements,
          size: result.size
        }
      };
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
  pagination: {
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    size: 20
  },
};

const filterProducts = (state) => {
  return state.items.filter((product) => {
    if (state.selectedCategory === "All") {
      return true;
    }

    const productName = product.name.toLowerCase();
    const selectedBrand = state.selectedCategory.toLowerCase();

    if (selectedBrand === "dell") {
      return productName.includes("dell");
    } else if (selectedBrand === "lenovo") {
      return productName.includes("lenovo");
    } else if (selectedBrand === "macbook") {
      return productName.includes("macbook");
    } else if (selectedBrand === "others") {
      return !productName.includes("dell") &&
        !productName.includes("lenovo") &&
        !productName.includes("macbook");
    }

    return true;
  });
};

const productSlice = createSlice({
  name: "products",
  initialState,
  reducers: {
    setSearchTerm: (state, action) => {
      state.searchTerm = action.payload;
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
        state.items = action.payload.items;
        state.filteredItems = filterProducts(state);
        state.showError = false;
        state.pagination = action.payload.pagination;
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