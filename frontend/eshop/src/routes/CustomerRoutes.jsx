import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Profile from '../pages/customer/Profile';
import Orders from '../pages/customer/Orders';

const CustomerRoutes = () => {
  return (
    <Routes>
      <Route path="/profile" element={<Profile />} />
      <Route path="/orders" element={<Orders />} />
      <Route path="/" element={<Profile />} />
    </Routes>
  );
};

export default CustomerRoutes;