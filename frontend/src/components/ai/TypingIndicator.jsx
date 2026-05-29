import React from 'react';
import { motion } from 'framer-motion';

export const TypingIndicator = () => {
  const dotTransition = (delay) => ({
    y: {
      duration: 0.45,
      repeat: Infinity,
      repeatType: 'reverse',
      ease: 'easeInOut',
      delay: delay
    }
  });

  return (
    <div className="w-full flex justify-start my-2">
      <div className="bg-white/[0.04] backdrop-blur-md border border-white/[0.06] p-3.5 rounded-2xl rounded-bl-xs flex items-center gap-1">
        <motion.div
          animate={{ y: [0, -6, 0] }}
          transition={dotTransition(0)}
          className="w-1.5 h-1.5 rounded-full bg-primary"
        />
        <motion.div
          animate={{ y: [0, -6, 0] }}
          transition={dotTransition(0.15)}
          className="w-1.5 h-1.5 rounded-full bg-secondary"
        />
        <motion.div
          animate={{ y: [0, -6, 0] }}
          transition={dotTransition(0.3)}
          className="w-1.5 h-1.5 rounded-full bg-white/40"
        />
      </div>
    </div>
  );
};

export default TypingIndicator;
// 
