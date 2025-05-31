import React, { useState, useEffect } from 'react';

const FieldError = ({ field, errors }) => {
  return errors?.[field] ? (
    <p className="text-red-500 text-sm mt-1">{errors[field]}</p>
  ) : null;
};

const Profile = () => {
  const [userDetails, setUserDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    address: ''
  });
  const [saveStatus, setSaveStatus] = useState({
    loading: false,
    error: null,
    fieldErrors: {}
  });

  useEffect(() => {
    fetchUserDetails();
  }, []);

  const fetchUserDetails = async () => {
    try {
      const response = await fetch('/api/user/me/details', {
        credentials: 'include'
      });
      const result = await response.json();

      if (response.ok) {
        setUserDetails(result.data);
        setFormData(result.data);
      } else {
        setError(result.message);
      }
    } catch (err) {
      setError('Failed to load user details');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSave = async () => {
    setSaveStatus({ loading: true, error: null, fieldErrors: {} });
    try {
      const response = await fetch('/api/user/me/details', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
          firstName: formData.firstName,
          lastName: formData.lastName,
          phoneNumber: formData.phoneNumber,
          address: formData.address
        })
      });

      const result = await response.json();
      if (response.ok) {
        setUserDetails({
          ...userDetails,
          ...formData
        });
        setIsEditing(false);
        setSaveStatus({ loading: false, error: null, fieldErrors: {} });
      } else {
        setSaveStatus({
          loading: false,
          error: result.message || 'Failed to update profile',
          fieldErrors: result.errors || {}
        });
      }
    } catch (err) {
      setSaveStatus({ loading: false, error: 'Failed to update profile', fieldErrors: {} });
    }
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">Loading...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-red-500 text-center">{error}</div>
      </div>
    );
  }

  const fields = [
    { name: 'firstName', label: 'First Name', type: 'text' },
    { name: 'lastName', label: 'Last Name', type: 'text' },
    { name: 'phoneNumber', label: 'Phone Number', type: 'tel' },
  ];

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">My Profile</h1>
        {!isEditing && (
          <button
            onClick={() => setIsEditing(true)}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            Edit Profile
          </button>
        )}
      </div>

      <div className="bg-white rounded-lg shadow p-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-4">
            <h2 className="text-lg font-semibold">Personal Information</h2>
            <div className="space-y-4">
              {fields.map(({ name, label, type }) => (
                <div key={name}>
                  <label className="block text-sm text-gray-600 mb-1">{label}</label>
                  {isEditing ? (
                    <input
                      type={type}
                      name={name}
                      value={formData[name]}
                      onChange={handleInputChange}
                      className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  ) : (
                    <p className="p-2 bg-gray-50 rounded">{userDetails[name]}</p>
                  )}
                  {isEditing && <FieldError field={name} errors={saveStatus.fieldErrors} />}
                </div>
              ))}
              <div>
                <label className="block text-sm text-gray-600 mb-1">Email</label>
                <p className="p-2 bg-gray-100 rounded text-gray-600">{userDetails.email}</p>
                {isEditing && (
                  <p className="text-sm text-gray-500 mt-1">Email cannot be changed</p>
                )}
              </div>
            </div>
          </div>

          <div className="space-y-4">
            <h2 className="text-lg font-semibold">Address Information</h2>
            <div>
              <label className="block text-sm text-gray-600 mb-1">Address</label>
              {isEditing ? (
                <textarea
                  name="address"
                  value={formData.address}
                  onChange={handleInputChange}
                  rows="3"
                  className="w-full p-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              ) : (
                <p className="p-2 bg-gray-50 rounded">{userDetails.address}</p>
              )}
              {isEditing && <FieldError field="address" errors={saveStatus.fieldErrors} />}
            </div>
          </div>
        </div>

        {isEditing && (
          <div className="mt-6">
            {saveStatus.error && (
              <p className="text-red-500 text-sm mb-2">{saveStatus.error}</p>
            )}
            <div className="flex justify-end gap-3">
              <button
                className="px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors"
                onClick={() => {
                  setIsEditing(false);
                  setFormData(userDetails); // Reset form data
                  setSaveStatus({ loading: false, error: null, fieldErrors: {} });
                }}
              >
                Cancel
              </button>
              <button
                className={`px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed`}
                onClick={handleSave}
                disabled={saveStatus.loading}
              >
                {saveStatus.loading ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Profile;