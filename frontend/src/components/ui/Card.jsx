import React from 'react';
import { motion } from 'framer-motion';

export const Card = ({
  children,
  variant = 'glass', // 'glass', 'gradient', 'wallet', 'solid'
  className = '',
  shine = false,
  animate = true,
  onClick,
  ...props
}) => {
  const baseStyles = 'rounded-[24px] overflow-hidden relative select-none';
  
  const variantStyles = {
    glass: 'glass-card',
    gradient: 'bg-btn-grad text-white border border-white/10 shadow-glow-primary',
    wallet: 'bg-wallet-grad text-white border border-white/10 shadow-glow-primary',
    solid: 'bg-[#12121A] border border-white/5',
    surface: 'bg-[#1A1A2E] border border-white/10'
  };

  const Component = animate ? motion.div : 'div';
  const motionProps = animate ? {
    initial: { y: 15, opacity: 0 },
    animate: { y: 0, opacity: 1 },
    transition: { type: 'spring', stiffness: 260, damping: 25 }
  } : {};

  return (
    <Component
      onClick={onClick}
      className={`${baseStyles} ${variantStyles[variant]} ${onClick ? 'cursor-pointer active:scale-[0.98] transition-transform duration-100' : ''} ${className}`}
      {...motionProps}
      {...props}
    >
      {/* Shine effect overlay */}
      {shine && (
        <div className="absolute inset-0 pointer-events-none overflow-hidden z-0">
          <div className="absolute inset-0 w-1/2 h-full bg-gradient-to-r from-transparent via-white/10 to-transparent -skew-x-12 animate-shine" />
        </div>
      )}
      
      <div className="relative z-10 w-full h-full">
        {children}
      </div>
    </Component>
  );
};

export default Card;
