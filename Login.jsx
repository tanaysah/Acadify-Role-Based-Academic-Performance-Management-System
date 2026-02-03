import { useState } from 'react';
import { motion } from 'framer-motion';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';
import CanvasLayout from '../canvas/CanvasLayout';
import Card from '../components/ui/Card';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  
  const [formData, setFormData] = useState({
    email: '',
    password: '',
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
    
    if (!formData.email) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid';
    }
    
    if (!formData.password) {
      newErrors.password = 'Password is required';
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
      
      const user = await login(formData.email, formData.password);
      
      // Redirect based on role
      const roleRoutes = {
        STUDENT: '/dashboard/student',
        TEACHER: '/dashboard/teacher',
        ADMIN: '/dashboard/admin',
      };
      
      navigate(roleRoutes[user.role] || '/dashboard');
    } catch (err) {
      setApiError(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <CanvasLayout>
      <div className="flex items-center justify-center h-full px-6">
        <Card variant="glass" className="w-full max-w-md p-8">
          {/* Logo/Brand */}
          <motion.div
            className="text-center mb-8"
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
          >
            <p className="text-sm text-gray-400 mb-2">Welcome back</p>
            <h1 className="font-display font-semibold text-3xl text-white">
              Sign in to <span className="text-primary">acadify</span>
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
          
          {/* Login Form */}
          <form onSubmit={handleSubmit} className="space-y-5">
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
            
            <Input
              label="Password"
              type="password"
              name="password"
              placeholder="••••••••"
              value={formData.password}
              onChange={handleChange}
              error={errors.password}
              autoComplete="current-password"
            />
            
            {/* Forgot Password Link */}
            <div className="text-right">
              <Link
                to="/forgot-password"
                className="text-sm text-primary hover:text-primary-hover transition-colors duration-300"
              >
                Forgot password?
              </Link>
            </div>
            
            {/* Submit Button */}
            <Button
              type="submit"
              size="large"
              loading={loading}
              className="w-full"
            >
              Continue
            </Button>
          </form>
          
          {/* Signup Link */}
          <motion.div
            className="mt-6 text-center"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.4 }}
          >
            <p className="text-gray-400 text-sm">
              Don't have an account?{' '}
              <Link
                to="/signup"
                className="text-primary hover:text-primary-hover font-medium transition-colors duration-300"
              >
                Create account
              </Link>
            </p>
          </motion.div>
        </Card>
      </div>
    </CanvasLayout>
  );
}
