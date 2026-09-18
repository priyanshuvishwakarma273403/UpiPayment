'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { PublicHeader } from '@/components/layout/PublicHeader';
import { PublicFooter } from '@/components/layout/PublicFooter';
import { Card3D } from '@/components/ui/Card3D';
import { StickyScrollReveal } from '@/components/ui/StickyScrollReveal';
import { LivePaymentSimulator } from '@/components/dashboard/LivePaymentSimulator';
import { NetworkGraphVisualizer } from '@/components/network/NetworkGraphVisualizer';
import {
  ShieldCheck,
  Zap,
  Network,
  Lock,
  Cpu,
  ArrowRight,
  CheckCircle2,
  Activity,
  Sparkles,
  CreditCard,
  Bot,
  Database,
  Layers,
  Terminal,
  FileSpreadsheet
} from 'lucide-react';

export default function LandingPage() {
  const [simulatorOpen, setSimulatorOpen] = useState(false);

  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100 font-sans selection:bg-indigo-500/30 selection:text-indigo-200">
      {/* Interactive Public Header with Simulator Trigger */}
      <PublicHeader onOpenSimulator={() => setSimulatorOpen(true)} />

      <main className="flex-1">
        {/* ================= HERO SECTION ================= */}
        <section className="relative overflow-hidden pt-12 pb-20 lg:pt-20 lg:pb-28 border-b border-slate-900">
          {/* Subtle Grid Background & Radial Orbs */}
          <div className="absolute inset-0 bg-[linear-gradient(to_right,#1e293b_1px,transparent_1px),linear-gradient(to_bottom,#1e293b_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_70%_60%_at_50%_0%,#000_70%,transparent_100%)] opacity-20 pointer-events-none" />
          <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[350px] bg-gradient-to-tr from-indigo-600/20 via-blue-600/15 to-purple-600/20 rounded-full blur-[120px] pointer-events-none" />

          <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-8 items-center">
              {/* Left Column: Headline, CTAs, Metrics (7 cols) */}
              <div className="lg:col-span-7 text-left space-y-6">
                <div className="inline-flex items-center gap-2 rounded-full bg-indigo-500/10 px-4 py-1.5 text-xs font-semibold text-indigo-300 ring-1 ring-inset ring-indigo-500/25 backdrop-blur-md">
                  <Sparkles className="h-3.5 w-3.5 text-indigo-400" />
                  <span>SentinelX 2.0 • Sub-10ms UPI 2.0 Fraud Switch</span>
                </div>

                <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-white leading-[1.1]">
                  See risk before it <br className="hidden sm:inline" />
                  <span className="bg-gradient-to-r from-blue-400 via-indigo-300 to-purple-400 bg-clip-text text-transparent">
                    becomes loss.
                  </span>
                </h1>

                <p className="text-base sm:text-lg text-slate-300 max-w-2xl font-normal leading-relaxed">
                  Real-time transaction intelligence, multi-hop graph network detection, and automated AML for high-throughput payment architectures across 27 microservices.
                </p>

                {/* Primary Action Buttons */}
                <div className="flex flex-col sm:flex-row items-center gap-3 pt-2">
                  <button
                    onClick={() => setSimulatorOpen(true)}
                    className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl bg-gradient-to-r from-blue-600 via-indigo-600 to-indigo-700 text-white font-bold text-sm shadow-xl shadow-indigo-600/30 ring-1 ring-white/20 transition-all hover:scale-[1.02] active:scale-[0.98]"
                  >
                    <CreditCard className="h-4 w-4" />
                    <span>Try Payment Simulator</span>
                  </button>

                  <Link href="/dashboard" className="w-full sm:w-auto">
                    <button className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl bg-slate-900/90 border border-slate-700 text-slate-200 font-semibold text-sm hover:bg-slate-800 hover:text-white transition-colors">
                      <span>Launch Analyst Console</span>
                      <ArrowRight className="h-4 w-4" />
                    </button>
                  </Link>

                  <Link href="/platform" className="w-full sm:w-auto">
                    <button className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-5 py-3.5 rounded-xl text-slate-400 hover:text-slate-200 text-sm font-medium transition-colors">
                      Explore Architecture
                    </button>
                  </Link>
                </div>

                {/* Live Metrics Counter Bar */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-6 border-t border-slate-900">
                  <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800/80">
                    <div className="text-xl font-bold font-mono text-white">&lt; 8.4 ms</div>
                    <div className="text-[11px] text-slate-400 font-medium mt-0.5">Median Risk Latency</div>
                  </div>
                  <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800/80">
                    <div className="text-xl font-bold font-mono text-emerald-400">99.98%</div>
                    <div className="text-[11px] text-slate-400 font-medium mt-0.5">Model Precision Rate</div>
                  </div>
                  <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800/80">
                    <div className="text-xl font-bold font-mono text-indigo-400">27 Services</div>
                    <div className="text-[11px] text-slate-400 font-medium mt-0.5">Microservice Mesh</div>
                  </div>
                  <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800/80">
                    <div className="text-xl font-bold font-mono text-purple-400">100%</div>
                    <div className="text-[11px] text-slate-400 font-medium mt-0.5">Audit Ledger Coverage</div>
                  </div>
                </div>
              </div>

              {/* Right Column: 3D Interactive Payment Card Showcase (5 cols) */}
              <div className="lg:col-span-5 relative flex flex-col items-center justify-center">
                {/* Ambient Halo behind Card */}
                <div className="absolute inset-0 bg-indigo-500/20 rounded-full blur-3xl pointer-events-none" />

                {/* Interactive 3D Card */}
                <Card3D
                  cardHolder="VIKRAM ADITYA"
                  upiId="vikram@okaxis"
                  cardNumber="4289 9012 3345 8842"
                  expiry="10/29"
                  showSkinSelector={true}
                />

                {/* Floating Ambient Risk Badges */}
                <div className="mt-4 flex flex-wrap items-center justify-center gap-2 text-[10px] font-mono">
                  <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-slate-900/80 border border-slate-800 text-slate-300 backdrop-blur-md">
                    <ShieldCheck className="h-3.5 w-3.5 text-emerald-400" />
                    <span>Risk Index: 12/1000 (Safe)</span>
                  </div>
                  <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-slate-900/80 border border-slate-800 text-slate-300 backdrop-blur-md">
                    <Lock className="h-3 w-3 text-indigo-400" />
                    <span>RSA-2048 Signed</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* ================= PLATFORM PILLARS (BENTO GRID) ================= */}
        <section className="py-20 bg-slate-950 border-b border-slate-900">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="text-center max-w-3xl mx-auto mb-16">
              <span className="inline-flex items-center gap-1.5 rounded-full bg-blue-500/10 px-3 py-1 text-xs font-mono font-semibold text-blue-400 ring-1 ring-inset ring-blue-500/25 mb-3">
                <Layers className="h-3.5 w-3.5" />
                ENTERPRISE CAPABILITIES
              </span>
              <h2 className="text-3xl font-extrabold tracking-tight text-white sm:text-4xl">
                Engineered for High-Throughput Fintech Operations
              </h2>
              <p className="mt-3 text-sm sm:text-base text-slate-400">
                Synchronous perimeter filtering, continuous machine learning inference, and cryptographic mesh synchronization.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6 shadow-xl hover:border-indigo-500/40 transition-all duration-300 group">
                <div className="h-11 w-11 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center mb-5 group-hover:bg-blue-500 group-hover:text-white transition-colors">
                  <Zap className="h-5 w-5" />
                </div>
                <h3 className="text-lg font-bold text-white mb-2">Sub-10ms Risk Scoring Engine</h3>
                <p className="text-xs text-slate-400 leading-relaxed mb-4">
                  Evaluates velocity, IP geofencing, device fingerprinting, and account behavior before payment authorization.
                </p>
                <div className="pt-3 border-t border-slate-800/80 space-y-2 text-xs text-slate-300">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                    <span>Synchronous 0-1000 risk index</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                    <span>Dynamic rule thresholding & step-up</span>
                  </div>
                </div>
              </div>

              <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6 shadow-xl hover:border-indigo-500/40 transition-all duration-300 group">
                <div className="h-11 w-11 rounded-xl bg-indigo-500/10 text-indigo-400 flex items-center justify-center mb-5 group-hover:bg-indigo-500 group-hover:text-white transition-colors">
                  <Network className="h-5 w-5" />
                </div>
                <h3 className="text-lg font-bold text-white mb-2">Multi-Hop Graph Intelligence</h3>
                <p className="text-xs text-slate-400 leading-relaxed mb-4">
                  Detects mule accounts, smurfing rings, and synthetic identity networks across connected UPI handles in real-time.
                </p>
                <div className="pt-3 border-t border-slate-800/80 space-y-2 text-xs text-slate-300">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                    <span>Real-time entity relationship clustering</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                    <span>Shared device ID & beneficiary mapping</span>
                  </div>
                </div>
              </div>

              <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6 shadow-xl hover:border-indigo-500/40 transition-all duration-300 group">
                <div className="h-11 w-11 rounded-xl bg-purple-500/10 text-purple-400 flex items-center justify-center mb-5 group-hover:bg-purple-500 group-hover:text-white transition-colors">
                  <Bot className="h-5 w-5" />
                </div>
                <h3 className="text-lg font-bold text-white mb-2">AI Copilot & XAI SHAP</h3>
                <p className="text-xs text-slate-400 leading-relaxed mb-4">
                  LLM-assisted case diagnostics with Model Context Protocol for automated policy RAG and evidence generation.
                </p>
                <div className="pt-3 border-t border-slate-800/80 space-y-2 text-xs text-slate-300">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                    <span>SHAP feature attribution explanations</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                    <span>Continuous model learning loop with labels</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* ================= STICKY SCROLL SECTION ================= */}
        <section className="py-20 bg-slate-950 border-b border-slate-900">
          <StickyScrollReveal />
        </section>

        {/* ================= INTERACTIVE NETWORK GRAPH VISUALIZER ================= */}
        <section className="py-20 bg-slate-950 border-b border-slate-900">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
              <div className="lg:col-span-5 space-y-5">
                <span className="inline-flex items-center gap-1.5 rounded-full bg-rose-500/10 px-3 py-1 text-xs font-mono font-semibold text-rose-400 ring-1 ring-inset ring-rose-500/25">
                  <Network className="h-3.5 w-3.5" />
                  MULE RING DETECTION
                </span>
                <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-white">
                  Uncover Mule Clusters & Syndicate Rings
                </h2>
                <p className="text-sm text-slate-400 leading-relaxed">
                  Bad actors operate in coordinated rings using stolen device IDs, shared bank accounts, and rapid fan-out money transfers. SentinelX visualizes multi-hop relationship graphs to freeze coordinated attacks before payout.
                </p>

                <ul className="space-y-2.5 text-xs text-slate-300 font-mono">
                  <li className="flex items-center gap-2.5">
                    <CheckCircle2 className="h-4 w-4 text-indigo-400 shrink-0" />
                    <span>Real-time graph community clustering algorithms</span>
                  </li>
                  <li className="flex items-center gap-2.5">
                    <CheckCircle2 className="h-4 w-4 text-indigo-400 shrink-0" />
                    <span>Cross-merchant shared device fingerprinting</span>
                  </li>
                  <li className="flex items-center gap-2.5">
                    <CheckCircle2 className="h-4 w-4 text-indigo-400 shrink-0" />
                    <span>Rapid velocity smurfing & velocity spike alerts</span>
                  </li>
                </ul>

                <div className="pt-2">
                  <Link href="/network">
                    <button className="inline-flex items-center gap-2 px-5 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs shadow-lg shadow-indigo-600/25 transition-all">
                      <span>Explore Live Graph Console</span>
                      <ArrowRight className="h-4 w-4" />
                    </button>
                  </Link>
                </div>
              </div>

              {/* Interactive Network Graph Component */}
              <div className="lg:col-span-7">
                <NetworkGraphVisualizer />
              </div>
            </div>
          </div>
        </section>

        {/* ================= BANK-GRADE COMPLIANCE & SECURITY ================= */}
        <section className="py-20 bg-slate-950 border-b border-slate-900">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="text-center max-w-3xl mx-auto mb-16">
              <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-500/10 px-3 py-1 text-xs font-mono font-semibold text-emerald-400 ring-1 ring-inset ring-emerald-500/25 mb-3">
                <Lock className="h-3.5 w-3.5" />
                REGULATORY BLUEPRINT
              </span>
              <h2 className="text-3xl font-extrabold tracking-tight text-white sm:text-4xl">
                Bank-Grade Security & Zero-Trust Compliance
              </h2>
              <p className="mt-3 text-sm sm:text-base text-slate-400">
                Engineered to meet the stringent security protocols required by central banking regulators and payment networks.
              </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {[
                {
                  title: 'Zero-Trust RBAC',
                  desc: 'Granular permissions for ADMIN, RISK_ANALYST, FRAUD_ANALYST, and AUDITOR roles with JWT claim isolation.',
                  badge: 'NIST SP 800-207',
                },
                {
                  title: 'AES-256-GCM AEAD',
                  desc: 'PCI-DSS compliant field-level encryption for Aadhaar, PAN, and bank accounts with tamper-evident auth tags.',
                  badge: 'PCI-DSS Lv. 1',
                },
                {
                  title: 'RSA-2048 Non-Repudiation',
                  desc: 'Asymmetric cryptographic keypairs generated at client edge for verifiable offline payment verification.',
                  badge: 'WebCrypto API',
                },
                {
                  title: 'PMLA & OFAC Watchdog',
                  desc: 'Continuous real-time Levenshtein fuzzy matching across global PEP and sanctions databases.',
                  badge: 'PMLA 2002',
                },
              ].map((item) => (
                <div
                  key={item.title}
                  className="p-6 rounded-2xl border border-slate-800 bg-slate-900/40 hover:bg-slate-900/70 transition-all space-y-3"
                >
                  <div className="flex items-center justify-between">
                    <Lock className="h-5 w-5 text-indigo-400" />
                    <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-slate-800 border border-slate-700 text-slate-400">
                      {item.badge}
                    </span>
                  </div>
                  <h3 className="text-sm font-bold text-white">{item.title}</h3>
                  <p className="text-xs text-slate-400 leading-relaxed">{item.desc}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* ================= CALL TO ACTION ================= */}
        <section className="py-20 bg-gradient-to-b from-slate-950 to-indigo-950/40 text-center relative overflow-hidden">
          <div className="absolute inset-0 bg-[radial-gradient(#6366f1_1px,transparent_1px)] [background-size:24px_24px] opacity-15 pointer-events-none" />

          <div className="relative mx-auto max-w-4xl px-4 sm:px-6 lg:px-8 space-y-6">
            <h2 className="text-3xl sm:text-4xl lg:text-5xl font-extrabold tracking-tight text-white">
              Ready to secure your payment infrastructure?
            </h2>
            <p className="text-sm sm:text-base text-slate-300 max-w-2xl mx-auto">
              Access the SentinelX Fraud Operations Console, monitor live transaction telemetry across all 27 microservices, and inspect continuous ML models.
            </p>

            <div className="pt-4 flex flex-col sm:flex-row items-center justify-center gap-4">
              <Link href="/dashboard">
                <button className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-8 py-4 rounded-xl bg-gradient-to-r from-blue-600 via-indigo-600 to-indigo-700 text-white font-bold text-sm shadow-xl shadow-indigo-600/30 transition-all hover:scale-[1.02] active:scale-[0.98]">
                  <span>Enter Operations Console</span>
                  <ArrowRight className="h-4 w-4" />
                </button>
              </Link>
              <button
                onClick={() => setSimulatorOpen(true)}
                className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-8 py-4 rounded-xl bg-slate-900 border border-slate-700 text-white font-semibold text-sm hover:bg-slate-800 transition-colors"
              >
                <CreditCard className="h-4 w-4 text-indigo-400" />
                <span>Launch Live Simulator</span>
              </button>
            </div>
          </div>
        </section>
      </main>

      {/* Simulator Modal */}
      {simulatorOpen && (
        <LivePaymentSimulator isModal={true} onClose={() => setSimulatorOpen(false)} />
      )}

      <PublicFooter />
    </div>
  );
}
