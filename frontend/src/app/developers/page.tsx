'use client';

import React from 'react';
import Link from 'next/link';
import { PublicHeader } from '@/components/layout/PublicHeader';
import { PublicFooter } from '@/components/layout/PublicFooter';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { FileCode, Terminal, Cpu, ArrowRight, ExternalLink } from 'lucide-react';

export default function DevelopersPage() {
  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900 font-sans">
      <PublicHeader />

      <main className="flex-1 py-16">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <Badge variant="info" size="sm" className="mb-3 uppercase tracking-wider font-mono">
              Developer Documentation & MCP
            </Badge>
            <h1 className="text-4xl font-extrabold tracking-tight text-slate-900 sm:text-5xl">
              APIs, SDKs & Model Context Protocol
            </h1>
            <p className="mt-4 text-base text-slate-600 leading-relaxed">
              Integrate real-time fraud checking, trigger risk calculations, or execute autonomous AI agent tools via standard HTTP endpoints and MCP JSON-RPC.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-16">
            <Card className="border border-slate-200 shadow-sm">
              <CardHeader>
                <div className="h-10 w-10 rounded-lg bg-slate-900 text-white flex items-center justify-center mb-3">
                  <Terminal className="h-5 w-5 text-indigo-400" />
                </div>
                <CardTitle className="text-lg font-bold">Spring Cloud Gateway REST Endpoints</CardTitle>
                <CardDescription className="text-xs leading-relaxed">
                  All 27 microservices exposed behind `http://localhost:8080` with bearer JWT authentication.
                </CardDescription>
              </CardHeader>
              <CardContent className="pt-0">
                <div className="rounded bg-slate-900 p-3 text-[11px] font-mono text-slate-200">
                  <span className="text-emerald-400">POST</span> /fraud/check<br/>
                  <span className="text-blue-400">GET</span> /risk/profile/{'{userId}'}<br/>
                  <span className="text-purple-400">POST</span> /aml/screen
                </div>
              </CardContent>
            </Card>

            <Card className="border border-slate-200 shadow-sm">
              <CardHeader>
                <div className="h-10 w-10 rounded-lg bg-indigo-100 text-indigo-600 flex items-center justify-center mb-3">
                  <Cpu className="h-5 w-5" />
                </div>
                <CardTitle className="text-lg font-bold">Model Context Protocol (MCP)</CardTitle>
                <CardDescription className="text-xs leading-relaxed">
                  Extend LLM agent workflows using registered tools (`search_cases`, `calculate_risk`, `query_policy_rag`, `flag_mule_account`).
                </CardDescription>
              </CardHeader>
              <CardContent className="pt-0">
                <div className="rounded bg-slate-900 p-3 text-[11px] font-mono text-slate-200">
                  <span className="text-amber-400">POST</span> /fraud/mcp/execute<br/>
                  <span className="text-emerald-400">GET</span> /fraud/mcp/tools
                </div>
              </CardContent>
            </Card>
          </div>

          <div className="p-8 rounded-2xl bg-slate-900 text-white text-center">
            <h2 className="text-2xl font-bold">Explore Full Contract Mapping</h2>
            <p className="text-xs text-slate-400 mt-2 mb-6">
              Review detailed request/response schemas for all controllers in backend-contract-map.md.
            </p>
            <Link href="/mcp">
              <Button variant="primary" size="md" rightIcon={<ArrowRight className="h-4 w-4" />}>
                Go to MCP Tools Console
              </Button>
            </Link>
          </div>
        </div>
      </main>

      <PublicFooter />
    </div>
  );
}
