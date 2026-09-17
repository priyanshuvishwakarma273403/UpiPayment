"use client";

import React from "react";
import { useUIStore, ToastMessage } from "@/stores/uiStore";
import { CheckCircle2, AlertTriangle, AlertCircle, Info, X } from "lucide-react";
import { cn } from "@/lib/utils";

export function ToastContainer() {
  const toasts = useUIStore((state) => state.toasts);
  const removeToast = useUIStore((state) => state.removeToast);

  if (toasts.length === 0) return null;

  const icons = {
    success: <CheckCircle2 className="h-5 w-5 text-emerald-600 shrink-0" />,
    warning: <AlertTriangle className="h-5 w-5 text-amber-600 shrink-0" />,
    danger: <AlertCircle className="h-5 w-5 text-red-600 shrink-0" />,
    info: <Info className="h-5 w-5 text-blue-600 shrink-0" />,
    neutral: <Info className="h-5 w-5 text-slate-600 shrink-0" />,
  };

  const borders = {
    success: "border-emerald-200 bg-emerald-50/90 text-emerald-900",
    warning: "border-amber-200 bg-amber-50/90 text-amber-900",
    danger: "border-red-200 bg-red-50/90 text-red-900",
    info: "border-blue-200 bg-blue-50/90 text-blue-900",
    neutral: "border-slate-200 bg-white text-slate-900",
  };

  return (
    <div className="fixed bottom-5 right-5 z-50 flex flex-col space-y-2.5 max-w-sm w-full pointer-events-none">
      {toasts.map((toast) => {
        const variant = toast.variant || "info";
        return (
          <div
            key={toast.id}
            className={cn(
              "pointer-events-auto flex items-start gap-3 rounded-lg border p-4 shadow-lg backdrop-blur-xs transition-all animate-in slide-in-from-bottom-5 duration-200",
              borders[variant]
            )}
          >
            {icons[variant]}
            <div className="flex-1 text-xs">
              <h4 className="font-semibold text-slate-900">{toast.title}</h4>
              {toast.description && <p className="mt-0.5 text-slate-600">{toast.description}</p>}
            </div>
            <button
              onClick={() => removeToast(toast.id)}
              className="text-slate-400 hover:text-slate-600 p-0.5 rounded-md"
            >
              <X className="h-4 w-4" />
            </button>
          </div>
        );
      })}
    </div>
  );
}
