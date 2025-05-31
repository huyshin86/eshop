import React from 'react';

const OrderItemsTable = ({ items }) => (
  <div className="border rounded-lg overflow-hidden">
    <table className="min-w-full divide-y divide-gray-200">
      <thead className="bg-gray-50">
        <tr>
          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Product</th>
          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Price</th>
          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Quantity</th>
          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Total</th>
        </tr>
      </thead>
      <tbody className="bg-white divide-y divide-gray-200">
        {items.map((item) => (
          <tr key={item.orderItemId}>
            <td className="px-6 py-4">
              <div className="flex items-center">
                <img src={item.product.imageUrl} alt={item.product.name} className="w-12 h-12 object-cover rounded" />
                <div className="ml-4">
                  <p className="font-medium">{item.product.name}</p>
                </div>
              </div>
            </td>
            <td className="px-6 py-4">${item.unitPrice.toFixed(2)}</td>
            <td className="px-6 py-4">{item.quantity}</td>
            <td className="px-6 py-4">${item.total.toFixed(2)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  </div>
);

export default OrderItemsTable;