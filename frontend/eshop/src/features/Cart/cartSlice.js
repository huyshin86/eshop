import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { performLogout } from '../Auth/authSlice';

// Async thunks for cart operations
export const fetchCart = createAsyncThunk(
  'cart/fetchCart',
  async (_, { rejectWithValue }) => {
    console.log('Fetching cart from server...');
    try {
      const response = await fetch('/api/cart', {
        credentials: 'include'
      });
      if (!response.ok) throw new Error('Failed to fetch cart');
      const result = await response.json();
      return result.data;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const addToCartAsync = createAsyncThunk(
  'cart/addToCartAsync',
  async (product, { rejectWithValue }) => {
    try {
      const response = await fetch('/api/cart', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
          productId: product.id,
          quantity: 1
        })
      });
      if (!response.ok) throw new Error('Failed to add item to cart');
      const result = await response.json();
      return result.data; // Return the updated cart data
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const updateCartQuantityAsync = createAsyncThunk(
  'cart/updateCartQuantityAsync',
  async ({ id, quantity }, { rejectWithValue }) => {
    try {
      const response = await fetch(`/api/cart/${id}?quantity=${quantity}`, {
        method: 'PUT',
        credentials: 'include'
      });
      if (!response.ok) throw new Error('Failed to update cart');
      const result = await response.json();
      return result.data; // Return the updated cart data
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const removeFromCartAsync = createAsyncThunk(
  'cart/removeFromCartAsync',
  async (id, { rejectWithValue }) => {
    try {
      const response = await fetch(`/api/cart/${id}`, {
        method: 'DELETE',
        credentials: 'include'
      });
      if (!response.ok) throw new Error('Failed to remove item from cart');
      const result = await response.json();
      return result.data; // Return the updated cart data
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const initiateCheckout = createAsyncThunk(
  'cart/initiateCheckout',
  async (_, { rejectWithValue }) => {
    try {
      const response = await fetch('/api/checkout/initialize', {
        method: 'POST',
        credentials: 'include',
      });

      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.message || 'Failed to initialize checkout');
      }

      // Extract PayPal details from response
      const paypalDetails = result.data?.payPalOrderDetailDto;
      if (!paypalDetails?.approvalUrl || !paypalDetails?.paypalOrderId) {
        throw new Error('Invalid checkout response');
      }

      return paypalDetails.approvalUrl;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const clearCartAsync = createAsyncThunk(
  'cart/clearCartAsync',
  async (_, { rejectWithValue }) => {
    try {
      const response = await fetch('/api/cart', {
        method: 'DELETE',
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error('Failed to clear cart');
      }

      return true;
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

const initialState = {
  items: [],
  totalPrice: 0,
  loading: false,
  error: null,
  isInitialized: false,
};

// Helper function to calculate total price
const calculateTotalPrice = (items) => {
  return items.reduce((total, item) => {
    const price = item.product?.price || item.price || 0;
    return total + (price * item.quantity);
  }, 0);
};

// Helper function to normalize cart items for consistent structure
const normalizeCartItems = (items, isFromServer = false) => {
  if (!Array.isArray(items)) return [];

  return items.map(item => {
    if (isFromServer) {
      // Server response format
      return {
        id: item.product.id,
        cartItemId: item.cartItemId,
        title: item.product.name,
        name: item.product.name,
        price: item.product.price,
        imageUrl: item.product.imageUrl,
        quantity: item.quantity,
        isAvailableInStock: item.isAvailableInStock,
        product: item.product
      };
    } else {
      // Local format (unauthenticated users)
      return {
        ...item,
        cartItemId: null,
        isAvailableInStock: true,
        product: null
      };
    }
  });
};

const cartSlice = createSlice({
  name: "cart",
  initialState,
  reducers: {
    // Clear Cart
    clearCart: (state) => {
      state.items = [];
      state.totalPrice = 0;
      state.error = null;
    },

    // Add Cart for unauthenticated users
    addToCart: (state, action) => {
      const existingItem = state.items.find(
        (item) => item.id === action.payload.id
      );
      if (existingItem) {
        existingItem.quantity += 1;
      } else {
        const newItem = { ...action.payload, quantity: 1 };
        state.items.push(normalizeCartItems([newItem])[0]);
      }
      state.totalPrice = calculateTotalPrice(state.items);
      state.error = null;
    },

    // Remove Cart for unauthenticated users
    removeFromCart: (state, action) => {
      state.items = state.items.filter((item) => item.id !== action.payload);
      state.totalPrice = calculateTotalPrice(state.items);
      state.error = null;
    },

    // Update quantity for unauthenticated users
    updateQuantity: (state, action) => {
      const item = state.items.find((item) => item.id === action.payload.id);
      if (item) {
        item.quantity = action.payload.quantity;
        state.totalPrice = calculateTotalPrice(state.items);
      }
      state.error = null;
    },

    // Clear error
    clearError: (state) => {
      state.error = null;
    }
  },
  extraReducers: (builder) => {
    builder
      // Fetch Cart
      .addCase(fetchCart.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchCart.fulfilled, (state, action) => {
        state.loading = false;
        state.isInitialized = true;

        if (action.payload && action.payload.items) {
          state.items = normalizeCartItems(action.payload.items, true);
          state.totalPrice = action.payload.totalPrice || calculateTotalPrice(state.items);
        } else {
          state.items = [];
          state.totalPrice = 0;
        }
        state.error = null;
      })
      .addCase(fetchCart.rejected, (state, action) => {
        state.loading = false;
        state.isInitialized = true;
        state.error = action.payload;
      })

      // Add to Cart
      .addCase(addToCartAsync.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(addToCartAsync.fulfilled, (state, action) => {
        state.loading = false;

        if (action.payload && action.payload.items) {
          // Server returned updated cart
          state.items = normalizeCartItems(action.payload.items, true);
          state.totalPrice = action.payload.totalPrice || calculateTotalPrice(state.items);
        } else {
          // Fallback: update locally
          const existingItem = state.items.find(
            (item) => item.id === action.meta.arg.id
          );
          if (existingItem) {
            existingItem.quantity += 1;
          } else {
            const newItem = normalizeCartItems([{ ...action.meta.arg, quantity: 1 }])[0];
            state.items.push(newItem);
          }
          state.totalPrice = calculateTotalPrice(state.items);
        }
        state.error = null;
      })
      .addCase(addToCartAsync.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Update Cart Quantity
      .addCase(updateCartQuantityAsync.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateCartQuantityAsync.fulfilled, (state, action) => {
        state.loading = false;

        if (action.payload && action.payload.items) {
          // Server returned updated cart
          state.items = normalizeCartItems(action.payload.items, true);
          state.totalPrice = action.payload.totalPrice || calculateTotalPrice(state.items);
        } else {
          // Fallback: update locally
          const item = state.items.find((item) =>
            item.cartItemId === action.meta.arg.id || item.id === action.meta.arg.id
          );
          if (item) {
            item.quantity = action.meta.arg.quantity;
          }
          state.totalPrice = calculateTotalPrice(state.items);
        }
        state.error = null;
      })
      .addCase(updateCartQuantityAsync.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Remove from Cart
      .addCase(removeFromCartAsync.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(removeFromCartAsync.fulfilled, (state, action) => {
        state.loading = false;

        if (action.payload && action.payload.items) {
          // Server returned updated cart
          state.items = normalizeCartItems(action.payload.items, true);
          state.totalPrice = action.payload.totalPrice || calculateTotalPrice(state.items);
        } else {
          // Fallback: update locally
          state.items = state.items.filter((item) =>
            item.cartItemId !== action.meta.arg && item.id !== action.meta.arg
          );
          state.totalPrice = calculateTotalPrice(state.items);
        }
        state.error = null;
      })
      .addCase(removeFromCartAsync.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Handle logout
      .addCase(performLogout.fulfilled, (state) => {
        state.items = [];
        state.totalPrice = 0;
        state.loading = false;
        state.error = null;
      })

      // Initiate Checkout
      .addCase(initiateCheckout.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(initiateCheckout.fulfilled, (state) => {
        state.loading = false;
        state.error = null;
      })
      .addCase(initiateCheckout.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const { addToCart, removeFromCart, updateQuantity, clearCart, clearError,  } = cartSlice.actions;
export default cartSlice.reducer;