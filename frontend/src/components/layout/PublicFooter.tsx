'use client';

import React from 'react';
import Link from 'next/link';
import { ShieldCheck, ExternalLink } from 'lucide-react';

export function PublicFooter() {
  return (
    <footer className="bg-[#080c14] text-slate-300 font-sans text-xs border-t border-slate-800/80">
      <div className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8 space-y-16">
        
        {/* ========================================================================= */}
        {/* ROW 1: BRAND LOGO + 4 PRIMARY COLUMNS                                      */}
        {/* ========================================================================= */}
        <div className="grid grid-cols-1 md:grid-cols-5 gap-8 lg:gap-12">
          
          {/* Brand Logo Header Column */}
          <div className="space-y-4">
            <Link href="/" className="flex items-center gap-2.5 group">
              <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600 text-white shadow-lg transition-transform group-hover:scale-105">
                <ShieldCheck className="h-5.5 w-5.5" />
              </div>
              <span className="text-xl font-bold tracking-tight text-white font-mono">
                SENTINEL<span className="text-blue-500">X</span>
              </span>
            </Link>
            <p className="text-xs text-slate-400 leading-relaxed max-w-xs">
              Enterprise financial fraud intelligence & payment operations platform. Real-time sub-10ms risk scoring and multi-hop graph networks.
            </p>
          </div>

          {/* Column 1: ONBOARDING */}
          <div>
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-slate-400 mb-4 font-mono">
              Onboarding
            </h3>
            <ul className="space-y-3 font-medium text-xs">
              <li><Link href="/platform" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Global KYC</Link></li>
              <li><Link href="/platform" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Global KYB</Link></li>
              <li><Link href="/customers" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Identity Verification</Link></li>
              <li><Link href="/kyc" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Document Verification</Link></li>
              <li><Link href="/payments" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Bank Verification</Link></li>
              <li><Link href="/risk" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Credit Underwriting</Link></li>
            </ul>
          </div>

          {/* Column 2: FRAUD PREVENTION */}
          <div>
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-slate-400 mb-4 font-mono">
              Fraud Prevention
            </h3>
            <ul className="space-y-3 font-medium text-xs">
              <li><Link href="/fraud" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Agentic Fraud Ops</Link></li>
              <li><Link href="/network" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Device & Behavior</Link></li>
              <li><Link href="/transactions" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Payment Fraud</Link></li>
              <li><Link href="/payments" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Bank Transactions</Link></li>
              <li><Link href="/merchants" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Card Issuing Fraud</Link></li>
              <li><Link href="/merchants" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Merchant Monitoring</Link></li>
              <li><Link href="/knowledge" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Policy Abuse</Link></li>
              <li><Link href="/disputes" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Refund Fraud</Link></li>
            </ul>
          </div>

          {/* Column 3: CYBER SECURITY */}
          <div>
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-slate-400 mb-4 font-mono">
              Cyber Security
            </h3>
            <ul className="space-y-3 font-medium text-xs">
              <li><Link href="/security" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Account Takeovers</Link></li>
              <li><Link href="/simulations" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Bot Detection</Link></li>
              <li><Link href="/audit" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Job Applicant Fraud</Link></li>
            </ul>
          </div>

          {/* Column 4: AML COMPLIANCE */}
          <div>
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-slate-400 mb-4 font-mono">
              AML Compliance
            </h3>
            <ul className="space-y-3 font-medium text-xs">
              <li><Link href="/platform/aml" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Agentic AML Ops</Link></li>
              <li><Link href="/transactions" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Transaction Monitoring</Link></li>
              <li><Link href="/risk" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Customer Risk Rating</Link></li>
              <li><Link href="/platform/aml" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Sanctions Screening</Link></li>
              <li><Link href="/cases" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Case Management</Link></li>
              <li><Link href="/settlement" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Sponsor Banking</Link></li>
            </ul>
          </div>

        </div>

        {/* ========================================================================= */}
        {/* ROW 2: BRAND GRAPHIC ARTWORK + 4 SECONDARY COLUMNS                        */}
        {/* ========================================================================= */}
        <div className="grid grid-cols-1 md:grid-cols-5 gap-8 lg:gap-12 pt-8 border-t border-slate-800/60">
          
          {/* Left Side: Isometric Graphic Artwork Card */}
          <div className="space-y-4">
            <div className="relative rounded-xl overflow-hidden border border-slate-800 bg-slate-900/60 p-4 group">
              <div className="h-36 rounded-lg bg-gradient-to-br from-blue-900/40 via-indigo-950/60 to-slate-950 flex flex-col items-center justify-center text-center p-3 relative overflow-hidden border border-blue-500/20">
                <div className="absolute inset-0 bg-[radial-gradient(#3b82f6_1px,transparent_1px)] [background-size:12px_12px] opacity-20" />
                <div className="h-12 w-12 rounded-xl bg-blue-600/30 border border-blue-400/30 flex items-center justify-center text-blue-400 mb-2 shadow-lg group-hover:scale-110 transition-transform">
                  <ShieldCheck className="h-7 w-7" />
                </div>
                <span className="text-xs font-bold text-white font-mono">SentinelX Core</span>
                <span className="text-[10px] text-blue-300 font-mono mt-0.5">Sub-10ms Fraud Mesh</span>
              </div>
            </div>
          </div>

          {/* Column 5: PLATFORM */}
          <div>
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-slate-400 mb-4 font-mono">
              Platform
            </h3>
            <ul className="space-y-3 font-medium text-xs">
              <li><Link href="/network" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Device & Behavior</Link></li>
              <li><Link href="/platform/risk" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Rules Engine</Link></li>
              <li><Link href="/fraud" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Machine Learning</Link></li>
              <li><Link href="/analytics" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Consortium Data</Link></li>
              <li><Link href="/mcp" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Workflow Automation</Link></li>
              <li><Link href="/dashboard" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Fraud Dashboard</Link></li>
              <li><Link href="/network" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Connections Graph</Link></li>
              <li><Link href="/cases" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Case Management</Link></li>
            </ul>
          </div>

          {/* Column 6: RESOURCES */}
          <div>
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-slate-400 mb-4 font-mono">
              Resources
            </h3>
            <ul className="space-y-3 font-medium text-xs">
              <li><Link href="/customers" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Customers</Link></li>
              <li><Link href="/knowledge" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Blog</Link></li>
              <li><Link href="/contact" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Events</Link></li>
              <li><Link href="/knowledge" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Whitepapers</Link></li>
              <li><Link href="/developers" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Engineering Blog</Link></li>
              <li><Link href="/mcp" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">SentinelX Tools</Link></li>
              <li><Link href="/developers" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Documentation</Link></li>
              <li>
                <a href="/docs/backend-contract-map.md" target="_blank" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold inline-flex items-center gap-1">
                  API Status <ExternalLink className="h-3 w-3" />
                </a>
              </li>
            </ul>
          </div>

          {/* Column 7: MEDIA & TOOLS */}
          <div>
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-slate-400 mb-4 font-mono">
              Media & Tools
            </h3>
            <ul className="space-y-3 font-medium text-xs">
              <li><Link href="/fraud-intelligence" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Fraud Forward Podcast</Link></li>
              <li><Link href="/knowledge" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">The Saturday Fraud Strategist</Link></li>
              <li><Link href="/ai-investigator" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Fraudology Podcast</Link></li>
            </ul>
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-slate-400 mt-6 mb-4 font-mono">
              SentinelX Tools
            </h3>
            <ul className="space-y-3 font-medium text-xs">
              <li><Link href="/knowledge" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Fraud & AML Glossary</Link></li>
              <li><Link href="/knowledge" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Fraud & AML Regulations Directory</Link></li>
            </ul>
          </div>

          {/* Column 8: COMPANY */}
          <div>
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-slate-400 mb-4 font-mono">
              Company
            </h3>
            <ul className="space-y-3 font-medium text-xs">
              <li><Link href="/platform" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">About</Link></li>
              <li><Link href="/contact" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Careers</Link></li>
              <li><Link href="/contact" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Events</Link></li>
              <li><Link href="/security" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Security Architecture</Link></li>
              <li><Link href="/contact" className="text-slate-100 hover:text-blue-400 transition-colors font-semibold">Contact Enterprise Sales</Link></li>
            </ul>
          </div>

        </div>

      </div>

      {/* ========================================================================= */}
      {/* BOTTOM BAR                                                                */}
      {/* ========================================================================= */}
      <div className="bg-[#05080e] border-t border-slate-900 py-6 px-4 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-7xl flex flex-col md:flex-row items-center justify-between text-xs text-slate-400 gap-4">
          
          {/* Left: Copyright */}
          <div>
            © {new Date().getFullYear()} SentinelX AI Corp. All rights reserved.
          </div>

          {/* Center: Legal & Privacy Links */}
          <div className="flex flex-wrap items-center justify-center gap-6 text-slate-300 font-medium">
            <Link href="/privacy" className="hover:text-white transition-colors">
              Privacy Policy
            </Link>
            <Link href="/security" className="hover:text-white transition-colors flex items-center gap-1.5">
              <span>Your Privacy Choices</span>
              <span className="bg-blue-600 text-white text-[9px] font-mono font-bold px-1 py-0.25 rounded">✓×</span>
            </Link>
            <Link href="/security" className="hover:text-white transition-colors">
              Security
            </Link>
            <Link href="/terms" className="hover:text-white transition-colors">
              Terms of Service
            </Link>
          </div>

          {/* Right: Social Media Buttons */}
          <div className="flex items-center gap-2">
            <a
              href="https://linkedin.com"
              target="_blank"
              rel="noreferrer"
              aria-label="LinkedIn"
              className="flex h-8 w-8 items-center justify-center rounded-full bg-slate-900 border border-slate-800 text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
            >
              <svg className="h-3.5 w-3.5 fill-current" viewBox="0 0 24 24">
                <path d="M19 3a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h14m-.5 15.5v-5.3a3.26 3.26 0 0 0-3.26-3.26c-.85 0-1.84.52-2.28 1.3v-1.11h-2.79v8.37h2.79v-4.93c0-.77.62-1.4 1.39-1.4a1.4 1.4 0 0 1 1.4 1.4v4.93h2.75M6.46 10.9v8.37H9.25V10.9H6.46M7.86 6.74a1.62 1.62 0 1 0 0 3.24 1.62 1.62 0 0 0 0-3.24Z" />
              </svg>
            </a>
            <a
              href="https://x.com"
              target="_blank"
              rel="noreferrer"
              aria-label="X / Twitter"
              className="flex h-8 w-8 items-center justify-center rounded-full bg-slate-900 border border-slate-800 text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
            >
              <svg className="h-3.5 w-3.5 fill-current" viewBox="0 0 24 24">
                <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z" />
              </svg>
            </a>
            <a
              href="https://github.com"
              target="_blank"
              rel="noreferrer"
              aria-label="GitHub"
              className="flex h-8 w-8 items-center justify-center rounded-full bg-slate-900 border border-slate-800 text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
            >
              <svg className="h-3.5 w-3.5 fill-current" viewBox="0 0 24 24">
                <path d="M12 2A10 10 0 0 0 2 12c0 4.42 2.87 8.17 6.84 9.5.5.08.66-.23.66-.5v-1.69c-2.77.6-3.36-1.34-3.36-1.34-.46-1.16-1.11-1.47-1.11-1.47-.91-.62.07-.6.07-.6 1 .07 1.53 1.03 1.53 1.03.87 1.52 2.34 1.07 2.91.83.1-.65.35-1.09.63-1.34-2.22-.25-4.55-1.11-4.55-4.92 0-1.11.38-2 1.03-2.71-.1-.25-.45-1.29.1-2.64 0 0 .84-.27 2.75 1.02.79-.22 1.65-.33 2.5-.33.85 0 1.71.11 2.5.33 1.91-1.29 2.75-1.02 2.75-1.02.55 1.35.2 2.39.1 2.64.65.71 1.03 1.6 1.03 2.71 0 3.82-2.34 4.66-4.57 4.91.36.31.69.92.69 1.85V21c0 .27.16.59.67.5C19.14 20.16 22 16.42 22 12A10 10 0 0 0 12 2Z" />
              </svg>
            </a>
          </div>

        </div>
      </div>
    </footer>
  );
}
