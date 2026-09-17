"use client";

import React, { useState, useRef, useEffect } from "react";
import { cn } from "@/lib/utils";

export interface DropdownItem {
  label: string;
  onClick?: () => void;
  icon?: React.ReactNode;
  danger?: boolean;
  disabled?: boolean;
  divider?: boolean;
}

export interface DropdownProps {
  trigger: React.ReactNode;
  items: DropdownItem[];
  align?: "left" | "right";
  className?: string;
}

export function Dropdown({ trigger, items, align = "right", className }: DropdownProps) {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div ref={dropdownRef} className="relative inline-block text-left">
      <div onClick={() => setIsOpen((prev) => !prev)} className="cursor-pointer">
        {trigger}
      </div>

      {isOpen && (
        <div
          className={cn(
            "absolute z-50 mt-1.5 w-56 rounded-md border border-slate-200 bg-white p-1 text-slate-900 shadow-lg ring-1 ring-black/5 animate-in fade-in duration-100",
            align === "right" ? "right-0" : "left-0",
            className
          )}
        >
          {items.map((item, idx) => (
            <React.Fragment key={idx}>
              {item.divider && <div className="my-1 border-t border-slate-100" />}
              <button
                type="button"
                disabled={item.disabled}
                onClick={() => {
                  if (!item.disabled) {
                    item.onClick?.();
                    setIsOpen(false);
                  }
                }}
                className={cn(
                  "flex w-full items-center gap-2 rounded-md px-3 py-2 text-xs font-medium text-left transition-colors",
                  item.danger
                    ? "text-red-600 hover:bg-red-50"
                    : "text-slate-700 hover:bg-slate-100 hover:text-slate-900",
                  item.disabled && "opacity-50 cursor-not-allowed"
                )}
              >
                {item.icon && <span className="h-4 w-4 shrink-0">{item.icon}</span>}
                <span className="truncate">{item.label}</span>
              </button>
            </React.Fragment>
          ))}
        </div>
      )}
    </div>
  );
}
