'use client';

import React, { useEffect, useRef } from 'react';
import gsap from 'gsap';
import { User, Smartphone, Building2, ShieldAlert, Zap } from 'lucide-react';
import { Badge } from '@/components/ui/Badge';

export function GsapNetworkCanvas() {
  const particle1Ref = useRef<HTMLDivElement>(null);
  const particle2Ref = useRef<HTMLDivElement>(null);
  const particle3Ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // GSAP Timeline animation for data-flow particles moving along financial network lines
    const ctx = gsap.context(() => {
      if (particle1Ref.current) {
        gsap.to(particle1Ref.current, {
          x: 180,
          repeat: -1,
          duration: 2.2,
          ease: 'power1.inOut',
          yoyo: false,
        });
      }
      if (particle2Ref.current) {
        gsap.to(particle2Ref.current, {
          x: 180,
          repeat: -1,
          duration: 1.8,
          delay: 0.5,
          ease: 'power2.inOut',
        });
      }
      if (particle3Ref.current) {
        gsap.to(particle3Ref.current, {
          x: 180,
          repeat: -1,
          duration: 2.5,
          delay: 1.0,
          ease: 'power1.out',
        });
      }
    });

    return () => ctx.revert();
  }, []);

  return (
    <div className="rounded-xl border border-slate-800 bg-slate-950 p-6 relative overflow-hidden text-white shadow-2xl">
      {/* Radial Background Grid */}
      <div className="absolute inset-0 bg-[radial-gradient(#334155_1px,transparent_1px)] [background-size:24px_24px] opacity-30 pointer-events-none" />

      {/* Top Header */}
      <div className="relative z-10 flex items-center justify-between border-b border-slate-800 pb-3 mb-6 font-mono text-xs">
        <div className="flex items-center gap-2">
          <Zap className="h-4 w-4 text-emerald-400 animate-pulse" />
          <span className="font-bold text-slate-200">GSAP Animated Live Payment Flow Engine</span>
        </div>
        <Badge variant="danger" className="font-mono text-[10px]">
          REAL-TIME GRAPH PIPELINE
        </Badge>
      </div>

      {/* Animated GSAP Canvas Diagram */}
      <div className="relative z-10 grid grid-cols-1 md:grid-cols-3 gap-8 items-center py-4">
        {/* Node 1: Customer Node */}
        <div className="p-4 rounded-2xl bg-slate-900/90 border border-indigo-500/40 text-center shadow-lg relative group hover:border-indigo-500 transition-colors">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-600 text-white mx-auto mb-2 shadow-md">
            <User className="h-5 w-5" />
          </div>
          <span className="font-mono font-bold text-xs text-indigo-300 block">anand@okaxis</span>
          <span className="text-[10px] text-slate-400 block font-mono">Originator (CUST-4091)</span>
          <div className="mt-2 inline-block px-2 py-0.5 rounded bg-indigo-950 text-indigo-300 text-[10px] font-mono">
            Score: 88/100
          </div>
        </div>

        {/* Data Line 1 & GSAP Particle */}
        <div className="hidden md:flex flex-col items-center justify-center relative">
          <div className="w-full h-1 bg-slate-800 rounded-full relative overflow-hidden">
            <div
              ref={particle1Ref}
              className="h-full w-8 bg-gradient-to-r from-transparent via-amber-400 to-transparent rounded-full shadow-[0_0_12px_#f59e0b]"
            />
          </div>
          <span className="text-[10px] font-mono text-slate-500 mt-2">VPN Proxy Telemetry</span>
        </div>

        {/* Node 2: Shared Device & IP Node */}
        <div className="p-4 rounded-2xl bg-slate-900/90 border border-amber-500/40 text-center shadow-lg relative group hover:border-amber-500 transition-colors">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-600 text-white mx-auto mb-2 shadow-md">
            <Smartphone className="h-5 w-5" />
          </div>
          <span className="font-mono font-bold text-xs text-amber-300 block">DEV-A990-21X</span>
          <span className="text-[10px] text-slate-400 block font-mono">Fingerprint (Android 14)</span>
          <div className="mt-2 inline-block px-2 py-0.5 rounded bg-amber-950 text-amber-300 text-[10px] font-mono">
            VPN Proxy Detected
          </div>
        </div>
      </div>

      <div className="relative z-10 text-[11px] font-mono text-slate-400 flex items-center justify-between border-t border-slate-800/80 pt-3 mt-2">
        <span>GSAP Hardware-Accelerated Animation Active</span>
        <span className="text-emerald-400">FPS: 60 • Latency: 3.4ms</span>
      </div>
    </div>
  );
}
