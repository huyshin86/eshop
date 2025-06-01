import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Dashboard from '../pages/admin/Dashboard';
import Products from '../pages/admin/Products';
import ProductForm from '../pages/admin/ProductForm';

const AdminRoutes = () => {
  return (
    <Routes>
      <Route path="/dashboard" element={<Dashboard />} />
      <Route path="/products" element={<Products />} />
      <Route path="/products/new" element={<ProductForm />} />
      <Route path="/products/:id/edit" element={<ProductForm />} />
    </Routes>
  );
};

export default AdminRoutes;