import React from 'react';

export const Avatar = ({ name = '', size = 'md', className = '' }) => {
  const getInitials = (fullName) => {
    if (!fullName) return 'U';
    const parts = fullName.trim().split(/\s+/);
    if (parts.length === 1) {
      return parts[0].slice(0, 2).toUpperCase();
    }
    return (parts[0][0] + parts[1][0]).toUpperCase();
  };

  const gradients = [
    'from-[#6C63FF] to-[#00D2FF]', // purple to blue
    'from-[#a855f7] to-[#ec4899]', // magenta to pink
    'from-[#06b6d4] to-[#3b82f6]', // cyan to indigo
    'from-[#10b981] to-[#059669]', // green to dark green
    'from-[#f59e0b] to-[#d97706]', // amber to orange
  ];

  // Hash the name string to pick a consistent gradient index
  const getGradientIndex = (str) => {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    return Math.abs(hash) % gradients.length;
  };

  const sizeClasses = {
    xs: 'w-6 h-6 text-[10px]',
    sm: 'w-8 h-8 text-xs',
    md: 'w-12 h-12 text-sm',
    lg: 'w-16 h-16 text-lg',
    xl: 'w-24 h-24 text-2xl',
  };

  const selectedGradient = gradients[getGradientIndex(name || 'User')];

  return (
    <div className={`rounded-full p-[1.5px] bg-gradient-to-tr ${selectedGradient} flex-shrink-0 ${className}`}>
      <div className={`rounded-full bg-[#12121A] flex items-center justify-center font-bold text-white tracking-wide ${sizeClasses[size]}`}>
        {getInitials(name)}
      </div>
    </div>
  );
};

export default Avatar;
