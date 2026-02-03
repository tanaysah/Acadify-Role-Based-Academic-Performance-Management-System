import { motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import CanvasLayout from '../canvas/CanvasLayout';
import Button from '../components/ui/Button';

// Acadify Landing Page
// First impression - Premium & Calm

export default function Landing() {
  const navigate = useNavigate();
  
  return (
    <CanvasLayout>
      <div className="flex flex-col items-center justify-center h-full px-6">
        <motion.div
          className="text-center max-w-4xl"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
        >
          {/* Small Tag */}
          <motion.p
            className="text-sm font-medium text-gray-400 tracking-wide mb-4"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.2, duration: 0.5 }}
          >
            Academic Intelligence Platform
          </motion.p>
          
          {/* Brand Name - Satoshi */}
          <motion.h1
            className="font-display font-medium text-8xl md:text-9xl text-white mb-6"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.3, duration: 0.6 }}
          >
            acadify
          </motion.h1>
          
          {/* Subheading */}
          <motion.p
            className="text-xl md:text-2xl text-gray-300 font-normal mb-12 max-w-2xl mx-auto"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.5, duration: 0.5 }}
          >
            Transforming academic data into meaningful insights.
          </motion.p>
          
          {/* CTA Buttons */}
          <motion.div
            className="flex flex-col sm:flex-row gap-4 justify-center items-center"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.7, duration: 0.5 }}
          >
            <Button
              size="large"
              onClick={() => navigate('/signup')}
            >
              Get Started
            </Button>
            
            <Button
              variant="secondary"
              size="large"
              onClick={() => navigate('/login')}
            >
              Login
            </Button>
          </motion.div>
          
          {/* Optional: Features List */}
          <motion.div
            className="mt-20 grid grid-cols-1 md:grid-cols-3 gap-8 text-left"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 1, duration: 0.6 }}
          >
            <FeatureCard
              title="Intelligent Analytics"
              description="AI-powered insights into student performance and trends"
            />
            <FeatureCard
              title="Real-time Data"
              description="Live dashboards with up-to-date academic information"
            />
            <FeatureCard
              title="Role-based Access"
              description="Customized views for students, teachers, and administrators"
            />
          </motion.div>
        </motion.div>
      </div>
    </CanvasLayout>
  );
}

function FeatureCard({ title, description }) {
  return (
    <motion.div
      className="p-6 rounded-xl bg-secondary-dark/40 backdrop-blur-sm border border-white/5"
      whileHover={{ y: -4, borderColor: 'rgba(79, 156, 255, 0.3)' }}
      transition={{ duration: 0.3 }}
    >
      <h3 className="font-display font-semibold text-lg text-white mb-2">
        {title}
      </h3>
      <p className="text-gray-400 text-sm font-normal">
        {description}
      </p>
    </motion.div>
  );
}
