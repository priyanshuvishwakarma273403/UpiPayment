'use client';

import React from 'react';
import Link from 'next/link';
import { PublicHeader } from '@/components/layout/PublicHeader';
import { PublicFooter } from '@/components/layout/PublicFooter';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Network, Bot, ShieldCheck, ArrowRight, CheckCircle2, Sparkles, Activity } from 'lucide-react';

export default function FraudIntelligencePage() {
  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900 font-sans">
      <PublicHeader />

      <main className="flex-1 py-16">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-12">
            <Badge variant="warning" size="sm" className="mb-3 uppercase tracking-wider font-mono">
              Fraud Intelligence
            </Badge>
            <h1 className="text-4xl font-extrabold tracking-tight text-slate-900 sm:text-5xl">
              Fraud is a Network Problem, Not a Single Transaction Problem.
            </h1>
            <p className="mt-4 text-base text-slate-600 leading-relaxed">
              Combining XGBoost Machine Learning, Model Context Protocol (MCP) agents, policy RAG knowledge lookup, and multi-hop graph cluster analytics to uncover hidden risk propagation across connected entities.
            </p>
          </div>

          {/* Hero Visual Showcase */}
          <div className="mb-16 rounded-2xl overflow-hidden border border-slate-200 shadow-xl bg-white group">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src="/fraud-network-problem.jpg"
              alt="Fraud is a Network Problem - SentinelX Entity Relationship Graph"
              className="w-full h-auto object-cover rounded-2xl transition-transform duration-500 group-hover:scale-101"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-16">
            <Card className="border border-slate-200 shadow-sm">
              <CardHeader>
                <div className="h-10 w-10 rounded-lg bg-rose-100 text-rose-600 flex items-center justify-center mb-3">
                  <Bot className="h-5 w-5" />
                </div>
                <CardTitle className="text-lg font-bold">AI Investigator Copilot</CardTitle>
                <CardDescription className="text-xs leading-relaxed">
                  Interactive multi-turn diagnostic assistant equipped with MCP tools for real-time risk profile lookup, SHAP explanations, and action execution.
                </CardDescription>
              </CardHeader>
              <CardContent className="pt-0 text-xs text-slate-600 space-y-2">
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="h-4 w-4 text-emerald-600" />
                  <span>Model Context Protocol (MCP) tool execution</span>
                </div>
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="h-4 w-4 text-emerald-600" />
                  <span>SHAP feature weight attribution breakdown</span>
                </div>
              </CardContent>
            </Card>

            <Card className="border border-slate-200 shadow-sm">
              <CardHeader>
                <div className="h-10 w-10 rounded-lg bg-indigo-100 text-indigo-600 flex items-center justify-center mb-3">
                  <Network className="h-5 w-5" />
                </div>
                <CardTitle className="text-lg font-bold">Multi-Hop Graph Visualizer</CardTitle>
                <CardDescription className="text-xs leading-relaxed">
                  Traverses transaction graphs across customer accounts, shared device fingerprints, and merchant UPI IDs to isolate mule networks.
                </CardDescription>
              </CardHeader>
              <CardContent className="pt-0 text-xs text-slate-600 space-y-2">
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="h-4 w-4 text-emerald-600" />
                  <span>Interactive node clustering and risk propagation</span>
                </div>
                <div className="flex items-center gap-2">
                  <CheckCircle2 className="h-4 w-4 text-emerald-600" />
                  <span>Sub-10ms graph query latency</span>
                </div>
              </CardContent>
            </Card>
          </div>

          <div className="p-8 rounded-2xl bg-gradient-to-r from-blue-900 to-indigo-900 text-white text-center">
            <h2 className="text-2xl font-bold">Launch Fraud Operations Console</h2>
            <p className="text-xs text-slate-300 mt-2 mb-6">
              Review live fraud cases, run attack simulations, and approve continuous ML candidate models.
            </p>
            <Link href="/fraud">
              <Button variant="primary" size="md" rightIcon={<ArrowRight className="h-4 w-4" />}>
                Go to Fraud Console
              </Button>
            </Link>
          </div>
        </div>
      </main>

      <PublicFooter />
    </div>
  );
}
