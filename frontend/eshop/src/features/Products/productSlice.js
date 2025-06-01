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

const API_BASE = process.env.REACT_APP_API_BASE || "http://localhost:8080";
// Fetch products for the home page

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

// Fetch list products for Admin
export const fetchAdminProducts = createAsyncThunk(
  "products/fetchAdminProducts",
  async ({ page = 0, size = 10 } = {}, thunkAPI) => {
    try {
      const res = await fetch(
        `${API_BASE}/api/admin/products?page=${page}&size=${size}`,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      const data = await res.json();
      if (!res.ok) {
        return thunkAPI.rejectWithValue(data || "Failed to fetch admin products");
      }
      return data;
    } catch (err) {
      return thunkAPI.rejectWithValue(err.message || "Network error");
    }
  }
);

// Create new product (upload image)
// Send form-data, including name, description, price, stock, category, imageFile
export const createAdminProduct = createAsyncThunk(
  "products/createAdminProduct",
  async (formData, thunkAPI) => {
    try {
      const res = await fetch(`${API_BASE}/api/admin/products`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: formData,
      });
      const data = await res.json();
      if (!res.ok) {
        return thunkAPI.rejectWithValue(data || "Failed to create product");
      }
      return data;
    } catch (err) {
      return thunkAPI.rejectWithValue(err.message || "Network error");
    }
  }
);

// Update product
export const updateAdminProduct = createAsyncThunk(
  "products/updateAdminProduct",
  async ({ id, formData }, thunkAPI) => {
    try {
      const res = await fetch(`${API_BASE}/api/admin/products/${id}`, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: formData,
      });
      const data = await res.json();
      if (!res.ok) {
        return thunkAPI.rejectWithValue(data || "Failed to update product");
      }
      return data;
    } catch (err) {
      return thunkAPI.rejectWithValue(err.message || "Network error");
    }
  }
);

// Delete product
export const deleteAdminProduct = createAsyncThunk(
  "products/deleteAdminProduct",
  async (id, thunkAPI) => {
    try {
      const res = await fetch(`${API_BASE}/api/admin/products/${id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      if (!res.ok) {
        const data = await res.json();
        return thunkAPI.rejectWithValue(data || "Failed to delete product");
      }
      return id;
    } catch (err) {
      return thunkAPI.rejectWithValue(err.message || "Network error");
    }
  }
);

// Update stock only
export const updateAdminProductStock = createAsyncThunk(
  "products/updateAdminProductStock",
  async ({ id, stock }, thunkAPI) => {
    try {
      const res = await fetch(`${API_BASE}/api/admin/products/${id}/stock`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({ stock }),
      });
      const data = await res.json();
      if (!res.ok) {
        return thunkAPI.rejectWithValue(data || "Failed to update stock");
      }
      return data;
    } catch (err) {
      return thunkAPI.rejectWithValue(err.message || "Network error");
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

    builder
      .addCase(fetchAdminProducts.pending, (state) => {
        state.adminLoading = true;
        state.adminError = null;
      })
      .addCase(fetchAdminProducts.fulfilled, (state, action) => {
        state.adminLoading = false;
        state.adminProducts = {
          content: action.payload.content,
          totalPages: action.payload.totalPages,
          totalElements: action.payload.totalElements,
        };
      })
      .addCase(fetchAdminProducts.rejected, (state, action) => {
        state.adminLoading = false;
        state.adminError = action.payload;
        state.adminProducts = { content: [], totalPages: 0, totalElements: 0 };
      });

    builder
      .addCase(createAdminProduct.pending, (state) => {
        state.adminLoading = true;
        state.adminError = null;
      })
      .addCase(createAdminProduct.fulfilled, (state, action) => {
        state.adminLoading = false;
        state.adminProducts.content.unshift(action.payload);
        state.adminProducts.totalElements += 1;
      })
      .addCase(createAdminProduct.rejected, (state, action) => {
        state.adminLoading = false;
        state.adminError = action.payload;
      });

    builder
      .addCase(updateAdminProduct.pending, (state) => {
        state.adminLoading = true;
        state.adminError = null;
      })
      .addCase(updateAdminProduct.fulfilled, (state, action) => {
        state.adminLoading = false;
        const updated = action.payload;
        const idx = state.adminProducts.content.findIndex(
          (p) => p.id === updated.id
        );
        if (idx !== -1) {
          state.adminProducts.content[idx] = updated;
        }
      })
      .addCase(updateAdminProduct.rejected, (state, action) => {
        state.adminLoading = false;
        state.adminError = action.payload;
      });

    builder
      .addCase(deleteAdminProduct.pending, (state) => {
        state.adminLoading = true;
        state.adminError = null;
      })
      .addCase(deleteAdminProduct.fulfilled, (state, action) => {
        state.adminLoading = false;
        state.adminProducts.content = state.adminProducts.content.filter(
          (p) => p.id !== action.payload
        );
        state.adminProducts.totalElements -= 1;
      })
      .addCase(deleteAdminProduct.rejected, (state, action) => {
        state.adminLoading = false;
        state.adminError = action.payload;
      });

    builder
      .addCase(updateAdminProductStock.pending, (state) => {
        state.adminLoading = true;
        state.adminError = null;
      })
      .addCase(updateAdminProductStock.fulfilled, (state, action) => {
        state.adminLoading = false;
        const updated = action.payload;
        const idx = state.adminProducts.content.findIndex(
          (p) => p.id === updated.id
        );
        if (idx !== -1) {
          state.adminProducts.content[idx].stock = updated.stock;
        }
      })
      .addCase(updateAdminProductStock.rejected, (state, action) => {
        state.adminLoading = false;
        state.adminError = action.payload;
      });
    },
});

export const { setSearchTerm, setSelectedCategory, clearError } = productSlice.actions;
export default productSlice.reducer;