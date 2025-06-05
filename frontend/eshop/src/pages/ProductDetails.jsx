import { useParams, Link } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { ShoppingCart, AlertCircle } from "lucide-react";
import { addToCart, addToCartAsync } from "../features/Cart/cartSlice";
import { useEffect } from "react";
import { fetchProducts } from "../features/Products/productSlice";

function ProductDetails() {
  const { id } = useParams();
  const dispatch = useDispatch();
  const isAuthenticated = useSelector(state => state.auth.isAuthenticated);

  const product = useSelector((state) =>
    state.product.items.find((p) => p.id === parseInt(id))
  );

  // Fetch product if not found
  useEffect(() => {
    if (!product) {
      dispatch(fetchProducts());
    }
  }, [dispatch, product]);

  const handleAddToCart = async () => {
    if (!product.isActive) {
      alert("This product is currently not available");
      return;
    }

    if (isAuthenticated) {
      try {
        await dispatch(addToCartAsync(product)).unwrap();
      } catch (error) {
        if (error.status === 403) {
          alert("You don't have permission to perform this action");
        } else {
          alert(error.message || "Failed to add item to cart");
        }
      }
    } else {
      dispatch(addToCart(product));
    }
  };

  if (!product) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <h2 className="text-2xl font-bold mb-4">Loading...</h2>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div>
        <Link to="/" className="mb-8 inline-block">
          ← Back to Products
        </Link>
        {!product.isActive && (
          <div className="mb-6 bg-yellow-50 border border-yellow-200 rounded-md p-4 flex items-center">
            <AlertCircle className="text-yellow-500 mr-2" size={20} />
            <span className="text-yellow-700">This product is currently not available for purchase</span>
          </div>
        )}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 items-start">
          <div className="w-full max-w-[600px] mx-auto bg-white shadow-md p-4 rounded">
            <div className="relative pb-[75%]">
              <img
                src={product.imageUrl}
                alt={product.name}
                className={`absolute inset-0 w-full h-full object-contain rounded-lg ${!product.isActive ? 'opacity-70' : ''}`}
                loading="lazy"
              />
            </div>
          </div>
          <div>
            <h1 className="text-3xl font-bold mb-4">{product.name}</h1>
            <p className="text-gray-600 mb-6">{product.description}</p>
            <div className="mb-6">
              <span className="text-3xl font-bold">${product.price}</span>
            </div>
            <div className="mb-6">
              <h3 className="font-semibold mb-2">Category</h3>
              <span className="inline-block bg-gray-200 rounded-full px-3 py-1 text-sm">
                {product.categoryName}
              </span>
            </div>

            <button
              className={`w-full md:w-auto px-8 py-3 rounded-md flex items-center justify-center gap-2 transition-all ease-in
                ${product.isActive 
                  ? 'bg-zinc-200 hover:bg-zinc-300 active:scale-105' 
                  : 'bg-gray-100 text-gray-400 cursor-not-allowed'}`}
              onClick={handleAddToCart}
              disabled={!product.isActive}
            >
              <ShoppingCart size={20} />
              {product.isActive ? 'Add to Cart' : 'Currently Unavailable'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductDetails;