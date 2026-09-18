'use client';

import React, { useState, useRef } from 'react';
import { motion, useMotionValue, useSpring, useTransform } from 'framer-motion';
import { ShieldCheck, Wifi, Cpu, Sparkles, RefreshCw, Lock, CheckCircle2 } from 'lucide-react';

interface Card3DProps {
  cardHolder?: string;
  upiId?: string;
  cardNumber?: string;
  expiry?: string;
  className?: string;
  showSkinSelector?: boolean;
}

export function Card3D({
  cardHolder = 'VIKRAM ADITYA',
  upiId = 'vikram@upimesh',
  cardNumber = '4289 9012 3345 8842',
  expiry = '10/29',
  className = '',
  showSkinSelector = true,
}: Card3DProps) {
  const cardRef = useRef<HTMLDivElement>(null);
  const [isFlipped, setIsFlipped] = useState(false);
  const [skin, setSkin] = useState<'obsidian' | 'cyber' | 'emerald'>('obsidian');

  // Mouse tilt animation coordinates
  const x = useMotionValue(0);
  const y = useMotionValue(0);

  const mouseXSpring = useSpring(x, { stiffness: 260, damping: 20 });
  const mouseYSpring = useSpring(y, { stiffness: 260, damping: 20 });

  const rotateX = useTransform(mouseYSpring, [-0.5, 0.5], ['18deg', '-18deg']);
  const rotateY = useTransform(mouseXSpring, [-0.5, 0.5], ['-18deg', '18deg']);

  // Dynamic Glare Position
  const glareX = useTransform(mouseXSpring, [-0.5, 0.5], ['0%', '100%']);
  const glareY = useTransform(mouseYSpring, [-0.5, 0.5], ['0%', '100%']);

  const handleMouseMove = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!cardRef.current) return;
    const rect = cardRef.current.getBoundingClientRect();
    const width = rect.width;
    const height = rect.height;
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;

    const xPct = mouseX / width - 0.5;
    const yPct = mouseY / height - 0.5;

    x.set(xPct);
    y.set(yPct);
  };

  const handleMouseLeave = () => {
    x.set(0);
    y.set(0);
  };

  const skinStyles = {
    obsidian: {
      bg: 'from-slate-900 via-slate-950 to-black',
      border: 'border-slate-700/60 shadow-indigo-500/15',
      accent: 'text-indigo-400',
      badge: 'Platinum RuPay',
      glow: 'rgba(99, 102, 241, 0.25)',
    },
    cyber: {
      bg: 'from-indigo-950 via-purple-950 to-slate-950',
      border: 'border-indigo-500/50 shadow-purple-500/25',
      accent: 'text-cyan-400',
      badge: 'Cyber UPI 2.0',
      glow: 'rgba(168, 85, 247, 0.35)',
    },
    emerald: {
      bg: 'from-emerald-950 via-slate-950 to-teal-950',
      border: 'border-emerald-500/40 shadow-emerald-500/20',
      accent: 'text-emerald-400',
      badge: 'Sovereign Sovereign',
      glow: 'rgba(16, 185, 129, 0.3)',
    },
  };

  const currentSkin = skinStyles[skin];

  return (
    <div className={`relative flex flex-col items-center select-none ${className}`}>
      {/* 3D Card Container with Perspective */}
      <div className="perspective-1000 w-full max-w-[420px] aspect-[1.586/1]">
        <motion.div
          ref={cardRef}
          onMouseMove={handleMouseMove}
          onMouseLeave={handleMouseLeave}
          style={{
            rotateX: isFlipped ? 0 : rotateX,
            rotateY: isFlipped ? 180 : rotateY,
            transformStyle: 'preserve-3d',
          }}
          transition={{ type: 'spring', stiffness: 200, damping: 20 }}
          className="relative w-full h-full cursor-pointer transition-transform duration-500 rounded-2xl shadow-2xl"
          onClick={() => setIsFlipped(!isFlipped)}
        >
          {/* ================= FRONT OF CARD ================= */}
          <div
            className={`absolute inset-0 w-full h-full rounded-2xl p-6 flex flex-col justify-between border bg-gradient-to-br ${currentSkin.bg} ${currentSkin.border} text-white shadow-2xl overflow-hidden backface-hidden`}
          >
            {/* Holographic Specular Glare Overlay */}
            <motion.div
              className="absolute inset-0 pointer-events-none opacity-40 mix-blend-color-dodge transition-opacity duration-300"
              style={{
                background: `radial-gradient(circle at ${glareX} ${glareY}, rgba(255,255,255,0.45) 0%, rgba(255,255,255,0) 65%)`,
              }}
            />

            {/* Subtle Circuit Grid Pattern */}
            <div className="absolute inset-0 bg-[radial-gradient(#6366f1_1px,transparent_1px)] [background-size:20px_20px] opacity-15 pointer-events-none" />

            {/* Card Header */}
            <div className="relative z-10 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="h-7 w-7 rounded-lg bg-white/10 backdrop-blur-sm border border-white/20 flex items-center justify-center">
                  <ShieldCheck className="h-4 w-4 text-white" />
                </div>
                <div>
                  <div className="text-[11px] font-bold tracking-widest uppercase font-mono text-white/90">
                    UPI MESH SWITCH
                  </div>
                  <div className="text-[8px] font-mono text-white/50 tracking-wider">
                    RESERVE CONSORTIUM RAIL
                  </div>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <Wifi className="h-4 w-4 text-white/70 rotate-90" />
                <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded-full bg-white/10 border border-white/15 text-white/90 backdrop-blur-md">
                  {currentSkin.badge}
                </span>
              </div>
            </div>

            {/* Card Middle: Gold Chip & Contactless */}
            <div className="relative z-10 flex items-center justify-between my-2">
              {/* Realistic Gold EMV Chip */}
              <div className="relative w-11 h-9 rounded-md bg-gradient-to-br from-amber-200 via-amber-400 to-amber-600 border border-amber-300/80 shadow-inner flex items-center justify-center overflow-hidden">
                <div className="absolute inset-0 bg-[linear-gradient(45deg,transparent_45%,rgba(0,0,0,0.25)_45%,rgba(0,0,0,0.25)_55%,transparent_55%)]" />
                <div className="w-7 h-5 rounded-xs border border-amber-700/40 grid grid-cols-2 gap-0.5 opacity-80">
                  <div className="border-r border-b border-amber-800/40" />
                  <div className="border-b border-amber-800/40" />
                  <div className="border-r border-amber-800/40" />
                  <div />
                </div>
              </div>

              {/* Cryptographic RSA Seal Indicator */}
              <div className="flex items-center gap-1.5 px-2 py-1 rounded-lg bg-black/40 border border-white/10 backdrop-blur-xs text-[10px] font-mono text-indigo-300">
                <Lock className="h-3 w-3 text-indigo-400" />
                <span>RSA-2048</span>
              </div>
            </div>

            {/* Card Footer: Numbers, Name & RuPay/UPI Badge */}
            <div className="relative z-10 space-y-3">
              <div className="font-mono text-lg sm:text-xl tracking-[0.22em] text-white font-medium drop-shadow-md">
                {cardNumber}
              </div>

              <div className="flex items-end justify-between">
                <div>
                  <div className="text-[9px] uppercase tracking-wider text-white/50 font-mono">
                    Authorized Holder • UPI ID
                  </div>
                  <div className="text-xs font-bold tracking-wider text-white font-mono flex items-center gap-1.5">
                    <span>{cardHolder}</span>
                    <span className="text-white/40">•</span>
                    <span className="text-indigo-300 font-normal">{upiId}</span>
                  </div>
                </div>

                <div className="text-right">
                  <div className="text-[8px] uppercase tracking-wider text-white/50 font-mono">
                    VALID THRU
                  </div>
                  <div className="text-xs font-bold font-mono text-white tracking-widest">
                    {expiry}
                  </div>
                </div>

                {/* RuPay & UPI Dual Logo */}
                <div className="flex items-center gap-1.5 pl-2">
                  <div className="h-7 px-2 rounded bg-white flex items-center justify-center shadow-md">
                    <span className="text-[11px] font-black tracking-tight text-slate-900 font-sans">
                      RuPay<span className="text-amber-500 font-bold">❯</span>
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Flip Hint */}
            <div className="absolute bottom-1 left-1/2 -translate-x-1/2 text-[8px] font-mono text-white/30 tracking-widest flex items-center gap-1">
              <RefreshCw className="h-2 w-2 animate-spin" /> CLICK TO FLIP
            </div>
          </div>

          {/* ================= BACK OF CARD ================= */}
          <div
            className={`absolute inset-0 w-full h-full rounded-2xl pt-6 pb-5 flex flex-col justify-between border bg-gradient-to-br ${currentSkin.bg} ${currentSkin.border} text-white shadow-2xl overflow-hidden backface-hidden rotate-y-180`}
          >
            {/* Magnetic Stripe */}
            <div className="w-full h-10 bg-black/90 shadow-inner border-y border-white/10" />

            {/* Signature Strip & CVV */}
            <div className="px-6 space-y-2">
              <div className="flex items-center gap-3">
                <div className="flex-1 h-8 rounded bg-slate-200 text-slate-800 flex items-center px-3 text-[10px] font-mono italic tracking-wider shadow-inner">
                  {cardHolder} • NOT VALID WITHOUT SIGNATURE
                </div>
                <div className="h-8 px-3 rounded bg-white text-slate-900 font-mono font-black flex items-center text-xs tracking-widest shadow-md">
                  892
                </div>
              </div>

              <div className="text-[8px] text-white/50 font-mono leading-tight">
                This electronic payment instrument is issued in compliance with RBI Master Directions on Prepaid Payment Instruments and UPI 2.0 specs. Protected by 2048-bit RSA digital signatures and AES-256-GCM field encryption.
              </div>
            </div>

            {/* Back Security Badges */}
            <div className="px-6 flex items-center justify-between border-t border-white/10 pt-3">
              <div className="flex items-center gap-2">
                <div className="h-5 w-5 rounded bg-emerald-500/20 border border-emerald-500/40 flex items-center justify-center">
                  <CheckCircle2 className="h-3 w-3 text-emerald-400" />
                </div>
                <span className="text-[9px] font-mono text-emerald-400">Zero-Trust Verified</span>
              </div>

              <div className="text-[9px] font-mono text-white/60">
                24/7 Switch Support: <span className="text-indigo-400">1800-UPI-MESH</span>
              </div>
            </div>
          </div>
        </motion.div>
      </div>

      {/* Interactive Skin Selector */}
      {showSkinSelector && (
        <div className="mt-5 flex items-center gap-2 rounded-full bg-slate-900/80 p-1 border border-slate-800 backdrop-blur-md">
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              setSkin('obsidian');
            }}
            className={`px-2.5 py-1 text-[10px] font-mono rounded-full transition-all ${
              skin === 'obsidian'
                ? 'bg-indigo-600 text-white font-bold shadow-sm'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            Obsidian Platinum
          </button>
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              setSkin('cyber');
            }}
            className={`px-2.5 py-1 text-[10px] font-mono rounded-full transition-all ${
              skin === 'cyber'
                ? 'bg-purple-600 text-white font-bold shadow-sm'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            Cyber UPI 2.0
          </button>
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              setSkin('emerald');
            }}
            className={`px-2.5 py-1 text-[10px] font-mono rounded-full transition-all ${
              skin === 'emerald'
                ? 'bg-emerald-600 text-white font-bold shadow-sm'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            Sovereign Gold
          </button>
        </div>
      )}
    </div>
  );
}
