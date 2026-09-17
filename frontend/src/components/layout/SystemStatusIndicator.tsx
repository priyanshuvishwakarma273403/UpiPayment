"use client";

import React, { useState } from "react";
import { useUIStore, SystemHealthStatus } from "@/stores/uiStore";
import { Badge } from "@/components/ui/Badge";
import { Activity, ShieldAlert, CheckCircle2, AlertTriangle, XCircle } from "lucide-react";

export function SystemStatusIndicator() {
  const systemStatus = useUIStore((state) => state.systemStatus);
  const setSystemStatus = useUIStore((state) => state.setSystemStatus);
  const [popoverOpen, setPopoverOpen] = useState(false);

  const statusConfigs: Record<
    SystemHealthStatus,
    { label: string; dotColor: string; badgeVariant: "success" | "warning" | "danger"; icon: React.ReactNode }
  > = {
    operational: {
      label: "Operational",
      dotColor: "bg-emerald-500",
      badgeVariant: "success",
      icon: <CheckCircle2 className="h-4 w-4 text-emerald-600" />,
    },
    degraded: {
      label: "Degraded Performance",
      dotColor: "bg-amber-500",
      badgeVariant: "warning",
      icon: <AlertTriangle className="h-4 w-4 text-amber-600" />,
    },
    unavailable: {
      label: "System Outage",
      dotColor: "bg-red-500",
      badgeVariant: "danger",
      icon: <XCircle className="h-4 w-4 text-red-600" />,
    },
  };

  const config = statusConfigs[systemStatus];

  return (
    <div className="relative inline-block text-left">
      <button
        type="button"
        onClick={() => setPopoverOpen((prev) => !prev)}
        className="flex items-center gap-2 rounded-md border border-slate-200 bg-slate-50 px-2.5 py-1 text-xs text-slate-700 hover:bg-slate-100 transition-colors focus:outline-none focus:ring-1 focus:ring-blue-600"
      >
        <span className={`h-2 w-2 rounded-full ${config.dotColor} animate-pulse`} />
        <span className="font-medium hidden sm:inline">{config.label}</span>
      </button>

      {popoverOpen && (
        <div className="absolute right-0 z-50 mt-2 w-72 rounded-lg border border-slate-200 bg-white p-4 shadow-xl ring-1 ring-black/5 animate-in fade-in duration-150">
          <div className="flex items-center justify-between border-b border-slate-100 pb-2.5">
            <div className="flex items-center gap-2">
              <Activity className="h-4 w-4 text-blue-600" />
              <h4 className="text-xs font-bold text-slate-900">System Telemetry Contract</h4>
            </div>
            <Badge variant="warning" size="sm" className="font-mono text-[9px] uppercase">
              HEALTH_CONTRACT_PENDING
            </Badge>
          </div>

          <div className="mt-3 space-y-2 text-xs text-slate-600">
            <div className="flex items-center justify-between rounded-md bg-slate-50 p-2 border border-slate-100">
              <span>Status Level:</span>
              <Badge variant={config.badgeVariant} size="sm">
                {config.label}
              </Badge>
            </div>
            <div className="flex items-center justify-between rounded-md bg-slate-50 p-2 border border-slate-100 font-mono text-[11px]">
              <span>API Gateway:</span>
              <span className="text-emerald-700 font-semibold">200 OK</span>
            </div>
            <div className="flex items-center justify-between rounded-md bg-slate-50 p-2 border border-slate-100 font-mono text-[11px]">
              <span>Latency (p99):</span>
              <span>18ms</span>
            </div>
          </div>

          {/* Status Simulation Controls (For UI Testing) */}
          <div className="mt-4 border-t border-slate-100 pt-2.5">
            <p className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider mb-1.5">
              UI Contract Status Override
            </p>
            <div className="grid grid-cols-3 gap-1">
              <button
                type="button"
                onClick={() => setSystemStatus("operational")}
                className="rounded bg-emerald-50 py-1 text-[10px] font-semibold text-emerald-700 hover:bg-emerald-100"
              >
                Operational
              </button>
              <button
                type="button"
                onClick={() => setSystemStatus("degraded")}
                className="rounded bg-amber-50 py-1 text-[10px] font-semibold text-amber-800 hover:bg-amber-100"
              >
                Degraded
              </button>
              <button
                type="button"
                onClick={() => setSystemStatus("unavailable")}
                className="rounded bg-red-50 py-1 text-[10px] font-semibold text-red-700 hover:bg-red-100"
              >
                Outage
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
