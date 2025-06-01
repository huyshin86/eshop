import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchAdminProducts,
  deleteAdminProduct,
  updateAdminProductStock,
} from "../../features/Products/productSlice";
import { useNavigate } from "react-router-dom";
import { Plus, Search, Edit, Trash2, ArrowUpCircle, Circle } from 'lucide-react';

export default function Products() {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const { adminProducts = { content: [], totalPages: 0, totalElements: 0 },
    adminLoading = false,
    adminError = null
  } = useSelector((state) => state.product);

  const products = adminProducts?.content || [];

  const [page, setPage] = useState(0);
  const size = 20;

  const [editingStockId, setEditingStockId] = useState(null);
  const [stockValue, setStockValue] = useState("");
  const [searchTerm, setSearchTerm] = useState("");

  // Filter products based on search term (after searchTerm is declared)
  const filteredProducts = products.filter(product =>
    product.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    product.category?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    product.id?.toString().includes(searchTerm) ||
    product.stockQuantity?.toString().includes(searchTerm)
  );

  useEffect(() => {
    dispatch(fetchAdminProducts({ page, size }));
  }, [dispatch, page]);

  const handleDelete = (id) => {
    if (window.confirm("Are you sure you want to delete this product?")) {
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
    setStockValue(prod.stockQuantity?.toString() || "0");
  };

  const cancelEditStock = () => {
    setEditingStockId(null);
    setStockValue("");
  };

  const saveStock = (id) => {
    const newStock = parseInt(stockValue, 10);
    if (isNaN(newStock) || newStock < 0) {
      alert("Stock must be a valid non-negative number.");
      return;
    }
    dispatch(updateAdminProductStock({ id, stock: newStock })).then(() => {
      setEditingStockId(null);
      setStockValue("");
    });
  };

  // Debug log to check product data structure
  console.log('Products data:', products);
  console.log('First product:', products[0]);

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Product Management</h1>
        <div className="flex items-center gap-4">
          {/* Search Input */}
          <div className="relative">
            <Search className="w-5 h-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="Search products..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <button
            onClick={handleAdd}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-700"
          >
            <Plus className="w-5 h-5" />
            Add Product
          </button>
        </div>
      </div>

      {adminLoading && <p>Loading products…</p>}
      {adminError && <p className="text-red-600">Error: {adminError}</p>}

      {/* Search Results Summary */}
      {searchTerm && (
        <div className="mb-4 text-sm text-gray-600">
          Showing {filteredProducts.length} of {products.length} products
        </div>
      )}

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
                Image
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                Status
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {filteredProducts.map((product) => (
              <tr key={product.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap">{product.id}</td>
                <td className="px-6 py-4 whitespace-nowrap">{product.name}</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  ${Number(product.price || 0).toLocaleString()}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {editingStockId === product.id ? (
                    <div className="flex items-center gap-2">
                      <input
                        type="number"
                        value={stockValue}
                        onChange={(e) => setStockValue(e.target.value)}
                        className="w-20 border rounded px-2 py-1"
                        min="0"
                      />
                      <button
                        onClick={() => saveStock(product.id)}
                        className="text-green-600 hover:text-green-800"
                        title="Save"
                      >
                        <ArrowUpCircle className="w-5 h-5" />
                      </button>
                      <button
                        onClick={cancelEditStock}
                        className="text-gray-400 hover:text-gray-600 text-xs"
                      >
                        Cancel
                      </button>
                    </div>
                  ) : (
                    <div className="flex items-center gap-2">
                      <span className="font-medium">
                        {product.stockQuantity !== undefined && product.stockQuantity !== null
                          ? product.stockQuantity
                          : 'N/A'}
                      </span>
                      <button
                        onClick={() => startEditStock(product)}
                        className="text-blue-600 hover:text-blue-800"
                        title="Edit stock"
                      >
                        <ArrowUpCircle className="w-4 h-4" />
                      </button>
                    </div>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">{product.category}</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {product.imageUrl ? (
                    <img
                      src={product.imageUrl}
                      alt={product.name}
                      className="w-16 h-16 object-cover rounded"
                    />
                  ) : (
                    <div className="w-16 h-16 bg-gray-200 rounded flex items-center justify-center text-gray-500 text-xs">
                      No Image
                    </div>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center gap-2">
                    <Circle
                      className={`w-3 h-3 ${product.isActive
                          ? "fill-green-500 text-green-500"
                          : "fill-gray-300 text-gray-300"
                        }`}
                    />
                    <span className={`text-sm ${product.isActive ? "text-green-700" : "text-gray-500"
                      }`}>
                      {product.isActive ? "Active" : "Inactive"}
                    </span>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex gap-3">
                    <button
                      onClick={() => handleEdit(product.id)}
                      className="text-blue-600 hover:text-blue-800"
                      title="Edit product"
                    >
                      <Edit className="w-5 h-5" />
                    </button>
                    <button
                      onClick={() => handleDelete(product.id)}
                      className="text-red-600 hover:text-red-800"
                      title="Delete product"
                    >
                      <Trash2 className="w-5 h-5" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}

            {filteredProducts.length === 0 && !adminLoading && (
              <tr>
                <td
                  colSpan="8"
                  className="px-6 py-4 text-center text-gray-500"
                >
                  {searchTerm ? `No products found matching "${searchTerm}"` : "No products found."}
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
          className={`px-3 py-1 rounded ${page === 0
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
          className={`px-3 py-1 rounded ${page + 1 >= (adminProducts.totalPages || 1)
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