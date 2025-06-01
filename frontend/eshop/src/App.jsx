import { BrowserRouter, Routes, Route } from "react-router-dom";
import { Provider } from "react-redux";
import { store } from "./App/store";
import Navbar from "./components/Navbar";
import CustomerRoutes from "./routes/CustomerRoutes";
import AdminRoutes from "./routes/AdminRoutes";
import PublicRoutes from "./routes/PublicRoutes";
import UnauthorizedPage from "./pages/UnauthorizedPage";
import ProtectedRoute from "./components/ProtectedRoute";
import { useDispatch, useSelector } from "react-redux";
import { useEffect } from "react";
import { fetchCart } from "./features/Cart/cartSlice";
import PayPalCallback from './pages/checkout/PayPalCallback';
import CheckoutSuccess from './pages/checkout/CheckoutSuccess';
import CheckoutError from './pages/checkout/CheckoutError';

// Create a separate component for the app content
function AppContent() {
  const dispatch = useDispatch();
  const isAuthenticated = useSelector(state => state.auth.isAuthenticated);

  useEffect(() => {
    if (isAuthenticated) {
      dispatch(fetchCart());
    }
  }, [dispatch, isAuthenticated]);

  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        {/* Public routes */}
        <Route path="/*" element={<PublicRoutes />} />
        
        {/* Protected routes */}
        <Route path="/customer/*" element={
          <ProtectedRoute allowedRoles={['CUSTOMER']}>
            <CustomerRoutes />
          </ProtectedRoute>
        } />
        <Route path="/admin/*" element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminRoutes />
          </ProtectedRoute>
        } />
        
        {/* Unauthorized access page */}
        <Route path="/unauthorized" element={<UnauthorizedPage />} />

        <Route path="/paypal/callback" element={<PayPalCallback />} />
        <Route path="/checkout/success" element={<CheckoutSuccess />} />
        <Route path="/checkout/error" element={<CheckoutError />} />
      </Routes>
    </BrowserRouter>
  );
}

// Main App component
function App() {
  return (
    <Provider store={store}>
      <AppContent />
    </Provider>
  );
}

export default App;