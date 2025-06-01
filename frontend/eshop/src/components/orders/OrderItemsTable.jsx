import PropTypes from 'prop-types';

const OrderItemsTable = ({ items }) => (
  <div className="border rounded-lg overflow-x-auto">
    <div className="max-h-[300px] overflow-y-auto min-w-[600px]">
      <table className="w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase w-[40%]">Product</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase w-[20%]">Price</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase w-[20%]">Quantity</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase w-[20%]">Total</th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {items.map((item) => (
            <tr key={item.orderItemId}>
              <td className="px-4 py-4 whitespace-nowrap">
                <div className="flex items-center space-x-3 max-w-xs">
                  <img src={item.product.imageUrl} alt={item.product.name} className="w-10 h-10 object-cover rounded flex-shrink-0" loading="lazy" />
                  <p className="font-medium truncate">{item.product.name}</p>
                </div>
              </td>
              <td className="px-4 py-4 whitespace-nowrap">${item.unitPrice.toFixed(2)}</td>
              <td className="px-4 py-4 whitespace-nowrap">{item.quantity}</td>
              <td className="px-4 py-4 whitespace-nowrap">${item.total.toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  </div>
);

OrderItemsTable.propTypes = {
  items: PropTypes.arrayOf(
    PropTypes.shape({
      orderItemId: PropTypes.string.isRequired,
      product: PropTypes.shape({
        name: PropTypes.string.isRequired,
        imageUrl: PropTypes.string.isRequired,
      }).isRequired,
      unitPrice: PropTypes.number.isRequired,
      quantity: PropTypes.number.isRequired,
      total: PropTypes.number.isRequired,
    })
  ).isRequired,
};

export default OrderItemsTable;