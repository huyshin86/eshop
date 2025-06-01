import { Link } from 'react-router-dom';
import { AlertCircle } from 'lucide-react';

function CheckoutError() {
    return (
        <div className="min-h-screen flex items-center justify-center">
            <div className="text-center">
                <div className="bg-red-100 p-4 rounded-full inline-block mb-4">
                    <AlertCircle className="text-red-600" size={48} />
                </div>
                <h1 className="text-2xl font-bold mb-2">Payment Failed</h1>
                <p className="text-gray-600 mb-4">
                    We couldn&apos;t process your payment. Please try again.
                </p>
                <Link
                    to="/cart"
                    className="text-blue-600 hover:text-blue-800 transition-colors"
                >
                    Return to Cart
                </Link>
            </div>
        </div>
    );
}

export default CheckoutError;