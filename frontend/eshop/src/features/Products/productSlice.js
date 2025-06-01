import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
//import axios from "axios";

// Fetch from backend
// export const fetchProducts = createAsyncThunk(
//   "products/fetchProducts",
//   async (_, thunkAPI) => {
//     try {
//       const response = await axios.get("/api/products");
//       return response.data.content;
//     } catch (error) {
//       return thunkAPI.rejectWithValue(error.response?.data || error.message);
//     }
//   }
// );

const API_BASE = process.env.REACT_APP_API_BASE || "http://localhost:8080";
// Fetch products for the home page

export const fetchProducts = createAsyncThunk(
  "products/fetchProducts",
  async (_, thunkAPI) => {
    try {
      const response = await fetch("/api/products", {
        credentials: "include",
      });

      const result = await response.json();

      if (!response.ok) {
        return thunkAPI.rejectWithValue(result || "Failed to fetch products");
      }

      return result.content;
    } catch (error) {
      return thunkAPI.rejectWithValue(error.message || "Network error");
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
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchProducts.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchProducts.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload || [];
        state.filteredItems = filterProducts(state);
      })
      .addCase(fetchProducts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to fetch products";
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

export const { setSearchTerm, setSelectedCategory } = productSlice.actions;
export default productSlice.reducer;