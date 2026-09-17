import React from "react";
import { cn } from "@/lib/utils";
import { Check, Minus } from "lucide-react";

export interface CheckboxProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, "type"> {
  label?: string;
  indeterminate?: boolean;
  error?: string;
  description?: string;
}

export const Checkbox = React.forwardRef<HTMLInputElement, CheckboxProps>(
  ({ className, label, indeterminate, error, description, checked, disabled, onChange, id, ...props }, ref) => {
    const checkboxId = id || (label ? label.toLowerCase().replace(/\s+/g, "-") : undefined);

    return (
      <div className="flex items-start space-x-2.5">
        <div className="relative flex items-center pt-0.5">
          <input
            type="checkbox"
            id={checkboxId}
            ref={ref}
            checked={checked}
            disabled={disabled}
            onChange={onChange}
            className="peer sr-only"
            {...props}
          />
          <div
            onClick={() => !disabled && onChange?.({ target: { checked: !checked } } as any)}
            className={cn(
              "flex h-4 w-4 shrink-0 items-center justify-center rounded border border-slate-300 bg-white transition-colors cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 peer-checked:border-blue-600 peer-checked:bg-blue-600 peer-checked:text-white peer-disabled:cursor-not-allowed peer-disabled:opacity-50",
              indeterminate && "border-blue-600 bg-blue-600 text-white",
              error && "border-red-500",
              className
            )}
          >
            {indeterminate ? (
              <Minus className="h-3 w-3 stroke-[3]" />
            ) : checked ? (
              <Check className="h-3 w-3 stroke-[3]" />
            ) : null}
          </div>
        </div>
        {(label || description) && (
          <div className="text-xs">
            {label && (
              <label
                htmlFor={checkboxId}
                className={cn(
                  "font-medium text-slate-900 cursor-pointer select-none",
                  disabled && "cursor-not-allowed text-slate-400"
                )}
              >
                {label}
              </label>
            )}
            {description && <p className="text-slate-500 text-[11px] mt-0.5">{description}</p>}
            {error && <p className="text-red-600 text-[11px] mt-0.5">{error}</p>}
          </div>
        )}
      </div>
    );
  }
);
Checkbox.displayName = "Checkbox";
