import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useAuthStore } from '../../store/authStore';
import { useHaptic } from '../../hooks/useHaptic';

export const SplashScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  useEffect(() => {
    // Light vibration on load
    triggerHaptic('light');

    const timeout = setTimeout(() => {
      if (isAuthenticated) {
        navigate('/home');
      } else {
        navigate('/onboarding');
      }
    }, 2200); // 2.2 seconds screen time

    return () => clearTimeout(timeout);
  }, [isAuthenticated, navigate]);

  return (
    <div className="flex-1 min-h-screen flex flex-col justify-between items-center bg-btn-grad py-12 px-6 select-none relative overflow-hidden">
      
      {/* Background glow flares */}
      <div className="absolute top-[-20%] left-[-20%] w-[80%] h-[40%] rounded-full bg-white/10 blur-[120px] pointer-events-none" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[60%] h-[30%] rounded-full bg-white/5 blur-[80px] pointer-events-none" />

      {/* Main Logo Container */}
      <div className="flex-1 flex flex-col items-center justify-center">
        <motion.div
          initial={{ scale: 0.6, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{
            type: 'spring',
            stiffness: 200,
            damping: 15,
            duration: 0.8
          }}
          className="relative flex items-center justify-center w-24 h-24 rounded-[32px] bg-white text-[#6C63FF] shadow-2xl mb-5"
        >
          {/* Animated pulsing outer ring */}
          <motion.div
            animate={{ scale: [1, 1.25, 1] }}
            transition={{ duration: 2, repeat: Infinity, ease: 'easeInOut' }}
            className="absolute inset-0 rounded-[32px] bg-white/20 border border-white/40 pointer-events-none"
          />
          
          <svg className="w-12 h-12 fill-current" viewBox="0 0 24 24">
            <path d="M12 2L2 22h20L12 2zm0 3.99L18.49 19H5.51L12 5.99zm-1 5.01h2v3h-2v-3zm0 4h2v2h-2v-2z" />
          </svg>
        </motion.div>

        {/* App Title */}
        <motion.h1
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.3, duration: 0.6 }}
          className="text-3xl font-black text-white tracking-wider uppercase"
        >
          UPI Mesh
        </motion.h1>

        {/* Subtitle */}
        <motion.p
          initial={{ opacity: 0 }}
          animate={{ opacity: 0.7 }}
          transition={{ delay: 0.6, duration: 0.6 }}
          className="text-xs font-semibold tracking-widest text-white/90 mt-2 uppercase"
        >
          Instant AI Payments
        </motion.p>
      </div>

      {/* Footer loading tracker */}
      <div className="w-full max-w-[200px] flex flex-col items-center gap-3">
        <div className="w-full h-1 bg-white/20 rounded-full overflow-hidden">
          <motion.div
            initial={{ width: '0%' }}
            animate={{ width: '100%' }}
            transition={{ duration: 2, ease: 'easeInOut' }}
            className="h-full bg-white rounded-full"
          />
        </div>
        <span className="text-[9px] font-bold text-white/50 tracking-wider uppercase">
          Secured by UPI Mesh Guard
        </span>
      </div>
    </div>
  );
};

export default SplashScreen;
