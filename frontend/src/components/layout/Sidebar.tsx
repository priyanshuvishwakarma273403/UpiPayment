"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { NAVIGATION_CONFIG } from "@/config/navigation.config";
import { useUIStore } from "@/stores/uiStore";
import { Tooltip } from "@/components/ui/Tooltip";
import { cn } from "@/lib/utils";
import { ShieldCheck, ChevronLeft, ChevronRight, X } from "lucide-react";

export function Sidebar({ className }: { className?: string }) {
  const pathname = usePathname();
  const sidebarMode = useUIStore((state) => state.sidebarMode);
  const toggleSidebarCollapsed = useUIStore((state) => state.toggleSidebarCollapsed);
  const setSidebarMode = useUIStore((state) => state.setSidebarMode);

  const isCollapsed = sidebarMode === "collapsed";
  const isMobileOpen = sidebarMode === "mobile-open";

  return (
    <>
      {/* Mobile Drawer Backdrop */}
      {isMobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/60 backdrop-blur-xs md:hidden"
          onClick={() => setSidebarMode("expanded")}
        />
      )}

      <aside
        className={cn(
          "flex flex-col border-r border-slate-800 bg-slate-900 text-slate-300 transition-all duration-200 ease-in-out z-40",
          isCollapsed ? "w-16" : "w-64",
          isMobileOpen
            ? "fixed inset-y-0 left-0 w-64 shadow-2xl animate-in slide-in-from-left duration-200"
            : "hidden md:flex h-screen",
          className
        )}
      >
        {/* Brand Header */}
        <div className="flex h-16 items-center justify-between border-b border-slate-800 px-4">
          <Link href="/dashboard" className="flex items-center gap-3">
            <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-blue-600 text-white shadow-sm">
              <ShieldCheck className="h-5 w-5" />
            </div>
            {!isCollapsed && (
              <div>
                <h1 className="font-semibold tracking-wide text-white text-base leading-tight">SentinelX</h1>
                <p className="text-[10px] font-medium tracking-wider text-slate-400 uppercase">Fraud Intelligence</p>
              </div>
            )}
          </Link>

          {isMobileOpen && (
            <button
              onClick={() => setSidebarMode("expanded")}
              className="md:hidden p-1.5 text-slate-400 hover:text-white rounded-md"
            >
              <X className="h-5 w-5" />
            </button>
          )}
        </div>

        {/* Navigation Groups */}
        <div className="flex-1 overflow-y-auto px-3 py-4 space-y-6">
          {NAVIGATION_CONFIG.map((group) => (
            <div key={group.section} className="space-y-1">
              {!isCollapsed && (
                <h2 className="px-3 text-[10px] font-bold uppercase tracking-wider text-slate-500">
                  {group.section}
                </h2>
              )}
              <nav className="space-y-1 mt-1">
                {group.items.map((item) => {
                  const isActive = pathname === item.href || (item.href !== "/dashboard" && pathname.startsWith(item.href));
                  const Icon = item.icon;

                  const linkContent = (
                    <Link
                      key={item.href}
                      href={item.href}
                      onClick={() => isMobileOpen && setSidebarMode("expanded")}
                      className={cn(
                        "flex items-center gap-3 rounded-md py-2 text-xs font-medium transition-colors",
                        isCollapsed ? "justify-center px-0 h-10" : "px-3",
                        isActive
                          ? "bg-blue-600 text-white shadow-xs"
                          : "text-slate-300 hover:bg-slate-800 hover:text-white"
                      )}
                    >
                      <Icon className={cn("h-4 w-4 shrink-0", isActive ? "text-white" : "text-slate-400")} />
                      {!isCollapsed && <span className="truncate">{item.title}</span>}
                    </Link>
                  );

                  return isCollapsed ? (
                    <Tooltip key={item.href} content={item.title} position="right">
                      {linkContent}
                    </Tooltip>
                  ) : (
                    linkContent
                  );
                })}
              </nav>
            </div>
          ))}
        </div>

        {/* Footer Collapse Toggle */}
        <div className="border-t border-slate-800 p-3 hidden md:flex items-center justify-between">
          {!isCollapsed && (
            <div className="text-[10px] text-slate-500 font-mono">
              SentinelX v1.0 • DEV
            </div>
          )}
          <button
            type="button"
            onClick={toggleSidebarCollapsed}
            className="flex h-8 w-8 items-center justify-center rounded-md text-slate-400 hover:bg-slate-800 hover:text-white transition-colors"
            aria-label={isCollapsed ? "Expand Sidebar" : "Collapse Sidebar"}
          >
            {isCollapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
          </button>
        </div>
      </aside>
    </>
  );
}
