import { ShoppingCart, User } from "lucide-react";
import { useState, useEffect, useRef } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import Logo from "../assets/imgs/showcase/logo.svg";
import { useDispatch, useSelector } from "react-redux";
import { setSearchTerm, fetchProducts } from "../features/Products/productSlice";
import AuthForms from "../pages/AuthForms";
import ReactDOM from "react-dom";
import {
  logout as logoutReducer,
  performLogout,
  selectIsAuthenticated,
  selectAuthLoading,
  selectIsAdmin,
  selectLogoutMessage,
  setLoggedIn,
  clearLogoutMessage,
} from "../features/Auth/authSlice";
import AdminNav from "./AdminNav";

function Navbar() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const searchTerm = useSelector((state) => state.product.searchTerm);

  const [isUserDropdownOpen, setIsUserDropdownOpen] = useState(false);
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [showLogoutToast, setShowLogoutToast] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const userDropdownRef = useRef(null);

  const cartItems = useSelector((state) => state.cart?.items || []);
  const itemCount = cartItems.reduce((total, item) => total + item.quantity, 0);

  const isAuthenticated = useSelector(selectIsAuthenticated);
  const isAdmin = useSelector(selectIsAdmin);
  const isLoadingAuth = useSelector(selectAuthLoading);
  const logoutMessage = useSelector(selectLogoutMessage);

  const handleUserIconClick = (e) => {
    e.stopPropagation();
    if (!isLoggingOut) {
      setIsUserDropdownOpen(!isUserDropdownOpen);
    }
  };

  const handleCloseAuthModal = () => {
    setShowAuthModal(false);
  };

  const handleOpenAuthModal = (e) => {
    e.preventDefault();
    setIsUserDropdownOpen(false);
    setShowAuthModal(true);
  };

  const handleLogout = async () => {
    if (isLoadingAuth || isLoggingOut) return;

    setIsLoggingOut(true);

    try {
      // Close dropdown immediately and clear search
      setIsUserDropdownOpen(false);
      dispatch(setSearchTerm(''));

      // Check if we're on a protected route before logout
      const isProtectedRoute = location.pathname.startsWith('/admin') ||
        location.pathname.startsWith('/customer');

      // Perform logout
      const resultAction = await dispatch(performLogout());

      if (performLogout.fulfilled.match(resultAction)) {
        console.log('Logout successful:', resultAction.payload);

        // Navigate away from protected routes after successful logout
        if (isProtectedRoute) {
          // Add a small delay to ensure state is updated
          setTimeout(() => {
            navigate('/', { replace: true });
          }, 100);
        }
      } else {
        console.error('Logout failed:', resultAction.error || resultAction.payload);
        // Optionally show error message to user
      }
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      setIsLoggingOut(false);
    }
  };

  const handleSearch = (e) => {
    if (e.type === 'submit') {
      e.preventDefault(); // Prevent form submission refresh
      dispatch(fetchProducts());
      return;
    }

    dispatch(setSearchTerm(e.target.value));
    // Debounce search for onChange
    const timeoutId = setTimeout(() => {
      dispatch(fetchProducts());
    }, 300);
    return () => clearTimeout(timeoutId);
  };

  const renderUserDropdown = () => {
    // Show loading state during logout
    if (isLoggingOut) {
      return (
        <li>
          <div className="text-gray-500">Signing out...</div>
        </li>
      );
    }

    if (!isAuthenticated) {
      return (
        <li>
          <button
            onClick={handleOpenAuthModal}
            className="w-full text-left hover:text-blue-600 transition-colors"
          >
            Sign In
          </button>
        </li>
      );
    }

    return (
      <>
        {isAdmin ? (
          <>
            <li>
              <Link
                to="/admin/dashboard"
                className="hover:text-blue-600 transition-colors"
                onClick={() => setIsUserDropdownOpen(false)}
              >
                Admin Dashboard
              </Link>
            </li>
            <li>
              <Link
                to="/admin/products"
                className="hover:text-blue-600 transition-colors"
                onClick={() => setIsUserDropdownOpen(false)}
              >
                Manage Products
              </Link>
            </li>
          </>
        ) : (
          <>
            <li>
              <Link
                to="/customer/profile"
                className="hover:text-blue-600 transition-colors"
                onClick={() => setIsUserDropdownOpen(false)}
              >
                My Profile
              </Link>
            </li>
            <li>
              <Link
                to="/customer/orders"
                className="hover:text-blue-600 transition-colors"
                onClick={() => setIsUserDropdownOpen(false)}
              >
                My Orders
              </Link>
            </li>
          </>
        )}
        <li>
          <button
            onClick={handleLogout}
            className="w-full text-left hover:text-blue-600 transition-colors text-red-600"
            disabled={isLoadingAuth || isLoggingOut}
          >
            {isLoadingAuth || isLoggingOut ? 'Signing Out...' : 'Sign Out'}
          </button>
        </li>
      </>
    );
  };

  // Effect to manage logout success message (toast)
  useEffect(() => {
    if (logoutMessage) {
      setShowLogoutToast(true);
      const timer = setTimeout(() => {
        setShowLogoutToast(false);
        dispatch(clearLogoutMessage());
      }, 2000);
      return () => {
        clearTimeout(timer);
      };
    } else {
      setShowLogoutToast(false);
    }
  }, [logoutMessage, dispatch]);

  // Effect to handle clicks outside the user dropdown
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (userDropdownRef.current && !userDropdownRef.current.contains(event.target)) {
        setIsUserDropdownOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  // Close dropdown when authentication state changes
  useEffect(() => {
    setIsUserDropdownOpen(false);
    setIsLoggingOut(false);
  }, [isAuthenticated]);

  // Initial authentication check and user role fetch
  useEffect(() => {
    const checkAuthStatusAndFetchRole = async () => {
      // Don't run this check if we're currently logging out
      if (isLoggingOut) return;

      if (!isAuthenticated) {
        try {
          const response = await fetch('/api/auth/user-role', {
            credentials: 'include',
            headers: {
              'Accept': 'application/json'
            }
          });

          if (response.ok) {
            const data = await response.json();
            if (data.role && data.role !== 'ANONYMOUS') {
              dispatch(setLoggedIn({ role: data.role }));
              console.log('User session verified, role:', data.role);
            } else {
              dispatch(logoutReducer());
              console.log('User not authenticated, Redux state cleared.');
            }
          } else if (response.status === 401 || response.status === 403) {
            dispatch(logoutReducer());
            console.log('Authentication check failed (401/403), Redux state cleared.');
          } else {
            console.error('Error checking authentication status:', response.status);
          }
        } catch (error) {
          console.error('Network error during auth status check:', error);
          dispatch(logoutReducer());
        }
      }
    };

    checkAuthStatusAndFetchRole();
  }, [dispatch, isAuthenticated, isLoggingOut]);

  useEffect(() => {
    console.log('Auth State - IsAuthenticated:', isAuthenticated, 'isAdmin:', isAdmin);
  }, [isAuthenticated, isAdmin]);

  return (
    <header className="bg-white shadow-md">
      {/* Logout Success Toast/Message */}
      {showLogoutToast && logoutMessage && (
        <div className="fixed top-0 left-1/2 -translate-x-1/2 mt-4 bg-green-500 text-white px-6 py-3 rounded-md shadow-lg z-50 animate-fade-in-down">
          {logoutMessage}
        </div>
      )}

      <div className="py-4 shadow-md">
        <ul className="container mx-auto flex flex-wrap justify-between md:flex-row px-4 md:px-2 items-center relative">
          <div className="flex items-center">
            {/* Regular navigation */}
            <div className="flex gap-4">
              <li><Link to="/" className="hover:text-blue-600 transition-colors">Home</Link></li>
              <li><Link to="/about" className="hover:text-blue-600 transition-colors">About</Link></li>
              <li><Link to="/faqs" className="hover:text-blue-600 transition-colors">FAQs</Link></li>
              <li><Link to="/contact" className="hover:text-blue-600 transition-colors">Contact</Link></li>
            </div>

            {/* Admin Navigation */}
            {isAdmin && <AdminNav />}
          </div>

          {/* User Menu */}
          <div className="relative" ref={userDropdownRef}>
            <button
              onClick={handleUserIconClick}
              className="flex items-center gap-2 bg-gray-200 p-2 rounded hover:bg-gray-300 transition-colors"
              disabled={isLoggingOut}
            >
              <User size={24} />
              <span className="text-sm">
                {isLoggingOut
                  ? 'Signing Out...'
                  : isAuthenticated
                    ? (isAdmin ? 'Admin' : 'Account')
                    : 'Sign In'
                }
              </span>
            </button>

            <div
              className={`flex flex-col absolute right-0 md:right-0 top-12 z-20 bg-white shadow-lg border rounded-md p-4 gap-4 min-w-[150px] ${isUserDropdownOpen ? "block" : "hidden"
                }`}
            >
              {renderUserDropdown()}
            </div>
          </div>
        </ul>
      </div>

      <nav className="flex justify-between items-center container mx-auto md:py-6 py-8 px-2">
        <div className="flex items-center">
          <Link to="/" className="flex items-center bg-gray-700 py-2 px-2 rounded hover:bg-gray-800 transition-colors">
            <img src={Logo} alt="TechShop" className="h-8 md:h-10 w-auto object-contain" />
          </Link>
        </div>

        {/* Desktop Search */}
        <form className="hidden md:block w-1/2" onSubmit={handleSearch}>
          <input
            type="text"
            placeholder="Search Product"
            className="bg-zinc-100 rounded-md border border-zinc-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent py-3 px-3 w-full transition-all"
            value={searchTerm}
            onChange={handleSearch}
          />
        </form>

        <div className="relative">
          <Link to="/cart">
            <ShoppingCart size={54} className="cursor-pointer bg-gray-100 px-3 py-2 rounded-full hover:bg-gray-200 transition-colors" />
            {itemCount > 0 && (
              <span className="absolute -top-2 -right-2 bg-blue-600 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-medium">
                {itemCount}
              </span>
            )}
          </Link>
        </div>
      </nav>

      {/* Mobile Search Bar - Always visible on mobile */}
      <div className="md:hidden px-4 py-3 border-t">
        <form onSubmit={handleSearch}>
          <input
            type="text"
            placeholder="Search Product"
            className="w-full bg-zinc-100 rounded-md border border-zinc-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent py-2 px-3 transition-all"
            value={searchTerm}
            onChange={handleSearch}
          />
        </form>
      </div>

      {/* Auth Modal */}
      {showAuthModal &&
        ReactDOM.createPortal(
          <div
            className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4"
            onClick={handleCloseAuthModal}
          >
            <div
              onClick={(e) => e.stopPropagation()}
              className="relative bg-white rounded-xl shadow-xl p-6 w-[95%] max-w-md sm:p-8 max-h-full overflow-y-auto"
            >
              <AuthForms onClose={handleCloseAuthModal} />
            </div>
          </div>,
          document.body
        )}
    </header>
  );
}

export default Navbar;