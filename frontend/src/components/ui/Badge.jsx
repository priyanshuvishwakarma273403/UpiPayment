import React from 'react';

export const Badge = ({ children, variant = 'info', className = '' }) => {
  const variantStyles = {
    success: 'bg-success/10 text-success border-success/25',
    failed: 'bg-danger/10 text-danger border-danger/25',
    pending: 'bg-warning/10 text-warning border-warning/25',
    info: 'bg-secondary/10 text-secondary border-secondary/25',
    purple: 'bg-primary/10 text-primary border-primary/25',
  };

  const getVariant = () => {
    const text = String(children || '').toUpperCase();
    if (text === 'SUCCESS' || text === 'COMPLETED' || variant === 'success') return 'success';
    if (text === 'FAILED' || text === 'BLOCKED' || variant === 'failed' || text === 'DANGER') return 'failed';
    if (text === 'PENDING' || text === 'REVIEW' || variant === 'pending' || text === 'WARNING') return 'pending';
    if (variant === 'purple') return 'purple';
    return 'info';
  };

  const activeVariant = getVariant();

  return (
    <span className={`px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider rounded-full border ${variantStyles[activeVariant]} ${className}`}>
      {children}
    </span>
  );
};

export default Badge;
