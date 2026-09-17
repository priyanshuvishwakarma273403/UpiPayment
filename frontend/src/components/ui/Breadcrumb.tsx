import React from "react";
import Link from "next/link";
import { ChevronRight, Home } from "lucide-react";
import { cn } from "@/lib/utils";

export interface BreadcrumbItem {
  label: string;
  href?: string;
}

export interface BreadcrumbProps {
  items: BreadcrumbItem[];
  className?: string;
}

export function Breadcrumb({ items, className }: BreadcrumbProps) {
  return (
    <nav className={cn("flex items-center space-x-1.5 text-xs text-slate-500", className)} aria-label="Breadcrumb">
      <Link href="/dashboard" className="flex items-center hover:text-slate-900 transition-colors">
        <Home className="h-3.5 w-3.5" />
      </Link>
      {items.map((item, idx) => {
        const isLast = idx === items.length - 1;
        return (
          <React.Fragment key={idx}>
            <ChevronRight className="h-3 w-3 text-slate-400 shrink-0" />
            {item.href && !isLast ? (
              <Link href={item.href} className="hover:text-slate-900 transition-colors">
                {item.label}
              </Link>
            ) : (
              <span className={cn(isLast ? "font-semibold text-slate-900" : "")}>{item.label}</span>
            )}
          </React.Fragment>
        );
      })}
    </nav>
  );
}
