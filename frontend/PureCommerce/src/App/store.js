import { configureStore } from "@reduxjs/toolkit";
import productReducer from "../features/Products/productSlice";
import cartReducer from "../features/Cart/cartSlice";

export const store = configureStore({
  reducer: {
    cart: cartReducer,
    product: productReducer,
  },
});
