import React from "react";
import { cn } from "@/lib/utils";

export type BadgeStatusVariant = "default" | "neutral" | "success" | "warning" | "danger" | "info" | "outline" | "critical";

export interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: BadgeStatusVariant;
  dot?: boolean;
  size?: "sm" | "md";
}

export const Badge = ({ className, variant = "default", dot = false, size = "md", children, ...props }: BadgeProps) => {
  const variants = {
    default: "bg-slate-100 text-slate-800 border-slate-200",
    neutral: "bg-slate-100 text-slate-700 border-slate-200",
    success: "bg-emerald-50 text-emerald-700 border-emerald-200",
    warning: "bg-amber-50 text-amber-800 border-amber-200",
    danger: "bg-red-50 text-red-700 border-red-200",
    critical: "bg-rose-50 text-rose-800 border-rose-200",
    info: "bg-blue-50 text-blue-700 border-blue-200",
    outline: "bg-transparent text-slate-700 border-slate-300",
  };

  const dotColors = {
    default: "bg-slate-500",
    neutral: "bg-slate-400",
    success: "bg-emerald-500",
    warning: "bg-amber-500",
    danger: "bg-red-500",
    critical: "bg-rose-500",
    info: "bg-blue-500",
    outline: "bg-slate-600",
  };

  const sizes = {
    sm: "px-2 py-0.25 text-[10px]",
    md: "px-2.5 py-0.5 text-xs",
  };

  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full border font-semibold tracking-wide transition-colors",
        variants[variant],
        sizes[size],
        className
      )}
      {...props}
    >
      {dot && <span className={cn("h-1.5 w-1.5 rounded-full shrink-0", dotColors[variant])} />}
      {children}
    </span>
  );
};
