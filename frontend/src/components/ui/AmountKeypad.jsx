import React from 'react';
import { Delete } from 'lucide-react';
import { useHaptic } from '../../hooks/useHaptic';

export const AmountKeypad = ({ value = '', onChange, maxAmount = 100000 }) => {
  const triggerHaptic = useHaptic();

  const handleKeyPress = (char) => {
    triggerHaptic('light');

    if (char === 'delete') {
      if (value.length > 0) {
        onChange(value.slice(0, -1));
      }
      return;
    }

    if (char === '.') {
      // Prevent multiple decimals
      if (value.includes('.')) return;
      // Add leading zero if empty
      if (value === '') {
        onChange('0.');
        return;
      }
    }

    // Limit to two decimal places
    if (value.includes('.')) {
      const [, decimals] = value.split('.');
      if (decimals && decimals.length >= 2) return;
    }

    const nextValue = value + char;

    // Check maximum payment limits
    if (parseFloat(nextValue) > maxAmount) return;
    
    // Prevent multiple leading zeroes
    if (value === '0' && char !== '.') {
      onChange(char);
      return;
    }

    onChange(nextValue);
  };

  const keys = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '0', 'delete'];

  return (
    <div className="grid grid-cols-3 gap-3 w-full max-w-[320px] mx-auto py-4 select-none amount-font">
      {keys.map((key) => {
        const isDelete = key === 'delete';
        return (
          <button
            key={key}
            type="button"
            onClick={() => handleKeyPress(key)}
            className="h-14 rounded-2xl flex items-center justify-center text-xl font-bold text-white bg-white/[0.03] active:bg-white/[0.1] active:scale-[0.93] transition-all duration-100 border border-white/[0.04]"
          >
            {isDelete ? (
              <Delete className="w-5 h-5 text-textSecondary" />
            ) : (
              key
            )}
          </button>
        );
      })}
    </div>
  );
};

export default AmountKeypad;
