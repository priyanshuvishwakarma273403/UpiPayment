'use client';

import React from 'react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { StatCard } from '@/components/ui/StatCard';
import { useAuth } from '@/providers/AuthProvider';
import { ShieldCheck, ArrowRight, UserCheck, Lock, Activity, Server, Database } from 'lucide-react';
import Link from 'next/link';

export default function OnboardingPage() {
  const { user } = useAuth();

  const userRoles = user?.roles || (user?.role ? [user.role] : ['INVESTIGATOR']);

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 p-4 py-8">
      <div className="w-full max-w-3xl space-y-6">
        <div className="text-center">
          <div className="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-slate-900 text-white shadow-lg">
            <ShieldCheck className="h-8 w-8 text-indigo-400" />
          </div>
          <h1 className="mt-3 text-2xl font-bold tracking-tight text-slate-900">Welcome to SentinelX</h1>
          <p className="mt-1 text-sm text-slate-500">Analyst Workspace Initialization & System Orientation</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <StatCard
            title="Active User"
            value={user?.username || "Vikram Singh"}
            change="Verified"
            changeType="neutral"
            comparisonText={user?.email || "vikram@upimesh"}
            icon={<UserCheck className="h-4 w-4 text-emerald-600" />}
            statusVariant="success"
          />
          <StatCard
            title="Role Level"
            value={userRoles[0]?.replace(/^ROLE_/, '') || "INVESTIGATOR"}
            change={`${userRoles.length} Clearance(s)`}
            changeType="neutral"
            comparisonText="RBAC Authorization Matrix"
            icon={<Lock className="h-4 w-4 text-blue-600" />}
            statusVariant="info"
          />
          <StatCard
            title="Gateway Mesh"
            value="ACTIVE"
            change="Connected"
            changeType="increase"
            comparisonText="Spring Cloud Gateway"
            icon={<Server className="h-4 w-4 text-purple-600" />}
            statusVariant="neutral"
          />
        </div>

        <Card className="shadow-lg">
          <CardHeader>
            <div>
              <CardTitle>System Orientation & Security Protocol</CardTitle>
              <CardDescription>Review workspace parameter configuration before commencing fraud monitoring operations</CardDescription>
            </div>
          </CardHeader>

          <CardContent className="space-y-6">
            {/* Section 1: Assigned Workspace & Roles */}
            <div className="rounded-lg border border-slate-200 bg-slate-50 p-4 space-y-3">
              <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-2">
                <UserCheck className="h-4 w-4 text-blue-600" /> Assigned Roles & Access Scope
              </h4>
              <div className="flex flex-wrap gap-2">
                {userRoles.map((role) => (
                  <Badge key={role} variant="info" size="md" className="font-mono">
                    {role}
                  </Badge>
                ))}
                {['ADMIN', 'RISK_ANALYST', 'FRAUD_ANALYST', 'INVESTIGATOR', 'AUDITOR', 'OPERATIONS'].map((platformRole) => (
                  <Badge key={platformRole} variant="outline" size="sm" className="font-mono text-slate-500">
                    {platformRole}
                  </Badge>
                ))}
              </div>
              <p className="text-xs text-slate-600 leading-relaxed">
                Your authorization token grants access to real-time UPI transaction stream monitoring, risk vector calculation, graph multi-hop queries, and AI investigation tools.
              </p>
            </div>

            {/* Section 2: Security Credentials Status */}
            <div className="rounded-lg border border-slate-200 bg-slate-50 p-4 space-y-3">
              <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-2">
                <Lock className="h-4 w-4 text-emerald-600" /> Security Credentials & Invalidation Rules
              </h4>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs text-slate-600">
                <div className="flex items-center justify-between rounded bg-white p-2.5 border border-slate-200">
                  <span className="font-semibold text-slate-700">JWT Token Expiry:</span>
                  <span className="font-mono text-emerald-700 font-semibold">Auto-Refresh Active</span>
                </div>
                <div className="flex items-center justify-between rounded bg-white p-2.5 border border-slate-200">
                  <span className="font-semibold text-slate-700">Phone Verification:</span>
                  <span className="font-mono text-blue-700 font-semibold">OTP Verified</span>
                </div>
              </div>
            </div>

            {/* Section 3: Microservice Telemetry Overview */}
            <div className="rounded-lg border border-slate-200 bg-slate-50 p-4 space-y-3">
              <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-2">
                <Database className="h-4 w-4 text-purple-600" /> Connected Microservice Telemetry
              </h4>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-[11px]">
                <div className="rounded bg-white p-2 border border-slate-200 text-center font-mono">
                  <span className="text-slate-500 block">AUTH</span>
                  <span className="text-emerald-600 font-semibold">200 OK</span>
                </div>
                <div className="rounded bg-white p-2 border border-slate-200 text-center font-mono">
                  <span className="text-slate-500 block">RISK</span>
                  <span className="text-emerald-600 font-semibold">200 OK</span>
                </div>
                <div className="rounded bg-white p-2 border border-slate-200 text-center font-mono">
                  <span className="text-slate-500 block">FRAUD</span>
                  <span className="text-emerald-600 font-semibold">200 OK</span>
                </div>
                <div className="rounded bg-white p-2 border border-slate-200 text-center font-mono">
                  <span className="text-slate-500 block">ML SERVICE</span>
                  <span className="text-emerald-600 font-semibold">200 OK</span>
                </div>
              </div>
            </div>

            {/* Proceed Action Button */}
            <div className="pt-2">
              <Link href="/dashboard" className="w-full">
                <Button variant="primary" fullWidth size="lg" rightIcon={<ArrowRight className="h-4 w-4" />}>
                  Enter Operational Dashboard Workspace
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
