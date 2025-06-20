import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { fetchProducts } from "../../features/Products/productSlice";
import { ShoppingBag, Users, Package, DollarSign } from 'lucide-react';

const Dashboard = () => {
  const dispatch = useDispatch();
  
  const { items, loading = false, error = null } = useSelector((state) => state.product);

  useEffect(() => {
    dispatch(fetchProducts());
  }, [dispatch]);

  const totalProducts = items.length;

  const stats = [
    { label: 'Total Orders', value: '150', icon: ShoppingBag, color: 'bg-blue-500' },
    { label: 'Total Users', value: '1,234', icon: Users, color: 'bg-green-500' },
    { label: 'Total Products', value: totalProducts, icon: Package, color: 'bg-purple-500' },
    { label: 'Revenue', value: '$12,345', icon: DollarSign, color: 'bg-yellow-500' },
  ];

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-6">Admin Dashboard</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
        {stats.map((stat, index) => (
          <div key={index} className="bg-white rounded-lg shadow-md p-6">
            <div className="flex items-center">
              <div className={`${stat.color} p-3 rounded-lg`}>
                <stat.icon className="w-6 h-6 text-white" />
              </div>
              <div className="ml-4">
                <p className="text-gray-500 text-sm">{stat.label}</p>
                <h3 className="text-xl font-bold">{stat.value}</h3>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-lg font-semibold mb-4">Recent Orders</h2>
          {/* Render real recent orders if available */}
        </div>
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-lg font-semibold mb-4">Latest Products</h2>
          <ul>
            {items.slice(0, 5).map((product) => (
              <li key={product.id} className="border-b py-2">
                {product.name}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;