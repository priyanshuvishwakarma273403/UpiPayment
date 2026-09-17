"use client";

import React from "react";
import { Modal } from "@/components/ui/Modal";
import { useUIStore } from "@/stores/uiStore";
import { BookOpen, Cpu, ShieldCheck, ExternalLink } from "lucide-react";
import Link from "next/link";

export function HelpModal() {
  const isOpen = useUIStore((state) => state.helpModalOpen);
  const setOpen = useUIStore((state) => state.setHelpModalOpen);

  return (
    <Modal
      isOpen={isOpen}
      onClose={() => setOpen(false)}
      title="SentinelX Analyst Support & Documentation"
      size="lg"
    >
      <div className="space-y-4 text-xs text-slate-700">
        <div className="rounded-lg bg-blue-50 p-4 border border-blue-200">
          <h4 className="font-semibold text-blue-900 flex items-center gap-2">
            <ShieldCheck className="h-4 w-4 text-blue-700" />
            Financial Intelligence Platform Operating Guide
          </h4>
          <p className="mt-1 text-slate-600 leading-relaxed">
            SentinelX provides real-time fraud monitoring, entity graph analysis, policy compliance RAG, and automated case investigation.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <Link
            href="/knowledge"
            onClick={() => setOpen(false)}
            className="flex items-start gap-3 rounded-lg border border-slate-200 p-3 hover:bg-slate-50 transition-colors"
          >
            <BookOpen className="h-5 w-5 text-blue-600 shrink-0 mt-0.5" />
            <div>
              <h5 className="font-semibold text-slate-900 flex items-center gap-1">
                Policy RAG Knowledge <ExternalLink className="h-3 w-3 text-slate-400" />
              </h5>
              <p className="text-[11px] text-slate-500 mt-0.5">Search runbooks, risk rules, and compliance guidelines.</p>
            </div>
          </Link>

          <Link
            href="/mcp"
            onClick={() => setOpen(false)}
            className="flex items-start gap-3 rounded-lg border border-slate-200 p-3 hover:bg-slate-50 transition-colors"
          >
            <Cpu className="h-5 w-5 text-blue-600 shrink-0 mt-0.5" />
            <div>
              <h5 className="font-semibold text-slate-900 flex items-center gap-1">
                MCP Tools Registry <ExternalLink className="h-3 w-3 text-slate-400" />
              </h5>
              <p className="text-[11px] text-slate-500 mt-0.5">Inspect bounded investigation tools and confirmation rules.</p>
            </div>
          </Link>
        </div>

        <div className="border-t border-slate-100 pt-3 flex justify-between items-center text-[11px] text-slate-400">
          <span>Keyboard shortcut: <kbd className="bg-slate-100 border border-slate-300 rounded px-1 text-slate-700 font-mono">⌘K</kbd> for global search</span>
          <span>SentinelX Docs v1.0</span>
        </div>
      </div>
    </Modal>
  );
}
