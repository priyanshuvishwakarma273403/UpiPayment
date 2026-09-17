'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { ShieldCheck, ArrowRight, Menu, X, ChevronDown, Activity, Cpu, Layers, Lock, FileCode, MessageSquare } from 'lucide-react';
import { Button } from '@/components/ui/Button';

export function PublicHeader() {
  const pathname = usePathname();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const navItems = [
    { label: 'Platform', href: '/platform' },
    { label: 'Fraud Intelligence', href: '/fraud-intelligence' },
    { label: 'Risk Engine', href: '/platform/risk' },
    { label: 'Payments', href: '/platform/payments' },
    { label: 'AML & Compliance', href: '/platform/aml' },
    { label: 'Security', href: '/security' },
    { label: 'Developers', href: '/developers' },
  ];

  return (
    <header className="sticky top-0 z-50 w-full border-b border-slate-200/80 bg-white/90 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Brand Logo */}
        <Link href="/" className="flex items-center gap-2.5 group">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-900 text-white shadow-md transition-transform group-hover:scale-105">
            <ShieldCheck className="h-5 w-5 text-indigo-400" />
          </div>
          <div>
            <span className="text-lg font-bold tracking-tight text-slate-900 font-mono">SENTINEL<span className="text-blue-600">X</span></span>
            <span className="hidden sm:inline-block ml-2 text-[10px] font-semibold text-slate-500 uppercase tracking-widest bg-slate-100 px-1.5 py-0.5 rounded border border-slate-200">Financial Fraud Intelligence</span>
          </div>
        </Link>

        {/* Desktop Navigation */}
        <nav className="hidden lg:flex items-center space-x-1 xl:space-x-2">
          {navItems.map((item) => {
            const isActive = pathname === item.href;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`px-3 py-2 text-xs font-semibold transition-colors rounded-md ${
                  isActive
                    ? 'text-blue-600 bg-blue-50/80 font-bold'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                }`}
              >
                {item.label}
              </Link>
            );
          })}
        </nav>

        {/* Action Buttons */}
        <div className="hidden sm:flex items-center gap-3">
          <Link href="/contact">
            <Button variant="ghost" size="sm" className="text-xs font-semibold text-slate-600 hover:text-slate-900">
              Contact Sales
            </Button>
          </Link>
          <Link href="/login">
            <Button variant="outline" size="sm" className="text-xs font-semibold border-slate-300">
              Sign In
            </Button>
          </Link>
          <Link href="/dashboard">
            <Button variant="primary" size="sm" className="text-xs font-semibold shadow-sm" rightIcon={<ArrowRight className="h-3.5 w-3.5" />}>
              Launch Console
            </Button>
          </Link>
        </div>

        {/* Mobile Menu Button */}
        <div className="flex lg:hidden items-center gap-2">
          <Link href="/login">
            <Button variant="outline" size="xs" className="text-xs border-slate-300">
              Sign In
            </Button>
          </Link>
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="p-2 text-slate-600 hover:text-slate-900 rounded-md focus:outline-none"
            aria-label="Toggle menu"
          >
            {mobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div className="lg:hidden border-b border-slate-200 bg-white px-4 pt-2 pb-6 space-y-2 shadow-lg">
          {navItems.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              onClick={() => setMobileMenuOpen(false)}
              className={`block px-3 py-2 text-sm font-medium rounded-md ${
                pathname === item.href ? 'bg-blue-50 text-blue-600 font-bold' : 'text-slate-700 hover:bg-slate-50'
              }`}
            >
              {item.label}
            </Link>
          ))}
          <div className="pt-4 border-t border-slate-100 flex flex-col gap-2">
            <Link href="/contact" onClick={() => setMobileMenuOpen(false)}>
              <Button variant="outline" fullWidth size="sm" className="text-xs">
                Contact Sales
              </Button>
            </Link>
            <Link href="/dashboard" onClick={() => setMobileMenuOpen(false)}>
              <Button variant="primary" fullWidth size="sm" className="text-xs" rightIcon={<ArrowRight className="h-3.5 w-3.5" />}>
                Launch Console
              </Button>
            </Link>
          </div>
        </div>
      )}
    </header>
  );
}
