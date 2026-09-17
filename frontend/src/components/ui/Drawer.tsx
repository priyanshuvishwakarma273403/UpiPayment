"use client";

import React, { useEffect } from "react";
import { X } from "lucide-react";
import { cn } from "@/lib/utils";

export interface DrawerProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  position?: "left" | "right";
  size?: "sm" | "md" | "lg";
  children: React.ReactNode;
  className?: string;
}

export function Drawer({
  isOpen,
  onClose,
  title,
  position = "right",
  size = "md",
  children,
  className,
}: DrawerProps) {
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    if (isOpen) {
      document.addEventListener("keydown", handleKeyDown);
      document.body.style.overflow = "hidden";
    }
    return () => {
      document.removeEventListener("keydown", handleKeyDown);
      document.body.style.overflow = "unset";
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const sizes = {
    sm: "max-w-sm",
    md: "max-w-md",
    lg: "max-w-xl",
  };

  const positionStyles = {
    right: "right-0 inset-y-0 animate-in slide-in-from-right duration-200",
    left: "left-0 inset-y-0 animate-in slide-in-from-left duration-200",
  };

  return (
    <div className="fixed inset-0 z-50 overflow-hidden">
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-slate-900/40 backdrop-blur-xs transition-opacity animate-in fade-in duration-200"
        onClick={onClose}
      />

      {/* Drawer Content */}
      <div
        className={cn(
          "fixed flex h-full w-full flex-col border-l border-slate-200 bg-white shadow-2xl",
          positionStyles[position],
          sizes[size],
          className
        )}
      >
        {title && (
          <div className="flex h-16 items-center justify-between border-b border-slate-200 px-6">
            <h3 className="text-base font-semibold text-slate-900">{title}</h3>
            <button
              onClick={onClose}
              className="rounded-md p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600 focus:outline-none"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
        )}
        <div className="flex-1 overflow-y-auto p-6">{children}</div>
      </div>
    </div>
  );
}
