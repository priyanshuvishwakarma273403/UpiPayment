"use client";

import React from "react";
import { Sidebar } from "./Sidebar";
import { Topbar } from "./Topbar";
import { CommandPalette } from "./CommandPalette";
import { NotificationsDrawer } from "./NotificationsDrawer";
import { HelpModal } from "./HelpModal";
import { ToastContainer } from "@/components/ui/Toast";

export function AppShell({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex h-screen w-screen overflow-hidden bg-slate-50 text-slate-900">
      {/* Sidebar Navigation */}
      <Sidebar />

      {/* Main Content Area */}
      <div className="flex flex-1 flex-col overflow-hidden">
        <Topbar />
        <main className="flex-1 overflow-y-auto p-4 md:p-6 lg:p-8">
          {children}
        </main>
      </div>

      {/* Global Modals, Drawers & Toast Containers */}
      <CommandPalette />
      <NotificationsDrawer />
      <HelpModal />
      <ToastContainer />
    </div>
  );
}
