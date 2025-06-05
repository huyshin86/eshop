import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import {
  createAdminProduct,
  updateAdminProduct,
  fetchAdminProducts,
} from "../../features/Products/productSlice";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import { useMemo } from "react";

export default function ProductForm() {
  const { id } = useParams();
  const isEditing = Boolean(id);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const { adminProducts, adminLoading, adminError } = useSelector(
    (state) => state.product
  );
  const productsList = useMemo(() => adminProducts?.content || [], [adminProducts]);

  // State form
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    price: "",
    stock: "",
    categoryId: 1, // Fixed to category ID 1 (Laptop)
    isActive: true,
    image: null,
  });
  const [previewUrl, setPreviewUrl] = useState(""); // để preview ảnh (khi edit, hoặc khi chọn ảnh mới)
  const [formError, setFormError] = useState(null);

  const fixedCategory = { id: 1, name: "Laptop" };

  useEffect(() => {
    if (isEditing) {
      if (productsList.length === 0) {
        dispatch(fetchAdminProducts());
      } else {
        const prod = productsList.find((p) => p.id === parseInt(id));
        if (prod) {
          setFormData({
            name: prod.name || "",
            description: prod.description || "",
            price: prod.price || "",
            stock: prod.stockQuantity || "",
            categoryId: prod.categoryId || 1, // Default to category ID 1 if not set
            isActive: prod.isActive !== undefined ? prod.isActive : true,
            image: null,
          });
          setPreviewUrl(prod.imageUrl || "");
        }
      }
    }
  }, [isEditing, id, productsList, dispatch]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    setFormData((prev) => ({
      ...prev,
      image: file,
    }));
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewUrl(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError(null);

    if (!formData.name.trim() || !formData.price || !formData.stock) {
      setFormError("Please fill in all required fields.");
      return;
    }
    if (!isEditing && !formData.image) {
      setFormError("Please upload an image for the product.");
      return;
    }

    const payload = new FormData();
    payload.append("name", formData.name);
    payload.append("description", formData.description);
    payload.append("price", formData.price);
    payload.append("stockQuantity", formData.stock);
    payload.append("categoryId", formData.categoryId);
    payload.append("isActive", formData.isActive);
    if (formData.image) {
      payload.append("image", formData.image);
    }
    
    try {
      if (isEditing) {
        await dispatch(updateAdminProduct({ id, formData: payload })).unwrap();
        alert("Update product successfully!");
      } else {
        await dispatch(createAdminProduct(payload)).unwrap();
        alert("Create new product successfully!");
      }
      navigate("/admin/products");
    } catch (err) {
      setFormError(err);
    }
  };

  return (
    <div className="p-6 max-w-3xl mx-auto">
      <button
        onClick={() => navigate("/admin/products")}
        className="flex items-center text-gray-600 hover:text-gray-800 mb-4"
      >
        <ArrowLeft className="w-5 h-5 mr-2" />
        Back to Products
      </button>
      <h2 className="text-2xl font-semibold mb-6">
        {isEditing ? "Edit Product" : "Create New Product"}
      </h2>

      {adminLoading && isEditing && <p>Loading data</p>}
      {adminError && <p className="text-red-600">Lỗi: {adminError}</p>}

      {(!isEditing || productsList.length > 0) && (
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && <p className="text-red-600">{formError}</p>}

          <div>
            <label className="block font-medium">Name of product *</label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              className="mt-1 block w-full border rounded px-3 py-2"
            />
          </div>

          <div>
            <label className="block font-medium">Description</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              className="mt-1 block w-full border rounded px-3 py-2"
              rows={4}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block font-medium">Price *</label>
              <input
                type="number"
                name="price"
                value={formData.price}
                onChange={handleChange}
                className="mt-1 block w-full border rounded px-3 py-2"
              />
            </div>
            <div>
              <label className="block font-medium">Stock *</label>
              <input
                type="number"
                name="stock"
                value={formData.stock}
                onChange={handleChange}
                className="mt-1 block w-full border rounded px-3 py-2"
              />
            </div>
          </div>

          <div>
            <label className="block font-medium">Category</label>
            <input
              type="text"
              value={fixedCategory.name}
              disabled
              className="mt-1 block w-full border rounded px-3 py-2 bg-gray-100 text-gray-600"
            />
            <p className="text-sm text-gray-500 mt-1">Currently only supporting Laptop category</p>
          </div>

          <div>
            <label className="flex items-center">
              <input
                type="checkbox"
                name="isActive"
                checked={formData.isActive}
                onChange={handleChange}
                className="mr-2"
              />
              <span className="font-medium">Product is active</span>
            </label>
          </div>

          <div>
            <label className="block font-medium">Product&apos;s image {isEditing ? "(if change)" : "*"}</label>
            <input
              type="file"
              accept="image/*"
              onChange={handleImageChange}
              className="mt-1 block w-full"
            />
            {previewUrl && (
              <img
                src={previewUrl}
                alt="Preview"
                className="mt-3 w-32 h-32 object-cover border rounded"
              />
            )}
          </div>

          <div className="space-x-2 mt-6">
            <button
              type="submit"
              className="px-6 py-2 bg-green-600 text-white rounded hover:bg-green-700"
            >
              {isEditing ? "Save changes" : "Create product"}
            </button>
            <button
              type="button"
              onClick={() => navigate("/admin/products")}
              className="px-6 py-2 bg-gray-400 text-white rounded hover:bg-gray-500"
            >
              Cancel
            </button>
          </div>
        </form>
      )}
    </div>
  );
}