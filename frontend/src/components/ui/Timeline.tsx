import React from "react";
import { cn } from "@/lib/utils";
import { Badge, BadgeStatusVariant } from "./Badge";

export interface TimelineEvent {
  id: string;
  timestamp: string;
  title: string;
  description?: string;
  actor?: string;
  status?: BadgeStatusVariant;
  statusText?: string;
  icon?: React.ReactNode;
}

export interface TimelineProps {
  events?: TimelineEvent[];
  items?: Array<{ id?: string; title: string; timestamp: string; description?: string; actor?: string; status?: BadgeStatusVariant; statusText?: string }>;
  className?: string;
}

export function Timeline({ events, items, className }: TimelineProps) {
  const normalizedEvents: TimelineEvent[] = (events || items || []).map((evt, idx) => ({
    id: evt.id || `evt-${idx}`,
    title: evt.title,
    timestamp: evt.timestamp,
    description: evt.description,
    actor: evt.actor,
    status: evt.status,
    statusText: evt.statusText,
  }));

  if (normalizedEvents.length === 0) return null;

  return (
    <div className={cn("relative space-y-6 pl-6 before:absolute before:left-2 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-200", className)}>
      {normalizedEvents.map((evt) => (
        <div key={evt.id} className="relative flex flex-col gap-1 text-xs">
          {/* Node Icon */}
          <div className="absolute -left-6 top-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-white ring-4 ring-white">
            {evt.icon ? (
              <span className="text-slate-600">{evt.icon}</span>
            ) : (
              <span className="h-2 w-2 rounded-full bg-blue-600" />
            )}
          </div>

          <div className="flex items-center justify-between gap-2">
            <h4 className="font-semibold text-slate-900">{evt.title}</h4>
            <span className="font-mono text-[11px] text-slate-400">{evt.timestamp}</span>
          </div>

          {evt.description && <p className="text-slate-600 leading-relaxed mt-0.5">{evt.description}</p>}

          <div className="flex items-center gap-2 mt-1">
            {evt.actor && <span className="font-mono text-[10px] text-slate-500">By: {evt.actor}</span>}
            {evt.statusText && (
              <Badge variant={evt.status || "neutral"} size="sm">
                {evt.statusText}
              </Badge>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}
