import { Link } from "react-router-dom";
import React from "react";
import { Facebook, Instagram, Twitter, Youtube } from "lucide-react";
import Logo from "../assets/imgs/showcase/logo.svg";

function Footer() {
  return (
    <footer className="bg-slate-900 shadow-md">
      <div className="container mx-auto px-4">
        <div className="min-h-16">
          <div className="flex flex-col md:flex-row justify-between items-center py-10">
            <h2 className="text-4xl font-bold text-white">
              Stay in the Loop - Sign Up Now!
            </h2>
            <form className="md:w-1/3 w-full mt-8 md:mt-0 relative">
              <input
                type="text"
                placeholder="Enter Your Email"
                className="py-4 px-4 rounded shadow-md w-full "
              />
              <button className="bg-gray-200 py-3 px-4 rounded-full absolute right-3 top-1">
                Submit
              </button>
            </form>
          </div>
        </div>
      </div>
      <div className="bg-slate-800 text-white py-8">
        <div className="container mx-auto px-4">
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4 ">
            <div>
              <img src={Logo} alt="" className="my-4" />
              <p>
                Welcome to TechShop, your tech destination for the latest electronic devices and accessories.
              </p>
              <div className="flex  items-center gap-8 mt-5">
                <Facebook
                  size={40}
                  className="bg-white text-black rounded-md p-2"
                />
                <Twitter
                  size={40}
                  className="bg-white text-black rounded-md p-2"
                />
                <Youtube
                  size={40}
                  className="bg-white text-black rounded-md p-2"
                />
                <Instagram
                  size={40}
                  className="bg-white text-black rounded-md p-2"
                />
              </div>
            </div>
            <div>
              <h2 className="text-2xl font-semibold my-4">Pages</h2>
              <ul>
                <li>
                  <Link>Home</Link>
                </li>
                <li>
                  <Link>About</Link>
                </li>
                <li>
                  <Link>FAQs</Link>
                </li>
                <li>
                  <Link>Contact</Link>
                </li>
              </ul>
            </div>
            <div>
              <h2 className="text-2xl font-semibold my-4">Categories</h2>
              <ul>
                <li>
                  <Link>Macbook</Link>
                </li>
                <li>
                  <Link>Dell</Link>
                </li>
                <li>
                  <Link>Lenovo</Link>
                </li>
                <li>
                  <Link>Others</Link>
                </li>
              </ul>
            </div>
            <div>
              <h2 className="text-2xl font-semibold my-4">Contact Info</h2>
              <p>
                International University - Room A1.604
              </p>
              <p>+12345 678 910 </p>
              <p>+10987 654 321 </p>
            </div>
          </div>
        </div>
      </div>
      <div className="container mx-auto text-center py-4 text-white">
        <p>Copyright &copy; 2025</p>
      </div>
    </footer>
  );
}

export default Footer;
