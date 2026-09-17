"use client";

import { create } from "zustand";

export type SidebarMode = "expanded" | "collapsed" | "mobile-open";
export type SystemHealthStatus = "operational" | "degraded" | "unavailable";

export interface ToastMessage {
  id: string;
  title: string;
  description?: string;
  variant?: "success" | "warning" | "danger" | "info" | "neutral";
  duration?: number;
}

interface UIStoreState {
  sidebarMode: SidebarMode;
  commandPaletteOpen: boolean;
  notificationsDrawerOpen: boolean;
  helpModalOpen: boolean;
  systemStatus: SystemHealthStatus;
  toasts: ToastMessage[];

  // Actions
  setSidebarMode: (mode: SidebarMode) => void;
  toggleSidebarCollapsed: () => void;
  toggleMobileSidebar: () => void;
  setCommandPaletteOpen: (open: boolean) => void;
  setNotificationsDrawerOpen: (open: boolean) => void;
  setHelpModalOpen: (open: boolean) => void;
  setSystemStatus: (status: SystemHealthStatus) => void;
  addToast: (toast: Omit<ToastMessage, "id">) => void;
  removeToast: (id: string) => void;
}

export const useUIStore = create<UIStoreState>((set, get) => ({
  sidebarMode: typeof window !== "undefined"
    ? (localStorage.getItem("sentinelx_sidebar_mode") as SidebarMode) || "expanded"
    : "expanded",
  commandPaletteOpen: false,
  notificationsDrawerOpen: false,
  helpModalOpen: false,
  systemStatus: "operational",
  toasts: [],

  setSidebarMode: (mode) => {
    if (typeof window !== "undefined" && (mode === "expanded" || mode === "collapsed")) {
      localStorage.setItem("sentinelx_sidebar_mode", mode);
    }
    set({ sidebarMode: mode });
  },

  toggleSidebarCollapsed: () => {
    const current = get().sidebarMode;
    const nextMode = current === "collapsed" ? "expanded" : "collapsed";
    if (typeof window !== "undefined") {
      localStorage.setItem("sentinelx_sidebar_mode", nextMode);
    }
    set({ sidebarMode: nextMode });
  },

  toggleMobileSidebar: () => {
    const current = get().sidebarMode;
    set({ sidebarMode: current === "mobile-open" ? "expanded" : "mobile-open" });
  },

  setCommandPaletteOpen: (open) => set({ commandPaletteOpen: open }),
  setNotificationsDrawerOpen: (open) => set({ notificationsDrawerOpen: open }),
  setHelpModalOpen: (open) => set({ helpModalOpen: open }),
  setSystemStatus: (status) => set({ systemStatus: status }),

  addToast: (toast) => {
    const id = `toast-${Date.now()}-${Math.random().toString(36).substring(2, 7)}`;
    const newToast = { ...toast, id };
    set((state) => ({ toasts: [...state.toasts, newToast] }));

    const duration = toast.duration || 4000;
    setTimeout(() => {
      get().removeToast(id);
    }, duration);
  },

  removeToast: (id) => {
    set((state) => ({ toasts: state.toasts.filter((t) => t.id !== id) }));
  },
}));
