import { useEffect, useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { Loader2 } from 'lucide-react';

function PayPalCallback() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const isProcessingRef = useRef(false);

  useEffect(() => {
    const completeCheckout = async () => {
      try {
        // Prevent redux from processing this action multiple times
        // during the same mount cycle
        // in strict mode
        if (isProcessingRef.current) {
          console.log("[PayPalCallback] Processing already initiated in this mount cycle. Skipping.");
          return;
        }
        isProcessingRef.current = true;

        const token = searchParams.get('token');

        const checkoutStatus = localStorage.getItem(`checkout_${token}`);

        // Prevent double processing
        if (checkoutStatus) {
          navigate(checkoutStatus === 'success' ? '/checkout/success' : '/', { replace: true });
          return;
        }

        // User approved the payment
        if (searchParams.get('success') === 'true') {
          const response = await fetch('/api/checkout/complete', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify({ paypalOrderId: token })
          });

          if (!response.ok) {
            throw new Error('Failed to complete checkout');
          }

          // Store completion status
          localStorage.setItem(`checkout_${token}`, 'success');

          // Navigate to success page
          navigate('/checkout/success', { replace: true });
          return;
        }


        // User canceled the payment
        if (searchParams.get('cancel') === 'true') {
          const response = await fetch('/api/checkout/cancel', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify({ paypalOrderId: token })
          });

          if (!response.ok) {
            throw new Error('Failed to cancel checkout');
          }

          navigate('/', { replace: true });
          return;
        }

        // No success or cancel parameter
        navigate('/', { replace: true });
      } catch (error) {
        console.error('Checkout completion failed:', error);
        navigate('/checkout/error', { replace: true });
      }
    };

    completeCheckout();
  }, [searchParams, navigate, dispatch]);

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="text-center">
        <Loader2 className="animate-spin mx-auto mb-4" size={48} />
        <p className="text-gray-600">Processing your payment...</p>
      </div>
    </div>
  );
}

export default PayPalCallback;