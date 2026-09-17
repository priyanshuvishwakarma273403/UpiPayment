'use client';

import React from 'react';
import Link from 'next/link';
import { PublicHeader } from '@/components/layout/PublicHeader';
import { PublicFooter } from '@/components/layout/PublicFooter';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Lock, ShieldCheck, Key, FileCheck, ArrowRight } from 'lucide-react';

export default function SecurityPage() {
  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900 font-sans">
      <PublicHeader />

      <main className="flex-1 py-16">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <Badge variant="success" size="sm" className="mb-3 uppercase tracking-wider font-mono">
              Enterprise Security Architecture
            </Badge>
            <h1 className="text-4xl font-extrabold tracking-tight text-slate-900 sm:text-5xl">
              Zero-Trust Financial Protection
            </h1>
            <p className="mt-4 text-base text-slate-600 leading-relaxed">
              SentinelX enforces strict role-based authorization, JWT token rotation, secrets management, and cryptographic audit ledgers across every service layer.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-16">
            <Card className="border border-slate-200 shadow-sm">
              <CardHeader>
                <div className="h-10 w-10 rounded-lg bg-emerald-100 text-emerald-600 flex items-center justify-center mb-3">
                  <Lock className="h-5 w-5" />
                </div>
                <CardTitle className="text-lg font-bold">Role-Based Access Control (RBAC)</CardTitle>
                <CardDescription className="text-xs leading-relaxed">
                  Fine-grained operational permissions for ADMIN, RISK_ANALYST, FRAUD_ANALYST, INVESTIGATOR, AUDITOR, and OPERATIONS users.
                </CardDescription>
              </CardHeader>
            </Card>

            <Card className="border border-slate-200 shadow-sm">
              <CardHeader>
                <div className="h-10 w-10 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center mb-3">
                  <Key className="h-5 w-5" />
                </div>
                <CardTitle className="text-lg font-bold">HMAC & Service Authentication</CardTitle>
                <CardDescription className="text-xs leading-relaxed">
                  Inter-service communication is secured via mTLS and HMAC payload verification signatures.
                </CardDescription>
              </CardHeader>
            </Card>
          </div>

          <div className="p-8 rounded-2xl bg-slate-900 text-white text-center">
            <h2 className="text-2xl font-bold">View Security & Audit Logs</h2>
            <p className="text-xs text-slate-400 mt-2 mb-6">
              Review immutable audit logs, active user sessions, and permission matrices.
            </p>
            <Link href="/audit">
              <Button variant="primary" size="md" rightIcon={<ArrowRight className="h-4 w-4" />}>
                Go to Audit Console
              </Button>
            </Link>
          </div>
        </div>
      </main>

      <PublicFooter />
    </div>
  );
}
