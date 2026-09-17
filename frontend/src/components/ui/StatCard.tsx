import React from "react";
import { Card } from "./Card";
import { Badge, BadgeStatusVariant } from "./Badge";
import { TrendingUp, TrendingDown, Minus } from "lucide-react";
import { cn } from "@/lib/utils";

export interface StatCardProps {
  title: string;
  value: string | number;
  change?: string;
  changeType?: "increase" | "decrease" | "neutral";
  comparisonText?: string;
  icon?: React.ReactNode;
  statusVariant?: BadgeStatusVariant;
  className?: string;
}

export function StatCard({
  title,
  value,
  change,
  changeType = "neutral",
  comparisonText = "vs previous window",
  icon,
  statusVariant = "neutral",
  className,
}: StatCardProps) {
  const trendIcons = {
    increase: <TrendingUp className="h-3 w-3 text-emerald-600" />,
    decrease: <TrendingDown className="h-3 w-3 text-red-600" />,
    neutral: <Minus className="h-3 w-3 text-slate-500" />,
  };

  return (
    <Card className={cn("p-5", className)}>
      <div className="flex items-center justify-between border-b border-slate-100 pb-3">
        <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">{title}</span>
        {icon && <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-700">{icon}</div>}
      </div>

      <div className="mt-3 flex items-baseline justify-between">
        <span className="text-2xl font-bold tracking-tight text-slate-900 font-mono">{value}</span>
        {change && (
          <Badge variant={statusVariant} size="sm">
            {trendIcons[changeType]}
            <span>{change}</span>
          </Badge>
        )}
      </div>

      {comparisonText && (
        <p className="mt-2 text-[11px] text-slate-400 font-medium">{comparisonText}</p>
      )}
    </Card>
  );
}
