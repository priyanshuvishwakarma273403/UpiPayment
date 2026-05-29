import React, { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';

export const Input = React.forwardRef(({
  label,
  error,
  icon: Icon,
  type = 'text',
  placeholder,
  className = '',
  prefix,
  containerClassName = '',
  showPasswordToggle = false,
  ...props
}, ref) => {
  const [showPassword, setShowPassword] = useState(false);

  const isPassword = type === 'password';
  const computedType = isPassword && showPasswordToggle ? (showPassword ? 'text' : 'password') : type;

  return (
    <div className={`w-full flex flex-col gap-1.5 ${containerClassName}`}>
      {label && (
        <label className="text-xs font-semibold tracking-wider text-textSecondary uppercase pl-1">
          {label}
        </label>
      )}
      <div className="relative w-full flex items-center">
        {/* Leading icon if provided */}
        {Icon && !prefix && (
          <div className="absolute left-4 text-textSecondary pointer-events-none z-10">
            <Icon className="w-5 h-5" />
          </div>
        )}

        {/* Flag prefix e.g. +91 */}
        {prefix && (
          <div className="absolute left-4 text-sm font-semibold text-textSecondary flex items-center gap-1.5 pointer-events-none z-10">
            <span>{prefix}</span>
            <span className="w-px h-4 bg-white/10" />
          </div>
        )}

        <input
          ref={ref}
          type={computedType}
          placeholder={placeholder}
          className={`w-full py-3.5 rounded-2xl text-white font-medium text-sm glass-input transition-all outline-none border focus:ring-0 ${
            Icon ? 'pl-11' : prefix ? 'pl-16' : 'pl-4'
          } ${showPasswordToggle ? 'pr-11' : 'pr-4'} ${
            error ? 'border-danger/50 focus:border-danger/100 shadow-[0_0_10px_rgba(255,71,87,0.15)]' : 'border-white/5'
          } ${className}`}
          {...props}
        />

        {/* Password Eye show/hide toggle */}
        {isPassword && showPasswordToggle && (
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-4 text-textSecondary hover:text-white transition-colors z-10"
            tabIndex="-1"
          >
            {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
          </button>
        )}
      </div>

      {error && (
        <span className="text-xs font-medium text-danger pl-1 animate-pulse">
          {error.message || error}
        </span>
      )}
    </div>
  );
});

Input.displayName = 'Input';

export default Input;
