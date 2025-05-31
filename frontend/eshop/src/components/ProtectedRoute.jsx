import React, { useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Navigate, useLocation } from 'react-router-dom';
import { 
  selectIsAuthenticated, 
  selectUserRole,
  initializeAuth 
} from '../features/Auth/authSlice';

const ProtectedRoute = ({ children, allowedRoles }) => {
  const dispatch = useDispatch();
  const location = useLocation();
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const userRole = useSelector(selectUserRole);
  
  useEffect(() => {
    const checkAuth = async () => {
      if (!isAuthenticated || !userRole) {
        try {
          const response = await fetch('/api/auth/user-role', {
            credentials: 'include'
          });
          const data = await response.json();
          
          if (response.ok) {
            dispatch(initializeAuth({ role: data.role }));
          }
        } catch (error) {
          console.error('Error checking auth:', error);
        }
      }
    };

    checkAuth();
  }, [dispatch, isAuthenticated, userRole]);

  if (!isAuthenticated) {
    return <Navigate to="/" state={{ from: location }} replace />;
  }

  if (!allowedRoles.includes(userRole)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

export default ProtectedRoute;