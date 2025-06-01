import { format } from 'date-fns';
import PropTypes from 'prop-types';

const OrderMeta = ({ order }) => (
  <div className="grid grid-cols-2 gap-6 mb-6">
    <div>
      <p className="text-gray-600">Order Date: {format(new Date(order.orderDate), 'PPp')}</p>
      <p className="text-gray-600">Status: <span className="font-semibold">{order.orderStatus}</span></p>
    </div>
    <div>
      <p className="text-gray-600">Shipping Address:</p>
      <p className="font-semibold">{order.shippingAddress}</p>
    </div>
  </div>
);

OrderMeta.propTypes = {
  order: PropTypes.shape({
    orderDate: PropTypes.string.isRequired,
    orderStatus: PropTypes.string.isRequired,
    shippingAddress: PropTypes.string.isRequired,
  }).isRequired,
};

export default OrderMeta;