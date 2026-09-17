"use client";

import React, { useEffect, useState } from "react";
import { useUIStore } from "@/stores/uiStore";
import { Search, ArrowRight, X } from "lucide-react";
import { PRIMARY_NAVIGATION, SECONDARY_NAVIGATION } from "@/config/navigation.config";
import { useRouter } from "next/navigation";

export function CommandPalette() {
  const isOpen = useUIStore((state) => state.commandPaletteOpen);
  const setOpen = useUIStore((state) => state.setCommandPaletteOpen);
  const [query, setQuery] = useState("");
  const router = useRouter();

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === "k") {
        e.preventDefault();
        setOpen(!isOpen);
      }
      if (e.key === "Escape" && isOpen) {
        setOpen(false);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, setOpen]);

  if (!isOpen) return null;

  const allItems = [...PRIMARY_NAVIGATION, ...SECONDARY_NAVIGATION];
  const filtered = query.trim()
    ? allItems.filter((item) => item.title.toLowerCase().includes(query.toLowerCase()) || item.href.includes(query.toLowerCase()))
    : allItems;

  const handleNavigate = (href: string) => {
    router.push(href);
    setOpen(false);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center bg-slate-900/50 backdrop-blur-xs pt-20 p-4 animate-in fade-in duration-150">
      <div className="w-full max-w-xl rounded-xl border border-slate-200 bg-white shadow-2xl overflow-hidden animate-in zoom-in-95 duration-150">
        {/* Search Header Input */}
        <div className="flex items-center border-b border-slate-200 px-4 py-3 bg-slate-50">
          <Search className="h-5 w-5 text-slate-400 mr-3 shrink-0" />
          <input
            type="text"
            autoFocus
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Type a command or search route (e.g. Transactions, Risk, AI Investigator)..."
            className="w-full bg-transparent text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none"
          />
          <button onClick={() => setOpen(false)} className="p-1 text-slate-400 hover:text-slate-600 rounded-md">
            <X className="h-4 w-4" />
          </button>
        </div>

        {/* Results List */}
        <div className="max-h-80 overflow-y-auto p-2">
          {filtered.length === 0 ? (
            <div className="p-6 text-center text-xs text-slate-500">No matching routes or commands found.</div>
          ) : (
            <div className="space-y-1">
              <p className="px-3 py-1.5 text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
                Platform Routes ({filtered.length})
              </p>
              {filtered.map((item) => {
                const Icon = item.icon;
                return (
                  <button
                    key={item.href}
                    onClick={() => handleNavigate(item.href)}
                    className="flex w-full items-center justify-between rounded-lg px-3 py-2 text-xs text-left text-slate-700 hover:bg-blue-50 hover:text-blue-900 transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <Icon className="h-4 w-4 text-slate-400 group-hover:text-blue-600" />
                      <span className="font-semibold">{item.title}</span>
                      <span className="font-mono text-[10px] text-slate-400">{item.href}</span>
                    </div>
                    <ArrowRight className="h-3.5 w-3.5 text-slate-300" />
                  </button>
                );
              })}
            </div>
          )}
        </div>

        {/* Footer info */}
        <div className="border-t border-slate-100 bg-slate-50 px-4 py-2 text-[11px] text-slate-400 flex justify-between">
          <span>Use arrow keys or click to navigate</span>
          <span className="font-mono">ESC to close</span>
        </div>
      </div>
    </div>
  );
}
