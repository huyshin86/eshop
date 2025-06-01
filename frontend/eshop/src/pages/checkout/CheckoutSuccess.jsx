import {useNavigate } from 'react-router-dom';
import { Check } from 'lucide-react';
import { useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { clearCart } from '../../features/Cart/cartSlice';

function CheckoutSuccess() {
    const navigate = useNavigate();
    const dispatch = useDispatch();

    useEffect(() => {
        // Clear any checkout tokens from localStorage
        const keys = Object.keys(localStorage);
        keys.forEach(key => {
            if (key.startsWith('checkout_')) {
                localStorage.removeItem(key);
            }
        });
        dispatch(clearCart());
    }, [dispatch]);

    const handleContinueShopping = () => {
        // Navigate to home page and replace history
        navigate('/', { replace: true });
    };

    return (
        <div className="min-h-screen flex items-center justify-center">
            <div className="text-center">
                <div className="bg-green-100 p-4 rounded-full inline-block mb-4">
                    <Check className="text-green-600" size={48} />
                </div>
                <h1 className="text-2xl font-bold mb-2">Payment Successful!</h1>
                <p className="text-gray-600 mb-4">Thank you for your purchase.</p>
                <button
                    onClick={handleContinueShopping}
                    className="text-blue-600 hover:text-blue-800 transition-colors"
                >
                    Continue Shopping
                </button>
            </div>
        </div>
    );
}

export default CheckoutSuccess;