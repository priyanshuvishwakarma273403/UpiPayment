import React from 'react';
import { useHaptic } from '../../hooks/useHaptic';

export const Button = ({
  children,
  variant = 'gradient',
  size = 'md',
  className = '',
  onClick,
  disabled = false,
  hapticType = 'light',
  type = 'button',
  ...props
}) => {
  const triggerHaptic = useHaptic();

  const handlePress = (e) => {
    if (disabled) return;
    triggerHaptic(hapticType);
    if (onClick) onClick(e);
  };

  const baseStyles = 'rounded-2xl font-semibold tracking-wide transition-all active:scale-[0.96] flex items-center justify-center gap-2 duration-100 disabled:opacity-50 disabled:pointer-events-none select-none';
  
  const sizeStyles = {
    sm: 'px-3 py-2 text-xs',
    md: 'px-5 py-3 text-sm',
    lg: 'px-6 py-4 text-base'
  };

  const variantStyles = {
    gradient: 'bg-btn-grad hover:brightness-110 text-white shadow-glow-primary border border-white/10',
    outline: 'bg-transparent text-white border border-white/20 hover:bg-white/5 active:border-white/40',
    ghost: 'bg-transparent text-textSecondary hover:text-white hover:bg-white/5',
    success: 'bg-success-grad text-white shadow-glow-success border border-white/10',
    danger: 'bg-gradient-to-r from-red-500 to-rose-600 hover:from-red-600 hover:to-rose-700 text-white border border-white/10',
    glass: 'bg-white/5 backdrop-blur-md text-white border border-white/10 hover:bg-white/10'
  };

  return (
    <button
      type={type}
      disabled={disabled}
      onClick={handlePress}
      className={`${baseStyles} ${sizeStyles[size]} ${variantStyles[variant]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
};

export default Button;
