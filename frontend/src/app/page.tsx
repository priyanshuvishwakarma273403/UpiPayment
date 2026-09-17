'use client';

import React from 'react';
import Link from 'next/link';
import { PublicHeader } from '@/components/layout/PublicHeader';
import { PublicFooter } from '@/components/layout/PublicFooter';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import {
  ShieldCheck,
  Zap,
  Network,
  Lock,
  Cpu,
  ArrowRight,
  CheckCircle2,
  TrendingUp,
  Activity,
  AlertTriangle,
  Layers,
  FileCode,
  Sparkles,
  Server,
  BarChart3,
  RefreshCw,
  Search,
  Bot
} from 'lucide-react';

export default function LandingPage() {
  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900 font-sans selection:bg-blue-100 selection:text-blue-900">
      <PublicHeader />

      <main className="flex-1">
        {/* HERO SECTION */}
        <section className="relative overflow-hidden bg-slate-950 text-white pt-16 pb-20 lg:pt-24 lg:pb-28">
          {/* Subtle Grid Background Pattern */}
          <div className="absolute inset-0 bg-[linear-gradient(to_right,#1e293b_1px,transparent_1px),linear-gradient(to_bottom,#1e293b_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_70%_60%_at_50%_0%,#000_70%,transparent_100%)] opacity-30" />
          
          <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
              {/* Left Column: Headline, CTAs, Metrics (7 cols) */}
              <div className="lg:col-span-7 text-left space-y-6">
                <div className="inline-flex items-center gap-2 rounded-full bg-blue-500/10 px-4 py-1.5 text-xs font-semibold text-blue-400 ring-1 ring-inset ring-blue-500/20 backdrop-blur-sm">
                  <Sparkles className="h-3.5 w-3.5" />
                  <span>SentinelX 2.0 — Sub-10ms UPI Fraud Prevention Engine</span>
                </div>

                <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-white leading-[1.1]">
                  See risk before it <span className="bg-gradient-to-r from-blue-400 via-indigo-300 to-purple-400 bg-clip-text text-transparent">becomes loss.</span>
                </h1>

                <p className="text-base sm:text-lg text-slate-300 max-w-2xl font-normal leading-relaxed">
                  Real-time financial transaction intelligence, multi-hop graph network detection, and automated AML for high-throughput payment architectures across 27 microservices.
                </p>

                <div className="flex flex-col sm:flex-row items-center gap-4 pt-2">
                  <Link href="/dashboard" className="w-full sm:w-auto">
                    <Button variant="primary" size="lg" className="w-full sm:w-auto px-8 py-3.5 text-sm shadow-lg shadow-blue-600/25" rightIcon={<ArrowRight className="h-4 w-4" />}>
                      Launch Analyst Console
                    </Button>
                  </Link>
                  <Link href="/platform" className="w-full sm:w-auto">
                    <Button variant="outline" size="lg" className="w-full sm:w-auto px-8 py-3.5 text-sm border-slate-700 text-slate-200 hover:bg-slate-800 hover:text-white">
                      Explore Architecture
                    </Button>
                  </Link>
                </div>

                {/* Live Metrics Counter bar */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-6 border-t border-slate-800/80 text-left">
                  <div className="p-3 rounded-lg bg-slate-900/60 border border-slate-800/80">
                    <div className="text-xl font-bold font-mono text-white">&lt; 8.4 ms</div>
                    <div className="text-[11px] text-slate-400 font-medium mt-0.5">Median Risk Latency</div>
                  </div>
                  <div className="p-3 rounded-lg bg-slate-900/60 border border-slate-800/80">
                    <div className="text-xl font-bold font-mono text-emerald-400">99.98%</div>
                    <div className="text-[11px] text-slate-400 font-medium mt-0.5">Model Precision Rate</div>
                  </div>
                  <div className="p-3 rounded-lg bg-slate-900/60 border border-slate-800/80">
                    <div className="text-xl font-bold font-mono text-blue-400">27 Services</div>
                    <div className="text-[11px] text-slate-400 font-medium mt-0.5">Microservice Mesh</div>
                  </div>
                  <div className="p-3 rounded-lg bg-slate-900/60 border border-slate-800/80">
                    <div className="text-xl font-bold font-mono text-purple-400">100%</div>
                    <div className="text-[11px] text-slate-400 font-medium mt-0.5">Audit Ledger Coverage</div>
                  </div>
                </div>
              </div>

              {/* Right Column: Hero Visual Artwork (5 cols) */}
              <div className="lg:col-span-5 relative flex justify-center">
                <div className="relative rounded-2xl overflow-hidden border border-slate-800 shadow-2xl bg-slate-900/80 group">
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src="/hero-visual.jpg"
                    alt="SentinelX Real-time Financial Transaction Intelligence Visual"
                    className="w-full h-auto object-cover rounded-2xl transition-transform duration-700 group-hover:scale-105"
                  />
                  <div className="absolute inset-0 ring-1 ring-inset ring-white/10 rounded-2xl pointer-events-none" />
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* PLATFORM PILLARS SECTION */}
        <section className="py-20 bg-white border-b border-slate-200">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="text-center max-w-3xl mx-auto mb-16">
              <Badge variant="info" size="sm" className="mb-3 uppercase tracking-wider font-mono">
                Platform Architecture
              </Badge>
              <h2 className="text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">
                Built for Enterprise Payment Operations
              </h2>
              <p className="mt-3 text-base text-slate-600">
                Comprehensive intelligence modules integrated with core banking, risk scoring engines, and machine learning models.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              <Card className="border border-slate-200 hover:shadow-md transition-shadow">
                <CardHeader>
                  <div className="h-10 w-10 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center mb-4">
                    <Zap className="h-5 w-5" />
                  </div>
                  <CardTitle className="text-lg font-bold">Sub-10ms Risk Scoring Engine</CardTitle>
                  <CardDescription className="text-xs leading-relaxed">
                    Evaluates velocity, IP geofencing, device fingerprinting, and account behavior before payment authorization.
                  </CardDescription>
                </CardHeader>
                <CardContent className="pt-0 text-xs text-slate-600 space-y-2">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-emerald-600 shrink-0" />
                    <span>Calculates 0-1000 risk index synchronously</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-emerald-600 shrink-0" />
                    <span>Dynamic rule engine with automated thresholding</span>
                  </div>
                </CardContent>
              </Card>

              <Card className="border border-slate-200 hover:shadow-md transition-shadow">
                <CardHeader>
                  <div className="h-10 w-10 rounded-lg bg-indigo-100 text-indigo-600 flex items-center justify-center mb-4">
                    <Network className="h-5 w-5" />
                  </div>
                  <CardTitle className="text-lg font-bold">Multi-Hop Graph Intelligence</CardTitle>
                  <CardDescription className="text-xs leading-relaxed">
                    Detects mule accounts, smurfing rings, and synthetic identity networks across connected UPI handles.
                  </CardDescription>
                </CardHeader>
                <CardContent className="pt-0 text-xs text-slate-600 space-y-2">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-emerald-600 shrink-0" />
                    <span>Real-time entity relationship clustering</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-emerald-600 shrink-0" />
                    <span>Shared device ID & beneficiary mapping</span>
                  </div>
                </CardContent>
              </Card>

              <Card className="border border-slate-200 hover:shadow-md transition-shadow">
                <CardHeader>
                  <div className="h-10 w-10 rounded-lg bg-purple-100 text-purple-600 flex items-center justify-center mb-4">
                    <Bot className="h-5 w-5" />
                  </div>
                  <CardTitle className="text-lg font-bold">AI Investigator & MCP Tools</CardTitle>
                  <CardDescription className="text-xs leading-relaxed">
                    LLM-assisted case diagnostics with Model Context Protocol for automated policy RAG and evidence generation.
                  </CardDescription>
                </CardHeader>
                <CardContent className="pt-0 text-xs text-slate-600 space-y-2">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-emerald-600 shrink-0" />
                    <span>SHAP feature attribution explanations</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-emerald-600 shrink-0" />
                    <span>Continuous model learning loop with labels</span>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </section>

        {/* REAL-TIME INTELLIGENCE PIPELINE FLOW */}
        <section className="py-20 bg-slate-900 text-white">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="text-center max-w-3xl mx-auto mb-16">
              <Badge variant="info" size="sm" className="mb-3 uppercase tracking-wider font-mono bg-blue-500/20 text-blue-300 border-blue-500/30">
                End-to-End Execution Trace
              </Badge>
              <h2 className="text-3xl font-bold tracking-tight text-white sm:text-4xl">
                High-Throughput Fraud Pipeline
              </h2>
              <p className="mt-3 text-sm text-slate-400">
                Every transaction flows seamlessly through our microservice telemetry mesh in real-time.
              </p>
            </div>

            {/* Pipeline Visual Flow */}
            <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-8 gap-3 text-center text-xs font-mono">
              {[
                { name: '1. Gateway', sub: 'Spring Gateway', color: 'border-blue-500 text-blue-400' },
                { name: '2. Payment', sub: 'Payment API', color: 'border-indigo-500 text-indigo-400' },
                { name: '3. Kafka Stream', sub: 'Event Broker', color: 'border-purple-500 text-purple-400' },
                { name: '4. Risk Engine', sub: 'Scoring Rules', color: 'border-amber-500 text-amber-400' },
                { name: '5. Fraud Service', sub: 'Graph & Signals', color: 'border-rose-500 text-rose-400' },
                { name: '6. Python ML', sub: 'XGBoost Infer', color: 'border-emerald-500 text-emerald-400' },
                { name: '7. AI Copilot', sub: 'MCP Diagnostic', color: 'border-cyan-500 text-cyan-400' },
                { name: '8. Case Ledger', sub: 'Resolution', color: 'border-blue-400 text-blue-300' },
              ].map((step, idx) => (
                <div key={step.name} className={`p-3 rounded-lg bg-slate-800/80 border ${step.color} shadow-sm space-y-1`}>
                  <div className="font-bold text-[11px]">{step.name}</div>
                  <div className="text-[10px] text-slate-400">{step.sub}</div>
                </div>
              ))}
            </div>

            <div className="mt-12 p-6 rounded-xl bg-slate-800/50 border border-slate-800 flex flex-col md:flex-row items-center justify-between gap-6 text-slate-300 text-xs">
              <div className="flex items-center gap-3">
                <Activity className="h-6 w-6 text-emerald-400 shrink-0" />
                <div>
                  <div className="font-semibold text-white">Continuous Observability & Audit Trail</div>
                  <div>Integrated with OpenTelemetry, Prometheus, Jaeger tracing, and Loki log aggregation.</div>
                </div>
              </div>
              <Link href="/developers">
                <Button variant="outline" size="sm" className="border-slate-700 text-slate-300 hover:bg-slate-800 hover:text-white shrink-0">
                  Read API Specification
                </Button>
              </Link>
            </div>
          </div>
        </section>

        {/* CONCEPTUAL FRAUD NETWORK GRAPHIC */}
        <section className="py-20 bg-slate-50 border-b border-slate-200">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
              <div>
                <Badge variant="warning" size="sm" className="mb-3 uppercase tracking-wider font-mono">
                  Network Graph Intelligence
                </Badge>
                <h2 className="text-3xl font-bold tracking-tight text-slate-900">
                  Uncover Mule Clusters & Fraud Rings
                </h2>
                <p className="mt-4 text-sm text-slate-600 leading-relaxed">
                  Bad actors operate in coordinated rings using stolen device IDs, shared bank accounts, and rapid fan-out money transfers. SentinelX visualizes multi-hop relationship graphs to freeze coordinated attacks before payout.
                </p>
                <ul className="mt-6 space-y-3 text-xs text-slate-700 font-medium">
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-blue-600" />
                    <span>Real-time graph community clustering algorithms</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-blue-600" />
                    <span>Cross-merchant shared device fingerprinting</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-blue-600" />
                    <span>Rapid velocity smurfing & velocity spike alerts</span>
                  </li>
                </ul>
                <div className="mt-8">
                  <Link href="/network">
                    <Button variant="primary" size="md" rightIcon={<ArrowRight className="h-4 w-4" />}>
                      View Graph Network Visualizer
                    </Button>
                  </Link>
                </div>
              </div>

              {/* Graphic Mockup Box */}
              <div className="rounded-2xl border border-slate-300 bg-white p-6 shadow-xl space-y-4">
                <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                  <span className="text-xs font-bold text-slate-800 flex items-center gap-2 font-mono">
                    <Network className="h-4 w-4 text-blue-600" /> Fraud Cluster #MULE-8832
                  </span>
                  <Badge variant="critical" size="sm">CRITICAL RISK</Badge>
                </div>

                <div className="h-48 rounded-lg bg-slate-900 p-4 relative overflow-hidden flex items-center justify-center">
                  <div className="absolute inset-0 bg-[radial-gradient(#334155_1px,transparent_1px)] [background-size:16px_16px] opacity-40" />
                  
                  {/* Conceptual Graph Nodes */}
                  <div className="relative z-10 w-full h-full flex items-center justify-around">
                    <div className="p-3 rounded-full bg-rose-500/20 border-2 border-rose-500 text-rose-300 text-center font-mono text-[10px] shadow-lg animate-pulse">
                      <div>UPI-SENDER</div>
                      <div className="text-[9px] text-rose-200">Risk: 940</div>
                    </div>
                    <div className="h-0.5 w-12 bg-rose-500/80" />
                    <div className="p-2.5 rounded-full bg-amber-500/20 border-2 border-amber-500 text-amber-300 text-center font-mono text-[10px]">
                      <div>MULE-HUB</div>
                      <div className="text-[9px] text-amber-200">3 Hops</div>
                    </div>
                    <div className="h-0.5 w-12 bg-blue-500/80" />
                    <div className="p-3 rounded-full bg-blue-500/20 border-2 border-blue-500 text-blue-300 text-center font-mono text-[10px]">
                      <div>BENEFICIARY</div>
                      <div className="text-[9px] text-blue-200">Merchant</div>
                    </div>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3 text-xs text-slate-600">
                  <div className="p-2.5 rounded bg-slate-50 border border-slate-200">
                    <span className="text-[10px] text-slate-500 block">Identified Ring Size</span>
                    <span className="font-bold text-slate-800 font-mono">14 Connected Nodes</span>
                  </div>
                  <div className="p-2.5 rounded bg-slate-50 border border-slate-200">
                    <span className="text-[10px] text-slate-500 block">Total Exposure</span>
                    <span className="font-bold text-rose-700 font-mono">₹ 14,80,000</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* SECURITY & COMPLIANCE SECTION */}
        <section className="py-20 bg-white">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="text-center max-w-3xl mx-auto mb-16">
              <Badge variant="success" size="sm" className="mb-3 uppercase tracking-wider font-mono">
                Bank-Grade Security
              </Badge>
              <h2 className="text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">
                Enterprise Compliance & Zero-Trust
              </h2>
              <p className="mt-3 text-sm text-slate-600">
                Engineered to meet the stringent security protocols required by central banking regulators and payment networks.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-6 text-left">
              {[
                { title: 'Zero-Trust RBAC', desc: 'Granular permissions for ADMIN, RISK_ANALYST, FRAUD_ANALYST, and AUDITOR roles.' },
                { title: 'Field Encryption', desc: 'PCI-DSS compliant AES-256 GCM encryption for PII and account numbers.' },
                { title: 'Immutable Audit Trail', desc: 'Every analyst decision, status update, and rule change logged with cryptographic hashes.' },
                { title: 'OFAC & PEP Screening', desc: 'Automated AML screening against global sanctions watchlists in real-time.' },
              ].map((item) => (
                <div key={item.title} className="p-5 rounded-xl border border-slate-200 bg-slate-50/50 hover:bg-white transition-colors">
                  <Lock className="h-5 w-5 text-blue-600 mb-3" />
                  <h3 className="text-sm font-bold text-slate-900">{item.title}</h3>
                  <p className="mt-2 text-xs text-slate-600 leading-relaxed">{item.desc}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* CALL TO ACTION */}
        <section className="py-16 bg-gradient-to-r from-blue-600 to-indigo-700 text-white text-center">
          <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8">
            <h2 className="text-3xl font-extrabold tracking-tight sm:text-4xl">
              Ready to secure your payment infrastructure?
            </h2>
            <p className="mt-4 text-base text-blue-100">
              Access the SentinelX Fraud Operations Console and monitor live transaction telemetry.
            </p>
            <div className="mt-8 flex items-center justify-center gap-4">
              <Link href="/dashboard">
                <Button variant="secondary" size="lg" className="bg-white text-blue-700 hover:bg-blue-50 font-bold px-8 shadow-md" rightIcon={<ArrowRight className="h-4 w-4" />}>
                  Enter Operations Console
                </Button>
              </Link>
              <Link href="/login">
                <Button variant="outline" size="lg" className="border-white text-white hover:bg-white/10 px-8">
                  Sign In
                </Button>
              </Link>
            </div>
          </div>
        </section>
      </main>

      <PublicFooter />
    </div>
  );
}
