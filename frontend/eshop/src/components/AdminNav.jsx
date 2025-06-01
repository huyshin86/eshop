import { Link } from 'react-router-dom';
import { LayoutDashboard, Package } from 'lucide-react';

const AdminNav = () => {
  return (
    <div className="flex items-center gap-4 ml-6 border-l pl-6 text-gray-700">
      <li className="flex items-center">
        <Link 
          to="/admin/dashboard" 
          className="flex items-center gap-2 hover:text-blue-600 transition-colors font-medium"
        >
          <LayoutDashboard size={18} />
          <span>Dashboard</span>
        </Link>
      </li>
      <li className="flex items-center">
        <Link 
          to="/admin/products" 
          className="flex items-center gap-2 hover:text-blue-600 transition-colors font-medium"
        >
          <Package size={18} />
          <span>Products</span>
        </Link>
      </li>
    </div>
  );
};

export default AdminNav;