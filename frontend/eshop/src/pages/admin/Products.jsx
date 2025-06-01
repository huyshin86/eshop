//TODO: Create a popular products page for the admin to manage products

import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchAdminProducts,
  deleteAdminProduct,
  updateAdminProductStock,
} from "../../features/Products/productSlice";
import { useNavigate } from "react-router-dom";
import { Plus, Search, Edit, Trash2, ArrowUpCircle } from 'lucide-react';

export default function Products() {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const { adminProducts, adminLoading, adminError } = useSelector(
    (state) => state.products
  );
  const products = adminProducts.content || [];

  const [page, setPage] = useState(0);
  const size = 20;

  const [editingStockId, setEditingStockId] = useState(null);
  const [stockValue, setStockValue] = useState("");

  useEffect(() => {
    dispatch(fetchAdminProducts({ page, size }));
  }, [dispatch, page]);

  const handleDelete = (id) => {
    if (window.confirm("Bạn có chắc muốn xóa sản phẩm này không?")) {
      dispatch(deleteAdminProduct(id));
    }
  };

  const handleEdit = (id) => {
    navigate(`/admin/products/${id}/edit`);
  };

  const handleAdd = () => {
    navigate("/admin/products/new");
  };

  const startEditStock = (prod) => {
    setEditingStockId(prod.id);
    setStockValue(prod.stock.toString());
  };

  const cancelEditStock = () => {
    setEditingStockId(null);
    setStockValue("");
  };

  const saveStock = (id) => {
    const newStock = parseInt(stockValue, 10);
    if (isNaN(newStock) || newStock < 0) {
      alert("Stock phải là số không âm.");
      return;
    }
    dispatch(updateAdminProductStock({ id, stock: newStock })).then(() => {
      setEditingStockId(null);
      setStockValue("");
    });
  };


  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Product Management</h1>
        <button className="bg-blue-600 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-700">
          <Plus className="w-5 h-5" />
          Add Product
        </button>
      </div>

      {adminLoading && <p>Loading products…</p>}
      {adminError && <p className="text-red-600">Error: {adminError}</p>}

      <div className="bg-white rounded-lg shadow-md overflow-x-auto">
        <table className="w-full min-w-[800px]">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                ID
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                Product
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                Price
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                Stock
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                Category
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {products.map((product) => (
              <tr key={product.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap">{product.id}</td>
                <td className="px-6 py-4 whitespace-nowrap">{product.name}</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {Number(product.price).toLocaleString()} ₫
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {editingStockId === product.id ? (
                    <div className="flex items-center gap-2">
                      <input
                        type="number"
                        value={stockValue}
                        onChange={(e) => setStockValue(e.target.value)}
                        className="w-20 border rounded px-2 py-1"
                      />
                      <button
                        onClick={() => saveStock(product.id)}
                        className="text-green-600 hover:text-green-800"
                      >
                        <ArrowUpCircle className="w-5 h-5" />
                      </button>
                      <button
                        onClick={cancelEditStock}
                        className="text-gray-400 hover:text-gray-600"
                      >
                        Hủy
                      </button>
                    </div>
                  ) : (
                    <div className="flex items-center gap-2">
                      <span>{product.stock}</span>
                      <button
                        onClick={() => startEditStock(product)}
                        className="text-blue-600 hover:text-blue-800"
                      >
                        <ArrowUpCircle className="w-5 h-5" />
                      </button>
                    </div>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">{product.category}</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {product.imageUrl && (
                    <img
                      src={product.imageUrl}
                      alt={product.name}
                      className="w-16 h-16 object-cover rounded"
                    />
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex gap-3">
                    <button
                      onClick={() => handleEdit(product.id)}
                      className="text-blue-600 hover:text-blue-800"
                    >
                      <Edit className="w-5 h-5" />
                    </button>
                    <button
                      onClick={() => handleDelete(product.id)}
                      className="text-red-600 hover:text-red-800"
                    >
                      <Trash2 className="w-5 h-5" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}

            {products.length === 0 && !adminLoading && (
              <tr>
                <td
                  colSpan="7"
                  className="px-6 py-4 text-center text-gray-500"
                >
                  No products found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination controls */}
      <div className="flex justify-center items-center mt-4 space-x-2">
        <button
          onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
          disabled={page === 0}
          className={`px-3 py-1 rounded ${
            page === 0
              ? "bg-gray-200 text-gray-400 cursor-not-allowed"
              : "bg-blue-500 text-white hover:bg-blue-600"
          }`}
        >
          Prev
        </button>
        <span>
          Page {page + 1} / {adminProducts.totalPages || 1}
        </span>
        <button
          onClick={() =>
            setPage((prev) =>
              prev + 1 < (adminProducts.totalPages || 1) ? prev + 1 : prev
            )
          }
          disabled={page + 1 >= (adminProducts.totalPages || 1)}
          className={`px-3 py-1 rounded ${
            page + 1 >= (adminProducts.totalPages || 1)
              ? "bg-gray-200 text-gray-400 cursor-not-allowed"
              : "bg-blue-500 text-white hover:bg-blue-600"
          }`}
        >
          Next
        </button>
      </div>
    </div>
  );
}