'use client';

import React from 'react';
import Link from 'next/link';
import { PublicHeader } from '@/components/layout/PublicHeader';
import { PublicFooter } from '@/components/layout/PublicFooter';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { ShieldCheck, Search, FileText, ArrowRight, CheckCircle2 } from 'lucide-react';

export default function AmlPage() {
  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900 font-sans">
      <PublicHeader />

      <main className="flex-1 py-16">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <Badge variant="warning" size="sm" className="mb-3 uppercase tracking-wider font-mono">
              AML & Compliance
            </Badge>
            <h1 className="text-4xl font-extrabold tracking-tight text-slate-900 sm:text-5xl">
              Anti-Money Laundering & Watchlist Screening
            </h1>
            <p className="mt-4 text-base text-slate-600 leading-relaxed">
              Automated entity screening against OFAC, UN Security Council, EU Sanctions, and Politically Exposed Persons (PEP) watchlists.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-16">
            <Card className="border border-slate-200 shadow-sm">
              <CardHeader>
                <div className="h-10 w-10 rounded-lg bg-emerald-100 text-emerald-600 flex items-center justify-center mb-3">
                  <Search className="h-5 w-5" />
                </div>
                <CardTitle className="text-lg font-bold">Watchlist Fuzzy Search</CardTitle>
                <CardDescription className="text-xs leading-relaxed">
                  Jaro-Winkler & Levenshtein distance matching across international sanctions datasets.
                </CardDescription>
              </CardHeader>
            </Card>

            <Card className="border border-slate-200 shadow-sm">
              <CardHeader>
                <div className="h-10 w-10 rounded-lg bg-rose-100 text-rose-600 flex items-center justify-center mb-3">
                  <FileText className="h-5 w-5" />
                </div>
                <CardTitle className="text-lg font-bold">Automated SAR Filing</CardTitle>
                <CardDescription className="text-xs leading-relaxed">
                  Generate Suspicious Activity Reports (SAR) with complete transaction trajectory & entity context.
                </CardDescription>
              </CardHeader>
            </Card>

            <Card className="border border-slate-200 shadow-sm">
              <CardHeader>
                <div className="h-10 w-10 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center mb-3">
                  <ShieldCheck className="h-5 w-5" />
                </div>
                <CardTitle className="text-lg font-bold">Regulatory Audits</CardTitle>
                <CardDescription className="text-xs leading-relaxed">
                  Cryptographically verified audit trails for all sanction resolution decisions.
                </CardDescription>
              </CardHeader>
            </Card>
          </div>

          <div className="p-8 rounded-2xl bg-slate-900 text-white text-center">
            <h2 className="text-2xl font-bold">Access AML Screening Workspace</h2>
            <p className="text-xs text-slate-400 mt-2 mb-6">
              Screen entity names, manage watchlists, and resolve compliance flags.
            </p>
            <Link href="/aml">
              <Button variant="primary" size="md" rightIcon={<ArrowRight className="h-4 w-4" />}>
                Go to AML Console
              </Button>
            </Link>
          </div>
        </div>
      </main>

      <PublicFooter />
    </div>
  );
}
