import React from "react";
import { cn } from "@/lib/utils";

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "outline" | "danger" | "ghost";
  size?: "xs" | "sm" | "md" | "lg";
  isLoading?: boolean;
  fullWidth?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "primary", size = "md", isLoading, fullWidth, leftIcon, rightIcon, children, disabled, ...props }, ref) => {
    const baseStyles = "inline-flex items-center justify-center font-medium rounded-md transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 disabled:opacity-50 disabled:pointer-events-none";
    
    const variants = {
      primary: "bg-blue-700 text-white hover:bg-blue-800 active:bg-blue-900 shadow-sm",
      secondary: "bg-slate-100 text-slate-900 hover:bg-slate-200 border border-slate-300",
      outline: "border border-slate-300 bg-white text-slate-700 hover:bg-slate-50",
      danger: "bg-red-600 text-white hover:bg-red-700 active:bg-red-800 shadow-sm",
      ghost: "text-slate-700 hover:bg-slate-100",
    };

    const sizes = {
      xs: "h-7 px-2.5 text-[11px] gap-1",
      sm: "h-8 px-3 text-xs gap-1.5",
      md: "h-9 px-4 text-sm gap-2",
      lg: "h-11 px-6 text-base gap-2",
    };

    return (
      <button
        ref={ref}
        disabled={disabled || isLoading}
        className={cn(baseStyles, variants[variant], sizes[size], fullWidth && "w-full", className)}
        {...props}
      >
        {isLoading ? (
          <span className="mr-2 inline-block h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
        ) : leftIcon ? (
          <span className="inline-flex shrink-0">{leftIcon}</span>
        ) : null}
        {children}
        {!isLoading && rightIcon ? (
          <span className="inline-flex shrink-0">{rightIcon}</span>
        ) : null}
      </button>
    );
  }
);
Button.displayName = "Button";
