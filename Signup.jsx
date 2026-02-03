import { useState } from 'react';
import { motion } from 'framer-motion';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';
import CanvasLayout from '../canvas/CanvasLayout';
import Card from '../components/ui/Card';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';

export default function Signup() {
  const navigate = useNavigate();
  const { signup } = useAuth();
  
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'STUDENT', // Default role
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [apiError, setApiError] = useState('');
  
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    // Clear error for this field
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
    setApiError('');
  };
  
  const validate = () => {
    const newErrors = {};
    
    if (!formData.name.trim()) {
      newErrors.name = 'Name is required';
    }
    
    if (!formData.email) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid';
    }
    
    if (!formData.password) {
      newErrors.password = 'Password is required';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
    }
    
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validate()) return;
    
    try {
      setLoading(true);
      setApiError('');
      
      const userData = {
        name: formData.name,
        email: formData.email,
        password: formData.password,
        role: formData.role,
      };
      
      const user = await signup(userData);
      
      // Redirect based on role
      const roleRoutes = {
        STUDENT: '/dashboard/student',
        TEACHER: '/dashboard/teacher',
        ADMIN: '/dashboard/admin',
      };
      
      navigate(roleRoutes[user.role] || '/dashboard');
    } catch (err) {
      setApiError(err.message || 'Signup failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <CanvasLayout>
      <div className="flex items-center justify-center min-h-screen px-6 py-12">
        <Card variant="glass" className="w-full max-w-md p-8">
          {/* Logo/Brand */}
          <motion.div
            className="text-center mb-8"
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
          >
            <p className="text-sm text-gray-400 mb-2">Get started</p>
            <h1 className="font-display font-semibold text-3xl text-white">
              Create your <span className="text-primary">acadify</span> account
            </h1>
          </motion.div>
          
          {/* Error Message */}
          {apiError && (
            <motion.div
              initial={{ opacity: 0, y: -5 }}
              animate={{ opacity: 1, y: 0 }}
              className="mb-6 p-4 bg-error/10 border border-error/30 rounded-lg"
            >
              <p className="text-error text-sm">{apiError}</p>
            </motion.div>
          )}
          
          {/* Signup Form */}
          <form onSubmit={handleSubmit} className="space-y-5">
            <Input
              label="Full Name"
              type="text"
              name="name"
              placeholder="John Doe"
              value={formData.name}
              onChange={handleChange}
              error={errors.name}
              autoComplete="name"
            />
            
            <Input
              label="Email"
              type="email"
              name="email"
              placeholder="your.email@example.com"
              value={formData.email}
              onChange={handleChange}
              error={errors.email}
              autoComplete="email"
            />
            
            {/* Role Selection */}
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">
                I am a
              </label>
              <div className="grid grid-cols-3 gap-3">
                {['STUDENT', 'TEACHER', 'ADMIN'].map((role) => (
                  <button
                    key={role}
                    type="button"
                    onClick={() => setFormData(prev => ({ ...prev, role }))}
                    className={`
                      px-4 py-3 rounded-lg font-medium text-sm
                      transition-all duration-300
                      ${formData.role === role
                        ? 'bg-primary text-white border-2 border-primary'
                        : 'bg-secondary-dark/60 text-gray-400 border-2 border-white/10 hover:border-primary/50'
                      }
                    `}
                  >
                    {role.charAt(0) + role.slice(1).toLowerCase()}
                  </button>
                ))}
              </div>
            </div>
            
            <Input
              label="Password"
              type="password"
              name="password"
              placeholder="••••••••"
              value={formData.password}
              onChange={handleChange}
              error={errors.password}
              autoComplete="new-password"
            />
            
            <Input
              label="Confirm Password"
              type="password"
              name="confirmPassword"
              placeholder="••••••••"
              value={formData.confirmPassword}
              onChange={handleChange}
              error={errors.confirmPassword}
              autoComplete="new-password"
            />
            
            {/* Submit Button */}
            <Button
              type="submit"
              size="large"
              loading={loading}
              className="w-full"
            >
              Create Account
            </Button>
          </form>
          
          {/* Login Link */}
          <motion.div
            className="mt-6 text-center"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.4 }}
          >
            <p className="text-gray-400 text-sm">
              Already have an account?{' '}
              <Link
                to="/login"
                className="text-primary hover:text-primary-hover font-medium transition-colors duration-300"
              >
                Sign in
              </Link>
            </p>
          </motion.div>
        </Card>
      </div>
    </CanvasLayout>
  );
}
