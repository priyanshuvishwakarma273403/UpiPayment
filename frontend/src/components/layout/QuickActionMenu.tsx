'use client';

import React, { useState, useRef, useEffect } from 'react';
import Link from 'next/link';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, ShieldAlert, Sliders, Zap, FileText, Search, Play, Sparkles } from 'lucide-react';
import confetti from 'canvas-confetti';
import { useToast } from '@/lib/useToast';

export function QuickActionMenu() {
  const [isOpen, setIsOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const { toast } = useToast();

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const triggerAction = (actionName: string, path?: string) => {
    setIsOpen(false);
    toast(`Quick Action Executed: ${actionName}`, 'Dispatched to SentinelX microservices engine.', 'success');

    if (actionName.includes('Simulation') || actionName.includes('Rule')) {
      confetti({
        particleCount: 50,
        spread: 60,
        origin: { y: 0.1, x: 0.8 },
      });
    }
  };

  return (
    <div className="relative" ref={menuRef}>
      <button
        onClick={() => setIsOpen((prev) => !prev)}
        className="inline-flex items-center gap-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-700 active:bg-indigo-800 text-white px-3 py-1.5 text-xs font-semibold shadow-xs transition-all duration-150 hover:shadow-md"
      >
        <Plus className="h-3.5 w-3.5" />
        <span className="hidden sm:inline">Quick Action</span>
      </button>

      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, y: 8, scale: 0.96 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 8, scale: 0.96 }}
            transition={{ duration: 0.15, ease: 'easeOut' }}
            className="absolute right-0 mt-2 w-64 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-2 shadow-xl z-50 text-xs"
          >
            <div className="px-2 py-1.5 text-[10px] font-mono font-semibold text-slate-400 uppercase tracking-wider">
              Enforcement & Actions
            </div>

            <button
              onClick={() => triggerAction('Run Velocity Simulation')}
              className="w-full text-left flex items-center gap-2.5 rounded-lg px-2.5 py-2 text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              <div className="flex h-7 w-7 items-center justify-center rounded-md bg-amber-100 dark:bg-amber-950/60 text-amber-600 shrink-0">
                <Zap className="h-3.5 w-3.5" />
              </div>
              <div>
                <span className="font-semibold block">Run Fraud Simulation</span>
                <span className="text-[10px] text-slate-400">Inject 1,000 synthetic velocity txns</span>
              </div>
            </button>

            <Link
              href="/risk"
              onClick={() => setIsOpen(false)}
              className="w-full text-left flex items-center gap-2.5 rounded-lg px-2.5 py-2 text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              <div className="flex h-7 w-7 items-center justify-center rounded-md bg-indigo-100 dark:bg-indigo-950/60 text-indigo-600 shrink-0">
                <Sliders className="h-3.5 w-3.5" />
              </div>
              <div>
                <span className="font-semibold block">Create Detection Rule</span>
                <span className="text-[10px] text-slate-400">Hot-deploy CEL condition expression</span>
              </div>
            </Link>

            <Link
              href="/ai-investigator"
              onClick={() => setIsOpen(false)}
              className="w-full text-left flex items-center gap-2.5 rounded-lg px-2.5 py-2 text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              <div className="flex h-7 w-7 items-center justify-center rounded-md bg-purple-100 dark:bg-purple-950/60 text-purple-600 shrink-0">
                <Sparkles className="h-3.5 w-3.5" />
              </div>
              <div>
                <span className="font-semibold block">Launch AI Copilot</span>
                <span className="text-[10px] text-slate-400">Multi-turn fraud diagnostic LLM</span>
              </div>
            </Link>

            <Link
              href="/network"
              onClick={() => setIsOpen(false)}
              className="w-full text-left flex items-center gap-2.5 rounded-lg px-2.5 py-2 text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              <div className="flex h-7 w-7 items-center justify-center rounded-md bg-rose-100 dark:bg-rose-950/60 text-rose-600 shrink-0">
                <ShieldAlert className="h-3.5 w-3.5" />
              </div>
              <div>
                <span className="font-semibold block">Inspect Mule Ring Graph</span>
                <span className="text-[10px] text-slate-400">Multi-hop entity connection canvas</span>
              </div>
            </Link>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
