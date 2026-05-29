import React, { useRef, useEffect } from 'react';

export const OtpInput = ({ value = '', onChange, length = 6 }) => {
  const inputsRef = useRef([]);

  const handleChange = (e, idx) => {
    const val = e.target.value.replace(/[^0-9]/g, ''); // keep only numbers
    const otpArray = value.split('');
    
    // Replace current digit
    otpArray[idx] = val.substring(val.length - 1);
    
    const newOtp = otpArray.join('');
    onChange(newOtp);

    // Auto-focus next input
    if (val && idx < length - 1) {
      inputsRef.current[idx + 1].focus();
    }
  };

  const handleKeyDown = (e, idx) => {
    if (e.key === 'Backspace') {
      const otpArray = value.split('');
      
      // If current is filled, clear it. If empty, shift focus back and clear that.
      if (!otpArray[idx] && idx > 0) {
        inputsRef.current[idx - 1].focus();
        otpArray[idx - 1] = '';
        onChange(otpArray.join(''));
      }
    }
  };

  // Keep focus in bounds
  const handlePaste = (e) => {
    e.preventDefault();
    const pastedText = e.clipboardData.getData('text').replace(/[^0-9]/g, '').slice(0, length);
    if (pastedText) {
      onChange(pastedText);
      // focus the last box or the next unfilled box
      const targetIdx = Math.min(pastedText.length, length - 1);
      inputsRef.current[targetIdx].focus();
    }
  };

  return (
    <div className="flex gap-2.5 justify-center w-full my-6" onPaste={handlePaste}>
      {Array.from({ length }).map((_, idx) => (
        <input
          key={idx}
          ref={(el) => (inputsRef.current[idx] = el)}
          type="text"
          inputMode="numeric"
          pattern="[0-9]*"
          maxLength={1}
          value={value[idx] || ''}
          onChange={(e) => handleChange(e, idx)}
          onKeyDown={(e) => handleKeyDown(e, idx)}
          className="w-12 h-14 text-center text-lg font-bold bg-white/[0.04] text-white border border-white/10 rounded-xl focus:border-primary focus:ring-1 focus:ring-primary focus:outline-none transition-all duration-100 placeholder:text-transparent select-none amount-font"
        />
      ))}
    </div>
  );
};

export default OtpInput;
