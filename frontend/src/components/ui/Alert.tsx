import React from "react";
import { AlertCircle, CheckCircle2, Info, AlertTriangle } from "lucide-react";
import { cn } from "@/lib/utils";

export interface AlertProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: "info" | "success" | "warning" | "error";
  title?: string;
}

export function Alert({ variant = "info", title, children, className, ...props }: AlertProps) {
  const styles = {
    info: "bg-sky-50 border-sky-200 text-sky-900 icon-sky-600",
    success: "bg-emerald-50 border-emerald-200 text-emerald-900 icon-emerald-600",
    warning: "bg-amber-50 border-amber-200 text-amber-900 icon-amber-600",
    error: "bg-red-50 border-red-200 text-red-900 icon-red-600",
  };

  const icons = {
    info: <Info className="h-4 w-4 text-sky-600" />,
    success: <CheckCircle2 className="h-4 w-4 text-emerald-600" />,
    warning: <AlertTriangle className="h-4 w-4 text-amber-600" />,
    error: <AlertCircle className="h-4 w-4 text-red-600" />,
  };

  return (
    <div className={cn("flex gap-3 rounded-md border p-4 text-sm", styles[variant], className)} role="alert" {...props}>
      <div className="shrink-0 pt-0.5">{icons[variant]}</div>
      <div className="space-y-1">
        {title && <h5 className="font-semibold leading-none tracking-tight">{title}</h5>}
        <div className="text-xs leading-relaxed opacity-90">{children}</div>
      </div>
    </div>
  );
}
