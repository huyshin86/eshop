import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

// Use consistent initial state
const initialState = {
  isAuthenticated: false,
  user: null,
  role: null,
  isAdmin: false,
  loading: false,
  error: null,
  logoutMessage: null,
};

export const performLogout = createAsyncThunk(
  'auth/performLogout',
  async (_, { rejectWithValue }) => {
    try {
      const response = await fetch('/api/logout', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      });

      if (!response.ok) {
        const errorData = await response.json();
        return rejectWithValue(errorData.message || 'Logout failed');
      }

      const data = await response.json();
      return data.message; // Success message from the backend
    } catch (error) {
      console.error('Logout API call error:', error);
      return rejectWithValue(error.message || 'Network error during logout');
    }
  }
);

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    logout: (state) => {
      state.isAuthenticated = false;
      state.user = null;
      state.role = null;
      state.isAdmin = false;
      state.error = null;
    },
    setLoggedIn: (state, action) => {
      state.isAuthenticated = true;
      state.role = action.payload.role;
      state.isAdmin = action.payload.role === 'ADMIN';
      state.error = null; // Clear any previous errors
    },
    clearLogoutMessage: (state) => {
      state.logoutMessage = null;
    },
    clearAuthError: (state) => {
      state.error = null;
    },
    setAuthError: (state, action) => {
      state.error = action.payload;
    },
    setAuthLoading: (state, action) => {
      state.loading = action.payload;
    },
    initializeAuth: (state, action) => {
      state.isAuthenticated = true;
      state.role = action.payload.role;
      state.isAdmin = action.payload.role === 'ADMIN';
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(performLogout.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(performLogout.fulfilled, (state, action) => {
        // Clear all authentication state on successful logout
        state.isAuthenticated = false;
        state.user = null;
        state.role = null;
        state.isAdmin = false;
        state.loading = false;
        state.error = null;
        state.logoutMessage = action.payload; // Store success message
      })
      .addCase(performLogout.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

// Export actions
export const { 
  logout, 
  setLoggedIn, 
  clearLogoutMessage, 
  clearAuthError, 
  setAuthError, 
  setAuthLoading,
  initializeAuth 
} = authSlice.actions;

// Selectors - Updated to match the consistent state structure
export const selectIsAdmin = (state) => state.auth.isAdmin;
export const selectIsCustomer = (state) => state.auth.role === 'CUSTOMER';
export const selectIsAuthenticated = (state) => state.auth.isAuthenticated;
export const selectAuthLoading = (state) => state.auth.loading;
export const selectLogoutMessage = (state) => state.auth.logoutMessage;
export const selectAuthError = (state) => state.auth.error;
export const selectUserRole = (state) => state.auth.role;

export default authSlice.reducer;