'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  ShieldCheck,
  ArrowRight,
  Menu,
  X,
  ChevronDown,
  Activity,
  Cpu,
  Layers,
  Lock,
  Zap,
  Network,
  Bot,
  FileSpreadsheet,
  Terminal,
  Sparkles,
  CreditCard
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export function PublicHeader({ onOpenSimulator }: { onOpenSimulator?: () => void }) {
  const pathname = usePathname();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [activeDropdown, setActiveDropdown] = useState<string | null>(null);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const platformItems = [
    {
      title: 'Core Switch Engine',
      desc: 'Sub-10ms UPI 2.0 payment routing and atomic dual-ledger settlement.',
      href: '/platform',
      icon: Zap,
      badge: 'UPI 2.0',
    },
    {
      title: 'SentinelX Risk Scorer',
      desc: 'Synchronous 0-1000 behavioral risk scoring powered by 9 rule strategies.',
      href: '/risk',
      icon: ShieldCheck,
      badge: '< 8ms',
    },
    {
      title: 'AI Investigator Copilot',
      desc: 'Autonomous LLM agent with SHAP feature attribution and policy RAG.',
      href: '/ai-investigator',
      icon: Bot,
      badge: 'Spring AI',
    },
    {
      title: 'Offline Cryptographic Mesh',
      desc: 'Peer-to-peer RSA-2048 signed payments syncing without cell network.',
      href: '/payments',
      icon: Network,
      badge: 'Non-Repudiation',
    },
  ];

  const solutionsItems = [
    {
      title: 'Instant UPI & RuPay Rails',
      desc: 'Dynamic amount-tagged QR generation, BharatQR, and inter-bank rails.',
      href: '/payments',
      icon: CreditCard,
    },
    {
      title: '3-Tier Regulatory KYC',
      desc: 'Aadhaar OTP with client AES-256-GCM encryption and 3D face liveness.',
      href: '/kyc',
      icon: Lock,
    },
    {
      title: '3-Way Reconciliation',
      desc: 'Automated ledger vs NPCI vs Bank CBS matching with instant Excel export.',
      href: '/reconciliation',
      icon: FileSpreadsheet,
    },
    {
      title: 'PMLA 2002 AML Watchdog',
      desc: 'Continuous sliding-window smurfing detection and Levenshtein PEP screening.',
      href: '/aml',
      icon: Activity,
    },
  ];

  const developerItems = [
    {
      title: 'Gateway API Specification',
      desc: 'Interactive Swagger & Postman collections for all 27 microservices.',
      href: '/developers',
      icon: Terminal,
    },
    {
      title: 'Cryptographic Security Standard',
      desc: 'Zero-Trust architecture, AES-256-GCM AEAD, and RSA-2048 blueprints.',
      href: '/security',
      icon: Lock,
    },
    {
      title: '27 Microservices Architecture',
      desc: 'Granular service ports, MySQL schemas, Kafka topics, and Eureka topology.',
      href: '/platform',
      icon: Layers,
    },
  ];

  return (
    <header
      className={`sticky top-0 z-50 w-full transition-all duration-300 ${
        scrolled
          ? 'bg-slate-950/90 backdrop-blur-xl border-b border-slate-800/80 shadow-2xl shadow-black/40'
          : 'bg-slate-950/70 backdrop-blur-md border-b border-white/5'
      }`}
    >
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Brand Logo */}
        <Link href="/" className="flex items-center gap-3 group">
          <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-500 via-blue-600 to-indigo-800 text-white shadow-lg shadow-indigo-500/20 ring-1 ring-white/20 transition-all duration-300 group-hover:scale-105 group-hover:shadow-indigo-500/40">
            <ShieldCheck className="h-5 w-5 text-white" />
            <div className="absolute inset-0 rounded-xl bg-white/20 opacity-0 transition-opacity group-hover:opacity-100" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-lg font-extrabold tracking-tight text-white font-mono">
                SENTINEL<span className="text-indigo-400">X</span>
              </span>
              <span className="inline-flex items-center gap-1 rounded-full bg-indigo-500/10 px-2 py-0.5 text-[9px] font-bold text-indigo-300 ring-1 ring-inset ring-indigo-500/30">
                <Sparkles className="h-2.5 w-2.5" /> UPI MESH
              </span>
            </div>
            <p className="text-[10px] font-medium tracking-wider text-slate-400 uppercase">
              Financial Fraud & Payment Switch
            </p>
          </div>
        </Link>

        {/* Live NPCI Network Telemetry Beacon */}
        <div className="hidden xl:flex items-center gap-2 rounded-full bg-slate-900/80 px-3 py-1 text-[11px] font-mono text-slate-300 border border-slate-800 shadow-inner">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
            <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500" />
          </span>
          <span className="text-slate-400">SWITCH:</span>
          <span className="font-bold text-emerald-400">99.99% UP</span>
          <span className="text-slate-600">|</span>
          <span className="text-slate-400">LATENCY:</span>
          <span className="font-bold text-blue-400">&lt; 8.4ms</span>
        </div>

        {/* Desktop Interactive Navigation with Rich Dropdowns */}
        <nav className="hidden lg:flex items-center space-x-1">
          {/* Platform Dropdown */}
          <div
            className="relative"
            onMouseEnter={() => setActiveDropdown('platform')}
            onMouseLeave={() => setActiveDropdown(null)}
          >
            <button className="flex items-center gap-1 px-3 py-2 text-xs font-semibold text-slate-300 hover:text-white rounded-lg transition-colors hover:bg-slate-900/60">
              Platform & Mesh
              <ChevronDown className={`h-3.5 w-3.5 transition-transform duration-200 ${activeDropdown === 'platform' ? 'rotate-180 text-indigo-400' : 'text-slate-500'}`} />
            </button>

            <AnimatePresence>
              {activeDropdown === 'platform' && (
                <motion.div
                  initial={{ opacity: 0, y: 8, scale: 0.98 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: 8, scale: 0.98 }}
                  transition={{ duration: 0.15 }}
                  className="absolute left-0 top-full mt-2 w-[420px] rounded-2xl border border-slate-800 bg-slate-950/95 p-3 shadow-2xl backdrop-blur-2xl ring-1 ring-white/10"
                >
                  <div className="grid grid-cols-1 gap-1">
                    {platformItems.map((item) => {
                      const Icon = item.icon;
                      return (
                        <Link
                          key={item.title}
                          href={item.href}
                          className="flex items-start gap-3 rounded-xl p-2.5 transition-colors hover:bg-slate-900/80 group"
                        >
                          <div className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-indigo-500/10 text-indigo-400 group-hover:bg-indigo-500 group-hover:text-white transition-colors">
                            <Icon className="h-4 w-4" />
                          </div>
                          <div className="flex-1">
                            <div className="flex items-center justify-between">
                              <span className="text-xs font-bold text-white group-hover:text-indigo-300 transition-colors">
                                {item.title}
                              </span>
                              {item.badge && (
                                <span className="rounded bg-indigo-500/20 px-1.5 py-0.5 text-[9px] font-mono text-indigo-300 border border-indigo-500/30">
                                  {item.badge}
                                </span>
                              )}
                            </div>
                            <p className="mt-0.5 text-[11px] text-slate-400 leading-snug">
                              {item.desc}
                            </p>
                          </div>
                        </Link>
                      );
                    })}
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          {/* Solutions Dropdown */}
          <div
            className="relative"
            onMouseEnter={() => setActiveDropdown('solutions')}
            onMouseLeave={() => setActiveDropdown(null)}
          >
            <button className="flex items-center gap-1 px-3 py-2 text-xs font-semibold text-slate-300 hover:text-white rounded-lg transition-colors hover:bg-slate-900/60">
              Solutions & Rails
              <ChevronDown className={`h-3.5 w-3.5 transition-transform duration-200 ${activeDropdown === 'solutions' ? 'rotate-180 text-indigo-400' : 'text-slate-500'}`} />
            </button>

            <AnimatePresence>
              {activeDropdown === 'solutions' && (
                <motion.div
                  initial={{ opacity: 0, y: 8, scale: 0.98 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: 8, scale: 0.98 }}
                  transition={{ duration: 0.15 }}
                  className="absolute left-0 top-full mt-2 w-[400px] rounded-2xl border border-slate-800 bg-slate-950/95 p-3 shadow-2xl backdrop-blur-2xl ring-1 ring-white/10"
                >
                  <div className="grid grid-cols-1 gap-1">
                    {solutionsItems.map((item) => {
                      const Icon = item.icon;
                      return (
                        <Link
                          key={item.title}
                          href={item.href}
                          className="flex items-start gap-3 rounded-xl p-2.5 transition-colors hover:bg-slate-900/80 group"
                        >
                          <div className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-emerald-500/10 text-emerald-400 group-hover:bg-emerald-500 group-hover:text-white transition-colors">
                            <Icon className="h-4 w-4" />
                          </div>
                          <div>
                            <span className="text-xs font-bold text-white group-hover:text-emerald-300 transition-colors">
                              {item.title}
                            </span>
                            <p className="mt-0.5 text-[11px] text-slate-400 leading-snug">
                              {item.desc}
                            </p>
                          </div>
                        </Link>
                      );
                    })}
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          {/* Developers Dropdown */}
          <div
            className="relative"
            onMouseEnter={() => setActiveDropdown('developers')}
            onMouseLeave={() => setActiveDropdown(null)}
          >
            <button className="flex items-center gap-1 px-3 py-2 text-xs font-semibold text-slate-300 hover:text-white rounded-lg transition-colors hover:bg-slate-900/60">
              Developers & Sec
              <ChevronDown className={`h-3.5 w-3.5 transition-transform duration-200 ${activeDropdown === 'developers' ? 'rotate-180 text-indigo-400' : 'text-slate-500'}`} />
            </button>

            <AnimatePresence>
              {activeDropdown === 'developers' && (
                <motion.div
                  initial={{ opacity: 0, y: 8, scale: 0.98 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: 8, scale: 0.98 }}
                  transition={{ duration: 0.15 }}
                  className="absolute right-0 top-full mt-2 w-[380px] rounded-2xl border border-slate-800 bg-slate-950/95 p-3 shadow-2xl backdrop-blur-2xl ring-1 ring-white/10"
                >
                  <div className="grid grid-cols-1 gap-1">
                    {developerItems.map((item) => {
                      const Icon = item.icon;
                      return (
                        <Link
                          key={item.title}
                          href={item.href}
                          className="flex items-start gap-3 rounded-xl p-2.5 transition-colors hover:bg-slate-900/80 group"
                        >
                          <div className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-blue-500/10 text-blue-400 group-hover:bg-blue-500 group-hover:text-white transition-colors">
                            <Icon className="h-4 w-4" />
                          </div>
                          <div>
                            <span className="text-xs font-bold text-white group-hover:text-blue-300 transition-colors">
                              {item.title}
                            </span>
                            <p className="mt-0.5 text-[11px] text-slate-400 leading-snug">
                              {item.desc}
                            </p>
                          </div>
                        </Link>
                      );
                    })}
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </nav>

        {/* Action Buttons */}
        <div className="hidden sm:flex items-center gap-3">
          {onOpenSimulator && (
            <button
              onClick={onOpenSimulator}
              className="group relative inline-flex items-center gap-2 rounded-xl bg-slate-900/90 px-3.5 py-2 text-xs font-semibold text-slate-200 border border-slate-800 shadow-sm transition-all hover:border-indigo-500/50 hover:bg-slate-800 hover:text-white hover:shadow-indigo-500/10"
            >
              <CreditCard className="h-3.5 w-3.5 text-indigo-400 group-hover:text-indigo-300" />
              <span>Try Payment Demo</span>
              <span className="rounded bg-indigo-500/20 px-1 py-0.2 text-[9px] font-mono text-indigo-300">Live</span>
            </button>
          )}

          <Link
            href="/login"
            className="px-3 py-2 text-xs font-semibold text-slate-300 hover:text-white transition-colors"
          >
            Sign In
          </Link>

          <Link href="/dashboard">
            <button className="relative inline-flex items-center gap-2 rounded-xl bg-gradient-to-r from-blue-600 via-indigo-600 to-indigo-700 px-4 py-2 text-xs font-bold text-white shadow-lg shadow-indigo-600/20 ring-1 ring-white/20 transition-all hover:scale-[1.02] hover:shadow-indigo-600/40 active:scale-[0.98]">
              <span>Operations Console</span>
              <ArrowRight className="h-3.5 w-3.5" />
            </button>
          </Link>
        </div>

        {/* Mobile Hamburger Button */}
        <div className="flex lg:hidden items-center gap-2">
          {onOpenSimulator && (
            <button
              onClick={onOpenSimulator}
              className="p-2 text-indigo-400 bg-indigo-500/10 rounded-lg border border-indigo-500/20"
              aria-label="Payment Demo"
            >
              <CreditCard className="h-4 w-4" />
            </button>
          )}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="p-2 text-slate-400 hover:text-white rounded-lg focus:outline-none bg-slate-900 border border-slate-800"
            aria-label="Toggle menu"
          >
            {mobileMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer */}
      <AnimatePresence>
        {mobileMenuOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="lg:hidden border-b border-slate-800 bg-slate-950 px-4 pt-3 pb-6 space-y-4 shadow-2xl"
          >
            <div className="flex items-center justify-between pb-3 border-b border-slate-900 text-xs text-slate-400 font-mono">
              <span className="flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-emerald-500 inline-block" />
                NPCI SWITCH ONLINE
              </span>
              <span className="text-indigo-400">27 SERVICES ACTIVE</span>
            </div>

            <div className="space-y-1">
              <div className="px-2 py-1 text-[10px] font-bold text-slate-500 uppercase tracking-wider font-mono">
                Platform Navigation
              </div>
              {[
                { label: 'Operations Dashboard', href: '/dashboard' },
                { label: 'Payment Gateway', href: '/payments' },
                { label: 'Fraud Intelligence', href: '/fraud' },
                { label: 'Behavioral Risk Scorer', href: '/risk' },
                { label: 'AI Investigator Copilot', href: '/ai-investigator' },
                { label: '3-Tier KYC Compliance', href: '/kyc' },
                { label: '3-Way Reconciliation', href: '/reconciliation' },
                { label: 'API Documentation', href: '/developers' },
              ].map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  onClick={() => setMobileMenuOpen(false)}
                  className="block rounded-lg px-3 py-2 text-xs font-semibold text-slate-300 hover:bg-slate-900 hover:text-white"
                >
                  {item.label}
                </Link>
              ))}
            </div>

            <div className="pt-2 border-t border-slate-900 flex flex-col gap-2">
              <Link href="/dashboard" onClick={() => setMobileMenuOpen(false)}>
                <button className="w-full py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 text-white text-xs font-bold shadow-md">
                  Launch Analyst Console
                </button>
              </Link>
              <Link href="/login" onClick={() => setMobileMenuOpen(false)}>
                <button className="w-full py-2 rounded-xl bg-slate-900 text-slate-300 border border-slate-800 text-xs font-semibold">
                  Sign In
                </button>
              </Link>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </header>
  );
}
