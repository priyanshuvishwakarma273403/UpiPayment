import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Send, WifiOff, Shield, ArrowRight } from 'lucide-react';
import { useHaptic } from '../../hooks/useHaptic';
import Button from '../../components/ui/Button';

export const OnboardingScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  const [currentSlide, setCurrentSlide] = useState(0);

  const slides = [
    {
      title: 'Send Money Instantly',
      description: 'Transfer money to anyone, anywhere using UPI ID, QR scans, or select from your active contacts.',
      icon: Send,
      color: 'from-violet-500/20 to-purple-500/5 text-primary',
      badge: 'SPEED'
    },
    {
      title: 'Works Offline Too',
      description: 'Stuck in a low network zone? Payments queue automatically in your wallet and sync when you return online.',
      icon: WifiOff,
      color: 'from-cyan-500/20 to-blue-500/5 text-secondary',
      badge: 'OFFLINE MODE'
    },
    {
      title: 'AI-Powered Security',
      description: 'Our real-time guardian alerts you to potential scam parameters and flags risky accounts before you pay.',
      icon: Shield,
      color: 'from-emerald-500/20 to-green-500/5 text-success',
      badge: 'SECURED BY AI'
    }
  ];

  const handleNext = () => {
    triggerHaptic('light');
    if (currentSlide < slides.length - 1) {
      setCurrentSlide(currentSlide + 1);
    } else {
      navigate('/auth/login');
    }
  };

  const handleSkip = () => {
    triggerHaptic('medium');
    navigate('/auth/login');
  };

  const currentData = slides[currentSlide];
  const IconComponent = currentData.icon;

  return (
    <div className="flex-1 min-h-screen flex flex-col justify-between p-6 bg-[#0A0A0F] select-none text-white">
      {/* Top action bar */}
      <div className="flex justify-between items-center h-10">
        <span className="text-[10px] font-extrabold tracking-widest text-white/40 uppercase">
          UPI MESH
        </span>
        {currentSlide < slides.length - 1 && (
          <button
            onClick={handleSkip}
            className="text-xs font-bold text-textSecondary hover:text-white transition-colors py-1 px-3 bg-white/5 rounded-full border border-white/[0.04]"
          >
            Skip
          </button>
        )}
      </div>

      {/* Slide graphics frame */}
      <div className="flex-1 flex flex-col items-center justify-center my-6">
        <AnimatePresence mode="wait">
          <motion.div
            key={currentSlide}
            initial={{ opacity: 0, x: 50, scale: 0.95 }}
            animate={{ opacity: 1, x: 0, scale: 1 }}
            exit={{ opacity: 0, x: -50, scale: 0.95 }}
            transition={{ type: 'spring', damping: 20, stiffness: 200 }}
            className="flex flex-col items-center w-full"
          >
            {/* Visual Icon Sphere with Neon Glow */}
            <div className="relative mb-10">
              {/* Pulsing neon background glow */}
              <motion.div
                animate={{ scale: [0.95, 1.1, 0.95], opacity: [0.25, 0.45, 0.25] }}
                transition={{ duration: 3, repeat: Infinity, ease: 'easeInOut' }}
                className="absolute inset-0 rounded-[44px] bg-gradient-to-tr from-[#6C63FF] to-[#00D2FF] blur-2xl pointer-events-none"
              />
              
              <div className={`w-36 h-36 rounded-[40px] bg-gradient-to-tr ${currentData.color} border border-white/10 flex items-center justify-center relative overflow-hidden`}>
                {/* Visual floating particles */}
                <div className="absolute top-[-10%] left-[-10%] w-12 h-12 rounded-full bg-white/5 blur-md" />
                <div className="absolute bottom-[-10%] right-[-10%] w-16 h-16 rounded-full bg-white/5 blur-md" />
                
                <IconComponent className="w-16 h-16 relative z-10" />
                
                <div className="absolute top-3 right-3 px-2 py-0.5 bg-white/15 rounded-md text-[8px] font-extrabold tracking-widest text-white border border-white/10 uppercase">
                  {currentData.badge}
                </div>
              </div>
            </div>

            {/* Typography with rich colors */}
            <h2 className="text-2xl font-black text-center tracking-wide px-2">
              <span className="bg-gradient-to-r from-white via-white/90 to-white/70 bg-clip-text text-transparent">
                {currentData.title}
              </span>
            </h2>
            
            <p className="text-xs text-textSecondary text-center mt-3.5 px-6 leading-relaxed max-w-[290px]">
              {currentData.description}
            </p>
          </motion.div>
        </AnimatePresence>
      </div>

      {/* Control Dots & Button footer */}
      <div className="flex flex-col gap-6">
        {/* Indicators */}
        <div className="flex justify-center gap-2">
          {slides.map((_, index) => (
            <div
              key={index}
              onClick={() => {
                triggerHaptic('light');
                setCurrentSlide(index);
              }}
              className={`h-1.5 rounded-full transition-all duration-300 cursor-pointer ${
                index === currentSlide ? 'w-6 bg-primary' : 'w-2 bg-white/15'
              }`}
            />
          ))}
        </div>

        {/* CTA Button */}
        <Button
          variant={currentSlide === slides.length - 1 ? 'gradient' : 'glass'}
          onClick={handleNext}
          className="w-full py-4 text-sm font-bold flex items-center justify-center gap-1.5"
        >
          {currentSlide === slides.length - 1 ? 'Get Started' : 'Next'}
          <ArrowRight className="w-4 h-4" />
        </Button>
      </div>
    </div>
  );
};

export default OnboardingScreen;
