import React, { useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X } from 'lucide-react';
import { useHaptic } from '../../hooks/useHaptic';

export const BottomSheet = ({ isOpen, onClose, title = '', children }) => {
  const triggerHaptic = useHaptic();

  const handleClose = () => {
    triggerHaptic('light');
    onClose();
  };

  // Prevent background scrolling on desktop and double taps when open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {/* Backdrop overlay */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="absolute inset-0 bg-black/60 backdrop-blur-xs z-40"
            onClick={handleClose}
          />

          {/* Slide up sheet */}
          <motion.div
            initial={{ y: '100%' }}
            animate={{ y: 0 }}
            exit={{ y: '100%' }}
            transition={{ type: 'spring', damping: 24, stiffness: 220 }}
            drag="y"
            dragConstraints={{ top: 0, bottom: 0 }}
            dragElastic={{ top: 0, bottom: 0.8 }}
            onDragEnd={(e, info) => {
              if (info.offset.y > 140) {
                handleClose();
              }
            }}
            className="absolute bottom-0 left-0 right-0 max-h-[80%] bg-[#12121A] border-t border-white/[0.08] rounded-t-[32px] p-6 pb-safe-bottom z-50 flex flex-col select-none"
          >
            {/* Grab drag bar */}
            <div className="w-12 h-1.5 bg-white/10 rounded-full mx-auto mb-4 cursor-grab active:cursor-grabbing" />

            {/* Title / Header */}
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-base font-bold text-white tracking-wide truncate pr-4">
                {title}
              </h3>
              <button
                type="button"
                onClick={handleClose}
                className="p-1.5 rounded-full text-textSecondary bg-white/5 active:scale-90 transition-transform duration-100"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Scrollable sheet body */}
            <div className="overflow-y-auto no-scrollbar flex-1">
              {children}
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
};

export default BottomSheet;
