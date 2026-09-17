'use client';

import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Activity, ShieldCheck, Zap } from 'lucide-react';

export function StreamStatusTicker() {
  const [tps, setTps] = useState(1420);
  const [anomalyRate, setAnomalyRate] = useState(0.04);

  useEffect(() => {
    const interval = setInterval(() => {
      // Fluctuate slightly to feel alive
      const deltaTps = Math.floor(Math.random() * 25) - 12;
      setTps((prev) => Math.max(1200, Math.min(1800, prev + deltaTps)));

      if (Math.random() > 0.7) {
        setAnomalyRate(Number((0.02 + Math.random() * 0.05).toFixed(2)));
      }
    }, 3000);

    return () => clearInterval(interval);
  }, []);

  return (
    <div className="hidden xl:flex items-center gap-2 rounded-full border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/60 px-3 py-1 text-xs font-mono">
      <span className="relative flex h-2 w-2">
        <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
        <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500" />
      </span>

      <span className="text-slate-500 dark:text-slate-400">Stream:</span>
      <AnimatePresence mode="wait">
        <motion.span
          key={tps}
          initial={{ opacity: 0, y: -4 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: 4 }}
          transition={{ duration: 0.2 }}
          className="font-bold text-slate-900 dark:text-slate-100"
        >
          {tps.toLocaleString()} TPS
        </motion.span>
      </AnimatePresence>

      <span className="text-slate-300 dark:text-slate-700">|</span>

      <span className="text-amber-600 dark:text-amber-400 font-semibold flex items-center gap-1">
        <Zap className="h-3 w-3" />
        {anomalyRate}% Anomaly
      </span>
    </div>
  );
}
