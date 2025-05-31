import { configureStore } from "@reduxjs/toolkit";
import productReducer from "../features/Products/productSlice";
import cartReducer from "../features/Cart/cartSlice";
import authReducer from '../features/Auth/authSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    cart: cartReducer,
    product: productReducer,
  },
});
