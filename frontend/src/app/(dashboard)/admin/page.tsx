'use client';

import React from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { StatCard } from '@/components/ui/StatCard';
import { ShieldCheck, UserCheck, Key, Server } from 'lucide-react';
import { RoleGuard } from '@/components/auth/RoleGuard';

export default function AdminPage() {
  return (
    <RoleGuard allowedRoles={['ADMIN', 'ROLE_ADMIN']}>
      <div className="space-y-6">
        <PageHeader
          title="System Administration & RBAC Management"
          description="Manage user clearance levels, microservice cluster settings, and API credentials."
          breadcrumbs={['SentinelX', 'Operations', 'Admin']}
        />

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <StatCard
            title="Registered Analysts"
            value="18 Active"
            change="RBAC Matrix"
            changeType="neutral"
            comparisonText="Auth Microservice User DB"
            icon={<UserCheck className="h-4 w-4 text-blue-600" />}
            statusVariant="info"
          />
          <StatCard
            title="API Keys Issued"
            value="12 Keys"
            change="HMAC Signed"
            changeType="neutral"
            comparisonText="Gateway Rate Limiting"
            icon={<Key className="h-4 w-4 text-purple-600" />}
            statusVariant="neutral"
          />
          <StatCard
            title="System Status"
            value="100% HEALTHY"
            change="27 Services"
            changeType="increase"
            comparisonText="Spring Boot Eureka Mesh"
            icon={<Server className="h-4 w-4 text-emerald-600" />}
            statusVariant="success"
          />
        </div>

        <Card>
          <CardHeader>
            <CardTitle>System Role Assignment Matrix</CardTitle>
            <CardDescription>Grant or revoke platform operational roles for analysts</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3 text-xs">
            {[
              { email: 'admin@upimesh', role: 'ADMIN', status: 'ACTIVE' },
              { email: 'analyst.vikram@upimesh', role: 'INVESTIGATOR', status: 'ACTIVE' },
              { email: 'risk.head@upimesh', role: 'RISK_ANALYST', status: 'ACTIVE' },
              { email: 'auditor@upimesh', role: 'AUDITOR', status: 'ACTIVE' },
            ].map((u) => (
              <div key={u.email} className="flex items-center justify-between p-3 rounded border border-slate-200 bg-slate-50 font-mono">
                <div>
                  <span className="font-bold text-slate-800">{u.email}</span>
                  <Badge variant="info" size="sm" className="ml-3">{u.role}</Badge>
                </div>
                <Button variant="outline" size="xs">
                  Edit Permissions
                </Button>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    </RoleGuard>
  );
}
