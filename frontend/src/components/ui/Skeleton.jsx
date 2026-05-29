import React from 'react';

export const Skeleton = ({ className = '', variant = 'rect' }) => {
  const shapeClass = variant === 'circle' ? 'rounded-full' : 'rounded-2xl';
  return (
    <div className={`shimmer-bg ${shapeClass} ${className}`} />
  );
};

export const SkeletonCard = ({ className = '' }) => {
  return (
    <div className={`glass-card p-6 flex flex-col gap-4 ${className}`}>
      <Skeleton className="w-24 h-4" />
      <Skeleton className="w-48 h-8" />
      <div className="flex gap-2">
        <Skeleton className="w-20 h-9" />
        <Skeleton className="w-20 h-9" />
      </div>
    </div>
  );
};

export const SkeletonList = ({ items = 5, className = '' }) => {
  return (
    <div className={`flex flex-col gap-4 ${className}`}>
      {Array.from({ length: items }).map((_, idx) => (
        <div key={idx} className="flex items-center justify-between py-2 border-b border-white/[0.02]">
          <div className="flex items-center gap-3">
            <Skeleton className="w-10 h-10" variant="circle" />
            <div className="flex flex-col gap-2">
              <Skeleton className="w-28 h-4" />
              <Skeleton className="w-20 h-3" />
            </div>
          </div>
          <Skeleton className="w-16 h-5" />
        </div>
      ))}
    </div>
  );
};

export const SkeletonText = ({ lines = 3, className = '' }) => {
  return (
    <div className={`flex flex-col gap-2 ${className}`}>
      {Array.from({ length: lines }).map((_, idx) => (
        <Skeleton 
          key={idx} 
          className={`h-3.5 ${idx === lines - 1 ? 'w-[70%]' : 'w-full'}`} 
        />
      ))}
    </div>
  );
};

export default Skeleton;
