import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import { useHaptic } from '../../hooks/useHaptic';

export const TopBar = ({ title, showBack = true, rightAction, onBack }) => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();

  const handleBack = () => {
    triggerHaptic('light');
    if (onBack) {
      onBack();
    } else {
      navigate(-1);
    }
  };

  return (
    <div className="h-14 px-4 bg-[#0A0A0F]/95 backdrop-blur-md flex items-center justify-between border-b border-white/[0.05] sticky top-0 z-40 select-none">
      <div className="flex items-center gap-2">
        {showBack && (
          <button
            onClick={handleBack}
            className="p-2 -ml-1 rounded-full text-white hover:bg-white/5 active:scale-90 transition-transform duration-100"
            aria-label="Go back"
          >
            <ChevronLeft className="w-6 h-6" />
          </button>
        )}
        <span className="text-base font-bold text-white tracking-wide truncate max-w-[240px]">
          {title}
        </span>
      </div>
      
      {rightAction && (
        <div className="flex items-center">
          {rightAction}
        </div>
      )}
    </div>
  );
};

export default TopBar;
