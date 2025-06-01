import OrderMeta from './OrderMeta';
import OrderItemsTable from './OrderItemsTable';
import OrderSummary from './OrderSummary';
import PropTypes from 'prop-types';

const OrderDetails = ({ order, onClose }) => (
  <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
    <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
      <div className="p-6">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-bold">Order Details: #{order.orderNumber}</h2>
          <button onClick={onClose} className="text-gray-500 hover:text-gray-700">
            &times;
          </button>
        </div>

        <OrderMeta order={order} />
        <OrderItemsTable items={order.items} />
        <OrderSummary order={order} />
      </div>
    </div>
  </div>
);

OrderDetails.propTypes = {
  order: PropTypes.shape({
    orderNumber: PropTypes.string.isRequired,
    orderDate: PropTypes.string.isRequired,
    orderStatus: PropTypes.string.isRequired,
    shippingAddress: PropTypes.string.isRequired,

    items: PropTypes.arrayOf(
      PropTypes.shape({
        orderItemId: PropTypes.string.isRequired,
        product: PropTypes.shape({
          name: PropTypes.string.isRequired,
          imageUrl: PropTypes.string.isRequired,
        }).isRequired,
        quantity: PropTypes.number.isRequired,
        unitPrice: PropTypes.number.isRequired,
        total: PropTypes.number.isRequired,
      })
    ).isRequired,

    subtotal: PropTypes.number.isRequired,
    shippingCost: PropTypes.number.isRequired,
    tax: PropTypes.number.isRequired,
    discountAmount: PropTypes.number.isRequired,
    grandTotal: PropTypes.number.isRequired,
  }).isRequired,
  onClose: PropTypes.func.isRequired,
};

export default OrderDetails;