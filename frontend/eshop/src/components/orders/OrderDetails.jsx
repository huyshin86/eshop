import React from 'react';
import OrderMeta from './OrderMeta';
import OrderItemsTable from './OrderItemsTable';
import OrderSummary from './OrderSummary';

const OrderDetails = ({ order, onClose }) => (
  <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
    <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
      <div className="p-6">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-bold">Order Details #{order.orderNumber}</h2>
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

export default OrderDetails;