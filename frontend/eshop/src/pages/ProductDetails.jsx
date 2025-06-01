import { useParams, Link } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { ShoppingCart } from "lucide-react";
import { addToCart, addToCartAsync } from "../features/Cart/cartSlice";

function ProductDetails() {
  const { id } = useParams();
  const dispatch = useDispatch();
  const isAuthenticated = useSelector(state => state.auth.isAuthenticated);

  const product = useSelector((state) =>
    state.product.items.find((p) => p.id === parseInt(id))
  );

  const handleAddToCart = async () => {
    if (isAuthenticated) {
      try {
        await dispatch(addToCartAsync(product)).unwrap();
      } catch (error) {
        console.error('Failed to add to cart:', error);
      }
    } else {
      dispatch(addToCart(product));
    }
  };

  if (!product) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <h2 className="text-2xl font-bold mb-4">Product Not Found</h2>
          <Link to="/" className="text-blue-600 hover:text-blue-800">
            Return to Home
          </Link>
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
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 items-start">
          <div className="w-full max-w-[600px] mx-auto bg-white shadow-md p-4 rounded">
            <div className="relative pb-[75%]"> {/* Creates a 4:3 aspect ratio container */}
              <img
                src={product.imageUrl}
                alt={product.name}
                className="absolute inset-0 w-full h-full object-contain rounded-lg"
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
              className="w-full md:w-auto bg-zinc-200 px-8 py-3 rounded-md flex items-center justify-center gap-2 hover:bg-zinc-300 active:scale-105 transition-all ease-in"
              onClick={handleAddToCart}
            >
              <ShoppingCart size={20} />
              Add to Cart
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductDetails;