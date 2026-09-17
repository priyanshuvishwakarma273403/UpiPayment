import React from "react";
import { Badge } from "@/components/ui/Badge";
import { Breadcrumb, BreadcrumbItem } from "@/components/ui/Breadcrumb";

export interface PageHeaderProps {
  title: string;
  description?: string;
  breadcrumbs?: string[] | BreadcrumbItem[];
  contractPending?: boolean;
  action?: React.ReactNode;
}

export function PageHeader({
  title,
  description,
  breadcrumbs = ["Operations", title],
  contractPending = false,
  action,
}: PageHeaderProps) {
  const formattedCrumbs: BreadcrumbItem[] = Array.isArray(breadcrumbs)
    ? breadcrumbs.map((b) => (typeof b === "string" ? { label: b } : b))
    : [{ label: title }];

  return (
    <div className="mb-6 flex flex-col gap-4 border-b border-slate-200 pb-5 sm:flex-row sm:items-center sm:justify-between">
      <div>
        {/* Breadcrumb Hierarchy Navigation */}
        <Breadcrumb items={formattedCrumbs} className="mb-2" />

        <div className="flex flex-wrap items-center gap-3">
          <h1 className="text-xl font-bold tracking-tight text-slate-900">{title}</h1>
          {contractPending && (
            <Badge variant="warning" size="sm" className="text-[10px] uppercase font-mono">
              Pending API Contract
            </Badge>
          )}
        </div>
        {description && <p className="mt-1 text-xs text-slate-500 leading-relaxed">{description}</p>}
      </div>

      {action && <div className="flex items-center gap-2 shrink-0">{action}</div>}
    </div>
  );
}
