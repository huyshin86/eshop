import { useSelector, useDispatch } from "react-redux";
import { Link, useNavigate } from "react-router-dom";
import { Trash2, Plus, Minus, AlertCircle, Loader2, X } from "lucide-react";
import { removeFromCart, updateQuantity, removeFromCartAsync, updateCartQuantityAsync, clearError, initiateCheckout } from "../features/Cart/cartSlice";
import { useEffect, useRef, useState } from 'react';
import ReactDOM from 'react-dom';
import AuthForms from '../pages/AuthForms';
import PropTypes from 'prop-types';

// Payment Method Modal Component
const PaymentMethodModal = ({ isOpen, onClose, onSelectPayment, loading }) => {
  const [selectedMethod, setSelectedMethod] = useState('paypal');

  const paymentMethods = [
    {
      id: 'paypal',
      name: 'PayPal',
      description: 'Pay securely with your PayPal account',
      icon: (
        <img
          src="https://www.paypalobjects.com/paypal-ui/logos/svg/paypal-mark-color.svg"
          alt="PayPal"
          className="w-8 h-8 object-contain"
        />
      ),
      available: true
    },
    // Future payment methods can be added here
    // {
    //   id: 'stripe',
    //   name: 'Credit Card',
    //   description: 'Pay with Visa, Mastercard, or American Express',
    //   icon: <CreditCard className="w-8 h-8 text-gray-600" />,
    //   available: false
    // }
  ];

  const handleContinue = () => {
    onSelectPayment(selectedMethod);
  };

  if (!isOpen) return null;

  return ReactDOM.createPortal(
    <div
      className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4"
      onClick={onClose}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className="relative bg-white rounded-xl shadow-xl p-6 w-[95%] max-w-md max-h-[90vh] overflow-y-auto"
      >
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-xl font-bold">Select Payment Method</h3>
          <button
            onClick={onClose}
            className="p-1 hover:bg-gray-100 rounded-full transition-colors"
            disabled={loading}
          >
            <X size={20} />
          </button>
        </div>

        {/* Payment Methods */}
        <div className="space-y-3 mb-6">
          {paymentMethods.map((method) => (
            <div key={method.id}>
              <label
                className={`
                  flex items-center p-4 border-2 rounded-lg cursor-pointer transition-all
                  ${selectedMethod === method.id
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-gray-200 hover:border-gray-300'
                  }
                  ${!method.available ? 'opacity-50 cursor-not-allowed' : ''}
                `}
              >
                <input
                  type="radio"
                  name="paymentMethod"
                  value={method.id}
                  checked={selectedMethod === method.id}
                  onChange={(e) => setSelectedMethod(e.target.value)}
                  disabled={!method.available}
                  className="sr-only"
                />
                <div className="flex items-center w-full">
                  {method.icon}
                  <div className="ml-3 flex-1">
                    <div className="flex items-center">
                      <span className="font-medium">{method.name}</span>
                      {!method.available && (
                        <span className="ml-2 text-xs bg-gray-200 text-gray-600 px-2 py-1 rounded">
                          Coming Soon
                        </span>
                      )}
                    </div>
                    <p className="text-sm text-gray-600 mt-1">{method.description}</p>
                  </div>
                  <div className={`
                    w-4 h-4 rounded-full border-2 flex items-center justify-center
                    ${selectedMethod === method.id
                      ? 'border-blue-500 bg-blue-500'
                      : 'border-gray-300'
                    }
                  `}>
                    {selectedMethod === method.id && (
                      <div className="w-2 h-2 bg-white rounded-full"></div>
                    )}
                  </div>
                </div>
              </label>
            </div>
          ))}
        </div>

        {/* Actions */}
        <div className="flex gap-3">
          <button
            onClick={onClose}
            className="flex-1 px-4 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            disabled={loading}
          >
            Cancel
          </button>
          <button
            onClick={handleContinue}
            disabled={loading || !paymentMethods.find(m => m.id === selectedMethod)?.available}
            className="flex-1 bg-zinc-200 px-4 py-3 rounded-lg hover:bg-zinc-300 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? (
              <div className="flex items-center justify-center">
                <Loader2 className="animate-spin mr-2" size={16} />
                Processing...
              </div>
            ) : (
              'Continue to Payment'
            )}
          </button>
        </div>
      </div>
    </div>,
    document.body
  );
};

PaymentMethodModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onSelectPayment: PropTypes.func.isRequired,
  loading: PropTypes.bool
};

function CartPage() {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { items: cartItems, loading, error, totalPrice } = useSelector((state) => state.cart);
  const isAuthenticated = useSelector((state) => state.auth.isAuthenticated);
  const wasAuthenticated = useRef(isAuthenticated);
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [showPaymentModal, setShowPaymentModal] = useState(false);

  useEffect(() => {
    if (wasAuthenticated.current && !isAuthenticated) {
      navigate('/', { replace: true });
    }
    wasAuthenticated.current = isAuthenticated;
  }, [isAuthenticated, navigate]);

  // Calculate totals
  const subtotal = totalPrice || cartItems.reduce((sum, item) => {
    const price = item.price || 0;
    return sum + (price * item.quantity);
  }, 0);

  const shipping = cartItems.length > 0 ? 20 : 0;
  const tax = subtotal * 0.1;
  const total = subtotal + shipping + tax;

  const handleQuantityUpdate = async (itemId, newQuantity) => {
    if (newQuantity === 0) {
      handleRemoveItem(itemId);
      return;
    }

    if (isAuthenticated) {
      try {
        await dispatch(updateCartQuantityAsync({ id: itemId, quantity: newQuantity })).unwrap();
      } catch (error) {
        const errorMessage = error?.errors ? Object.values(error.errors)[0] : error;
        alert(errorMessage);
      }
    } else {
      dispatch(updateQuantity({ id: itemId, quantity: newQuantity }));
    }
  };

  const handleRemoveItem = async (itemId) => {
    if (isAuthenticated) {
      try {
        await dispatch(removeFromCartAsync(itemId)).unwrap();
      } catch (error) {
        console.error('Failed to remove item:', error);
      }
    } else {
      dispatch(removeFromCart(itemId));
    }
  };

  const handleClearError = () => {
    dispatch(clearError());
  };

  const handleCheckout = () => {
    if (!isAuthenticated) {
      setShowAuthModal(true);
      return;
    }

    // Show payment method selection modal
    setShowPaymentModal(true);
  };

  const handlePaymentMethodSelect = async (paymentMethod) => {
    try {
      if (paymentMethod === 'paypal') {
        const approvalUrl = await dispatch(initiateCheckout()).unwrap();

        // If successful, redirect to the payment URL
        if (approvalUrl) {
          window.location.href = approvalUrl;
        }
      }
      setShowPaymentModal(false);
    } catch (error) {
      console.error('Checkout failed:', error);
      // Keep modal open on error so user can try again
    }
  };

  const handleCloseAuthModal = () => {
    setShowAuthModal(false);
  };

  const handleClosePaymentModal = () => {
    setShowPaymentModal(false);
  };

  // Loading state
  if (loading && cartItems.length === 0) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <Loader2 className="animate-spin mx-auto mb-4" size={48} />
          <p className="text-gray-600">Loading your cart...</p>
        </div>
      </div>
    );
  }

  // Empty cart state
  if (cartItems.length === 0) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <h2 className="text-2xl font-bold mb-4">Your Cart is Empty</h2>
          <p className="text-gray-600 mb-4">
            Add some products to your cart to see them here.
          </p>
          <Link
            to="/"
            className="inline-block bg-zinc-200 px-6 py-2 rounded-lg hover:bg-zinc-300 transition-colors"
          >
            Continue Shopping
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Auth Modal */}
      {showAuthModal &&
        ReactDOM.createPortal(
          <div
            className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4"
            onClick={handleCloseAuthModal}
          >
            <div
              onClick={(e) => e.stopPropagation()}
              className="relative bg-white rounded-xl shadow-xl p-6 w-[95%] max-w-md sm:p-8 max-h-[90vh] overflow-y-auto"
            >
              <AuthForms onClose={handleCloseAuthModal} />
            </div>
          </div>,
          document.body
        )}

      {/* Payment Method Modal */}
      <PaymentMethodModal
        isOpen={showPaymentModal}
        onClose={handleClosePaymentModal}
        onSelectPayment={handlePaymentMethodSelect}
        loading={loading}
      />

      <h2 className="text-2xl font-bold mb-8">Shopping Cart</h2>

      {/* Error Message */}
      {error && (
        <div className="mb-6 bg-red-50 border border-red-200 rounded-md p-4 flex items-center justify-between">
          <div className="flex items-center">
            <AlertCircle className="text-red-500 mr-2" size={20} />
            <span className="text-red-700">{error}</span>
          </div>
          <button
            onClick={handleClearError}
            className="text-red-500 hover:text-red-700 text-sm"
          >
            Dismiss
          </button>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Cart Items */}
        <div className="lg:col-span-2 shadow-md p-4 rounded-md relative">
          {loading && (
            <div className="absolute inset-0 bg-white/70 flex items-center justify-center rounded-md">
              <Loader2 className="animate-spin" size={24} />
            </div>
          )}

          {cartItems.map((item) => {
            if (!item) return null;

            // Use consistent item identification
            const itemId = item.id; // Use item.id for both authenticated and unauthenticated users
            const productId = item.id; // Product ID is always item.id after normalization
            const title = item.title || item.name;
            const price = item.price;
            const image = item.imageUrl;
            const quantity = item.quantity || 0;
            const isAvailable = item.isAvailableInStock !== false;

            if (!itemId || !productId || !title || typeof price === 'undefined') {
              return null;
            }

            return (
              <div
                key={itemId}
                className={`flex items-center gap-4 py-4 border-b ${!isAvailable ? 'opacity-60' : ''}`}
              >
                <Link to={`/product/${productId}`}>
                  <img
                    src={image}
                    alt={title}
                    className="w-24 h-24 object-cover rounded"
                    loading="lazy"
                  />
                </Link>

                <div className="flex-1">
                  <Link
                    to={`/product/${productId}`}
                    className="font-semibold hover:text-blue-600 transition-colors"
                  >
                    {title}
                  </Link>
                  <p className="text-gray-600">${price.toFixed(2)}</p>

                  {!isAvailable && (
                    <p className="text-red-500 text-sm">Out of stock</p>
                  )}

                  <div className="flex items-center gap-2 mt-2">
                    <button
                      onClick={() => handleQuantityUpdate(itemId, quantity - 1)}
                      className="p-1 rounded-full hover:bg-gray-100 transition-colors disabled:opacity-50"
                      disabled={loading || quantity <= 1}
                    >
                      <Minus size={16} />
                    </button>
                    <span className="min-w-[2rem] text-center">{quantity}</span>
                    <button
                      onClick={() => handleQuantityUpdate(itemId, quantity + 1)}
                      className="p-1 rounded-full hover:bg-gray-100 transition-colors disabled:opacity-50"
                      disabled={loading || !isAvailable}
                    >
                      <Plus size={16} />
                    </button>
                    <button
                      onClick={() => handleRemoveItem(itemId)}
                      className="ml-4 text-red-500 hover:text-red-700 transition-colors disabled:opacity-50"
                      disabled={loading}
                    >
                      <Trash2 size={20} />
                    </button>
                  </div>
                </div>

                <div className="text-right">
                  <p className="font-bold">
                    ${(price * quantity).toFixed(2)}
                  </p>
                </div>
              </div>
            );
          })}
        </div>

        {/* Order Summary */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-lg shadow-md p-6 sticky top-4">
            <h3 className="text-xl font-bold mb-4">Order Summary</h3>
            <div className="space-y-2 mb-4">
              <div className="flex justify-between">
                <span>Subtotal</span>
                <span>${subtotal.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span>Shipping</span>
                <span>${shipping.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span>Tax (10%)</span>
                <span>${tax.toFixed(2)}</span>
              </div>
              <div className="border-t pt-2 font-bold">
                <div className="flex justify-between">
                  <span>Total</span>
                  <span>${total.toFixed(2)}</span>
                </div>
              </div>
            </div>
            <button
              className="w-full bg-zinc-200 px-6 py-3 rounded-lg hover:bg-zinc-300 transition-colors disabled:opacity-50"
              disabled={loading || cartItems.some(item => !item.isAvailableInStock)}
              onClick={handleCheckout}
            >
              {loading ? (
                <div className="flex items-center justify-center">
                  <Loader2 className="animate-spin mr-2" size={16} />
                  Processing...
                </div>
              ) : (
                'Proceed to Checkout'
              )}
            </button>

            {cartItems.some(item => !item.isAvailableInStock) && (
              <p className="text-red-500 text-sm mt-2 text-center">
                Remove out-of-stock items to proceed
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default CartPage;