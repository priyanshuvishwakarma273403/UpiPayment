"use client";

import React, { useState } from "react";
import { cn } from "@/lib/utils";

export interface TabItem {
  id: string;
  label: string;
  count?: number;
  icon?: React.ReactNode;
  content?: React.ReactNode;
}

export interface TabsProps {
  tabs: TabItem[];
  activeTab?: string;
  defaultTabId?: string;
  onChange?: (tabId: string) => void;
  className?: string;
}

export function Tabs({ tabs, activeTab: externalActiveTab, defaultTabId, onChange, className }: TabsProps) {
  const [internalActiveTabId, setInternalActiveTabId] = useState(defaultTabId || tabs[0]?.id);
  const activeTabId = externalActiveTab || internalActiveTabId;

  const handleTabClick = (id: string) => {
    setInternalActiveTabId(id);
    onChange?.(id);
  };

  const activeTab = tabs.find((t) => t.id === activeTabId);

  return (
    <div className={cn("w-full space-y-4", className)}>
      <div className="border-b border-slate-200">
        <nav className="-mb-px flex space-x-6 overflow-x-auto" aria-label="Tabs">
          {tabs.map((tab) => {
            const isActive = tab.id === activeTabId;
            return (
              <button
                key={tab.id}
                onClick={() => handleTabClick(tab.id)}
                className={cn(
                  "flex items-center gap-2 whitespace-nowrap border-b-2 py-3 text-xs font-semibold transition-colors focus:outline-none",
                  isActive
                    ? "border-blue-600 text-blue-700"
                    : "border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700"
                )}
              >
                {tab.icon && <span className="h-4 w-4 shrink-0">{tab.icon}</span>}
                <span>{tab.label}</span>
                {typeof tab.count === "number" && (
                  <span
                    className={cn(
                      "rounded-full px-2 py-0.5 text-[10px] font-mono",
                      isActive ? "bg-blue-100 text-blue-800" : "bg-slate-100 text-slate-600"
                    )}
                  >
                    {tab.count}
                  </span>
                )}
              </button>
            );
          })}
        </nav>
      </div>
      {activeTab?.content && <div className="pt-2">{activeTab.content}</div>}
    </div>
  );
}
