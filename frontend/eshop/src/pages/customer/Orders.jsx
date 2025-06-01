import { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { ArrowUpDown } from 'lucide-react';
import OrderDetails from '../../components/orders/OrderDetails';

const Orders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedOrder, setSelectedOrder] = useState(null);

  const [sortConfig, setSortConfig] = useState({ key: 'orderDate', direction: 'desc' });

  const sortedOrders = [...orders].sort((a, b) => {
    if (sortConfig.key === 'orderDate') {
      return sortConfig.direction === 'asc'
        ? new Date(a.orderDate) - new Date(b.orderDate)
        : new Date(b.orderDate) - new Date(a.orderDate);
    }
    if (sortConfig.key === 'grandTotal') {
      return sortConfig.direction === 'asc'
        ? a.grandTotal - b.grandTotal
        : b.grandTotal - a.grandTotal;
    }
    return sortConfig.direction === 'asc'
      ? a[sortConfig.key].localeCompare(b[sortConfig.key])
      : b[sortConfig.key].localeCompare(a[sortConfig.key]);
  });

  const requestSort = (key) => {
    setSortConfig((current) => ({
      key,
      direction: current.key === key && current.direction === 'asc' ? 'desc' : 'asc',
    }));
  };

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const response = await fetch('/api/user/me/orders', {
          credentials: 'include'
        });
        const result = await response.json();

        if (response.ok) {
          setOrders(result.data.orders);
        } else {
          setError(result.message);
        }
      } catch {
        setError('Failed to load orders');
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, []);

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">Loading orders...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-red-500 text-center">{error}</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">My Orders</h1>
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <div className="overflow-x-auto">
          <div className="min-w-[768px] max-h-[500px] overflow-y-auto">
            <table className="w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase w-[20%]">
                    Order #
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase w-[25%]">
                    <button
                      type="button"
                      className="flex items-center gap-1 hover:text-gray-700"
                      onClick={() => requestSort('orderDate')}
                    >
                      Date <ArrowUpDown size={14} />
                    </button>
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase w-[20%]">
                    <button
                      type="button"
                      className="flex items-center gap-1 hover:text-gray-700"
                      onClick={() => requestSort('orderStatus')}
                    >
                      Status <ArrowUpDown size={14} />
                    </button>
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase w-[15%]">
                    <button
                      type="button"
                      className="flex items-center gap-1 hover:text-gray-700"
                      onClick={() => requestSort('grandTotal')}
                    >
                      Total <ArrowUpDown size={14} />
                    </button>
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase w-[20%]">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {sortedOrders.map((order) => (
                  <tr key={order.orderId}>
                    <td className="px-6 py-4 whitespace-nowrap">{order.orderNumber}</td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {format(new Date(order.orderDate), 'PPp')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">
                        {order.orderStatus}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      ${order.grandTotal.toFixed(2)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <button
                        onClick={() => setSelectedOrder(order)}
                        className="text-blue-600 font-bold hover:text-blue-800"
                      >
                        View Details
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {selectedOrder && (
        <OrderDetails
          order={selectedOrder}
          onClose={() => setSelectedOrder(null)}
        />
      )}
    </div>
  );
};

export default Orders;