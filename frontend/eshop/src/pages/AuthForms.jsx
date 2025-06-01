import { useState } from 'react';
import { Eye, EyeOff, User, Mail, Phone, MapPin, Lock } from 'lucide-react';
import { useDispatch } from 'react-redux';
import { setLoggedIn, setAuthError, clearAuthError } from '../features/Auth/authSlice';
import PropTypes from 'prop-types';

const AuthForms = ({ onClose }) => {
  const dispatch = useDispatch();
  const [activeTab, setActiveTab] = useState('login');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [successMessage, setSuccessMessage] = useState('');

  // Login form state
  const [loginData, setLoginData] = useState({
    email: '',
    password: ''
  });

  // Register form state
  const [registerData, setRegisterData] = useState({
    email: '',
    passwordFields: {
      password: '',
      confirmPassword: ''
    },
    firstName: '',
    lastName: '',
    phone: '',
    address: '',
    confirmCode: ''
  });

  // Validation functions (unchanged)
  const validateEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const validatePhone = (phone) => {
    const phoneRegex = /^\d{10,15}$/;
    return phoneRegex.test(phone);
  };

  const validatePassword = (password) => {
    // At least 8 characters, max 32 characters
    if (password.length < 8 || password.length > 32) {
      return 'Password must be between 8 and 32 characters';
    }

    // At least one uppercase letter
    if (!/[A-Z]/.test(password)) {
      return 'Password must contain at least one uppercase letter';
    }

    // At least one lowercase letter
    if (!/[a-z]/.test(password)) {
      return 'Password must contain at least one lowercase letter';
    }

    // At least one number
    if (!/\d/.test(password)) {
      return 'Password must contain at least one number';
    }

    // At least one special character
    if (!/[@#$%^&*+=]/.test(password)) {
      return 'Password must contain at least one special character';
    }

    return null;
  };

  const validateLoginForm = () => {
    const newErrors = {};

    if (!loginData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!validateEmail(loginData.email)) {
      newErrors.email = 'Invalid email format';
    }

    if (!loginData.password.trim()) {
      newErrors.password = 'Password is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const validateRegisterForm = () => {
    const newErrors = {};

    if (!registerData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!validateEmail(registerData.email)) {
      newErrors.email = 'Invalid email format';
    }

    // Password validation
    if (!registerData.passwordFields.password) {
      newErrors.password = 'Password is required';
    } else {
      const passwordError = validatePassword(registerData.passwordFields.password);
      if (passwordError) {
        newErrors.password = passwordError;
      }
    }

    // Confirm password validation
    if (!registerData.passwordFields.confirmPassword) {
      newErrors.confirmPassword = 'Confirm password is required';
    } else if (registerData.passwordFields.password !== registerData.passwordFields.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    if (!registerData.firstName.trim()) {
      newErrors.firstName = 'First name is required';
    } else if (registerData.firstName.trim().length < 2) {
      newErrors.firstName = 'First name must be at least 2 characters long';
    }

    if (!registerData.lastName.trim()) {
      newErrors.lastName = 'Last name is required';
    } else if (registerData.lastName.trim().length < 2) {
      newErrors.lastName = 'Last name must be at least 2 characters long';
    }

    if (!registerData.phone.trim()) {
      newErrors.phone = 'Phone is required';
    } else if (!validatePhone(registerData.phone)) {
      newErrors.phone = 'Phone number must be between 10 and 15 digits';
    }

    if (!registerData.address.trim()) {
      newErrors.address = 'Address is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Handle login form submission
  const handleLoginSubmit = async () => {
    dispatch(clearAuthError());
    setErrors({});
    setSuccessMessage('');
    if (!validateLoginForm()) return;

    setLoading(true);
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(loginData)
      });

      const result = await response.json();

      if (response.ok) {
        console.log('Login successful:', result);
        setSuccessMessage(result.message || 'Login successful!');

        // Fetch user role after successful login
        const roleResponse = await fetch('/api/auth/user-role', {
          credentials: 'include'
        });
        const roleData = await roleResponse.json();

        // Dispatch setLoggedIn with the role
        dispatch(setLoggedIn({
          user: result.data,
          role: roleData.role
        }));

        setTimeout(() => {
          onClose();
        }, 1500);
      } else {
        console.error('Login failed:', result);
        const errorMessage = result.errors?.message || result.message || 'Login failed';
        setErrors({ submit: errorMessage });
        // Fix: change authError to setAuthError
        dispatch(setAuthError(errorMessage));
      }
    } catch (error) {
      console.error('Network or parsing error:', error);
      const errorMessage = 'Network error or invalid response from server. Please try again.';
      setErrors({ submit: errorMessage });
      // Fix: change authError to setAuthError
      dispatch(setAuthError(errorMessage));
    } finally {
      setLoading(false);
    }
  };

  // Handle register form submission
  const handleRegisterSubmit = async () => {
    setErrors({});
    setSuccessMessage('');
    if (!validateRegisterForm()) return;

    setLoading(true);
    try {
      const response = await fetch('/api/auth/register/customer', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(registerData)
      });

      const result = await response.json();

      if (response.ok) {
        // Attempt auto-login after successful registration
        const loginResponse = await fetch('/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include',
          body: JSON.stringify({
            email: registerData.email,
            password: registerData.passwordFields.password
          })
        });

        const loginResult = await loginResponse.json();

        if (loginResponse.ok) {
          // Fetch user role after successful login
          const roleResponse = await fetch('/api/auth/user-role', {
            credentials: 'include'
          });
          const roleData = await roleResponse.json();
          // Update Redux state
          dispatch(setLoggedIn({
            user: loginResult.data,
            role: roleData.role
          }));

          setSuccessMessage('Registration successful! You are now logged in.');

          // Close the modal after a short delay
          setTimeout(() => {
            onClose();
          }, 1500);
        } else {
          // If auto-login fails, show success message and switch to login tab
          setSuccessMessage('Registration successful! Please sign in.');
          setActiveTab('login');
        }
        // Reset form data
        setRegisterData({
          email: '',
          passwordFields: { password: '', confirmPassword: '' },
          firstName: '',
          lastName: '',
          phone: '',
          address: '',
          confirmCode: ''
        });
      } else {
        console.error('Registration failed:', result);
        setErrors({ submit: result.errors?.message || result.message || 'Registration failed' });
      }
    } catch (error) {
      console.error('Network or parsing error:', error);
      setErrors({ submit: 'Network error or invalid response from server. Please try again.' });
    } finally {
      setLoading(false);
    }
  };

  const getPasswordRequirements = (password) => {
    return [
      {
        text: 'Be between 8 and 32 characters',
        met: password.length >= 8 && password.length <= 32
      },
      {
        text: 'Contain at least one uppercase letter',
        met: /[A-Z]/.test(password)
      },
      {
        text: 'Contain at least one lowercase letter',
        met: /[a-z]/.test(password)
      },
      {
        text: 'Contain at least one number',
        met: /\d/.test(password)
      },
      {
        text: 'Contain at least one special character (@#$%^&*+=)',
        met: /[@#$%^&*+=]/.test(password)
      }
    ];
  };

  return (
    <div className="flex items-center justify-center">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden">
        {/* Tab Headers */}
        <div className="flex">
          <button
            onClick={() => { setActiveTab('login'); setErrors({}); setSuccessMessage(''); }}
            className={`flex-1 py-4 px-6 text-center font-semibold transition-all ${activeTab === 'login'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
          >
            Sign In
          </button>
          <button
            onClick={() => { setActiveTab('register'); setErrors({}); setSuccessMessage(''); }}
            className={`flex-1 py-4 px-6 text-center font-semibold transition-all ${activeTab === 'register'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
          >
            Register
          </button>
        </div>

        <div className="p-8">
          {/* Display general submission errors */}
          {errors.submit && (
            <div className="text-red-500 text-sm text-center bg-red-50 p-3 rounded-lg mb-4">
              {errors.submit}
            </div>
          )}

          {/* Display success messages */}
          {successMessage && (
            <div className="text-green-600 text-sm text-center bg-green-50 p-3 rounded-lg mb-4">
              {successMessage}
            </div>
          )}

          {/* Login Form */}
          {activeTab === 'login' && (
            <div className="space-y-6">
              <div className="text-center mb-6">
                <h2 className="text-2xl font-bold text-gray-800">Welcome Back</h2>
                <p className="text-gray-600 mt-2">Sign in to your account</p>
              </div>

              {/* Email Field */}
              <div>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                  <input
                    type="email"
                    placeholder="Email address"
                    value={loginData.email}
                    onChange={(e) => setLoginData({ ...loginData, email: e.target.value })}
                    className={`w-full pl-12 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all ${errors.email ? 'border-red-500' : 'border-gray-300'
                      }`}
                  />
                </div>
                {errors.email && <p className="text-red-500 text-sm mt-1">{errors.email}</p>}
              </div>

              {/* Password Field */}
              <div>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    placeholder="Password"
                    value={loginData.password}
                    onChange={(e) => setLoginData({ ...loginData, password: e.target.value })}
                    className={`w-full pl-12 pr-12 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all ${errors.password ? 'border-red-500' : 'border-gray-300'
                      }`}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
                {errors.password && <p className="text-red-500 text-sm mt-1">{errors.password}</p>}
              </div>

              <button
                onClick={handleLoginSubmit}
                disabled={loading}
                className="w-full bg-blue-600 text-white py-3 rounded-lg font-semibold hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Signing In...' : 'Sign In'}
              </button>
            </div>
          )}

          {/* Register Form */}
          {activeTab === 'register' && (
            <div className="space-y-4">
              <div className="text-center mb-6">
                <h2 className="text-2xl font-bold text-gray-800">Create Account</h2>
                <p className="text-gray-600 mt-2">Join us today</p>
              </div>

              {/* Email Field */}
              <div>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                  <input
                    type="email"
                    placeholder="Email address"
                    value={registerData.email}
                    onChange={(e) => setRegisterData({ ...registerData, email: e.target.value })}
                    className={`w-full pl-12 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all ${errors.email ? 'border-red-500' : 'border-gray-300'
                      }`}
                  />
                </div>
                {errors.email && <p className="text-red-500 text-sm mt-1">{errors.email}</p>}
              </div>

              {/* Name Fields */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <div className="relative">
                    <User className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                    <input
                      type="text"
                      placeholder="First Name"
                      value={registerData.firstName}
                      onChange={(e) => setRegisterData({ ...registerData, firstName: e.target.value })}
                      className={`w-full pl-12 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all ${errors.firstName ? 'border-red-500' : 'border-gray-300'
                        }`}
                    />
                  </div>
                  {errors.firstName && <p className="text-red-500 text-xs mt-1">{errors.firstName}</p>}
                </div>
                <div>
                  <input
                    type="text"
                    placeholder="Last Name"
                    value={registerData.lastName}
                    onChange={(e) => setRegisterData({ ...registerData, lastName: e.target.value })}
                    className={`w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all ${errors.lastName ? 'border-red-500' : 'border-gray-300'
                      }`}
                    />
                  {errors.lastName && <p className="text-red-500 text-xs mt-1">{errors.lastName}</p>}
                </div>
              </div>

              {/* Phone Field */}
              <div>
                <div className="relative">
                  <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                  <input
                    type="tel"
                    placeholder="Phone Number"
                    value={registerData.phone}
                    onChange={(e) => setRegisterData({ ...registerData, phone: e.target.value })}
                    className={`w-full pl-12 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all ${errors.phone ? 'border-red-500' : 'border-gray-300'
                      }`}
                  />
                </div>
                {errors.phone && <p className="text-red-500 text-sm mt-1">{errors.phone}</p>}
              </div>

              {/* Address Field */}
              <div>
                <div className="relative">
                  <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                  <input
                    type="text"
                    placeholder="Address"
                    value={registerData.address}
                    onChange={(e) => setRegisterData({ ...registerData, address: e.target.value })}
                    className={`w-full pl-12 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all ${errors.address ? 'border-red-500' : 'border-gray-300'
                      }`}
                  />
                </div>
                {errors.address && <p className="text-red-500 text-sm mt-1">{errors.address}</p>}
              </div>

              {/* Password Fields */}
              <div>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    placeholder="Password"
                    value={registerData.passwordFields.password}
                    onChange={(e) => setRegisterData({
                      ...registerData,
                      passwordFields: { ...registerData.passwordFields, password: e.target.value }
                    })}
                    className={`w-full pl-12 pr-12 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all ${errors.password ? 'border-red-500' : 'border-gray-300'
                      }`}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
                {errors.password && <p className="text-red-500 text-sm mt-1">{errors.password}</p>}
                {registerData.passwordFields.password && (
                  <div className="text-xs mt-1">
                    {getPasswordRequirements(registerData.passwordFields.password).every(req => req.met) ? (
                      <p className="text-green-500 flex items-center gap-1">
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                        </svg>
                        Strong password
                      </p>
                    ) : (
                      <>
                        <p className="text-gray-500 mb-1">Password requirements:</p>
                        <ul className="list-disc pl-4 space-y-0.5">
                          {getPasswordRequirements(registerData.passwordFields.password)
                            .filter(req => !req.met)
                            .map((req, index) => (
                              <li key={index} className="text-gray-500">
                                {req.text}
                              </li>
                            ))}
                        </ul>
                      </>
                    )}
                  </div>
                )}
              </div>

              <div>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                  <input
                    type={showConfirmPassword ? 'text' : 'password'}
                    placeholder="Confirm Password"
                    value={registerData.passwordFields.confirmPassword}
                    onChange={(e) => setRegisterData({
                      ...registerData,
                      passwordFields: { ...registerData.passwordFields, confirmPassword: e.target.value }
                    })}
                    className={`w-full pl-12 pr-12 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all ${errors.confirmPassword ? 'border-red-500' : 'border-gray-300'
                      }`}
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
                {errors.confirmPassword && <p className="text-red-500 text-sm mt-1">{errors.confirmPassword}</p>}
              </div>

              <button
                onClick={handleRegisterSubmit}
                disabled={loading}
                className="w-full bg-blue-600 text-white py-3 rounded-lg font-semibold hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Creating Account...' : 'Create Account'}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

AuthForms.propTypes = {
  onClose: PropTypes.func.isRequired
};

export default AuthForms;