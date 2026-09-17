"use client";

import React from "react";
import { useUIStore } from "@/stores/uiStore";
import { Drawer } from "@/components/ui/Drawer";
import { Badge } from "@/components/ui/Badge";
import { Bell, ShieldAlert, CheckCircle2, Info, Clock } from "lucide-react";

export function NotificationsDrawer() {
  const isOpen = useUIStore((state) => state.notificationsDrawerOpen);
  const setOpen = useUIStore((state) => state.setNotificationsDrawerOpen);

  const sampleAlerts = [
    {
      id: "N-101",
      title: "High Velocity Spike Detected",
      description: "Customer C9281 executed 14 UPI transfers in 90 seconds from 2 distinct IP ranges.",
      timestamp: "2 mins ago",
      variant: "danger" as const,
    },
    {
      id: "N-102",
      title: "ML Model Deployment Approved",
      description: "xgb_fraud_detector_v2.4 promoted to production candidate by Lead Model Risk Auditor.",
      timestamp: "18 mins ago",
      variant: "success" as const,
    },
    {
      id: "N-103",
      title: "New Watchlist Match",
      description: "Beneficiary VPA target@upi flagged on National Cyber Crime Watchlist.",
      timestamp: "1 hour ago",
      variant: "warning" as const,
    },
  ];

  return (
    <Drawer
      isOpen={isOpen}
      onClose={() => setOpen(false)}
      title="System Notifications & Alerts"
      position="right"
      size="md"
    >
      <div className="space-y-4">
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Recent Activity</span>
          <Badge variant="warning" size="sm" className="font-mono text-[9px]">
            NOTIF_CONTRACT_PENDING
          </Badge>
        </div>

        <div className="space-y-3">
          {sampleAlerts.map((alert) => (
            <div
              key={alert.id}
              className="rounded-lg border border-slate-200 bg-white p-4 shadow-xs hover:border-blue-300 transition-colors space-y-2"
            >
              <div className="flex items-center justify-between">
                <Badge variant={alert.variant} size="sm">
                  {alert.title}
                </Badge>
                <div className="flex items-center gap-1 text-[11px] text-slate-400 font-mono">
                  <Clock className="h-3 w-3" />
                  <span>{alert.timestamp}</span>
                </div>
              </div>
              <p className="text-xs text-slate-600 leading-relaxed">{alert.description}</p>
            </div>
          ))}
        </div>
      </div>
    </Drawer>
  );
}
