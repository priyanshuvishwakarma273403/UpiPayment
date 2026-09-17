'use client';

import React from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Settings, Sliders, ShieldCheck } from 'lucide-react';

export default function SettingsPage() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Workspace & System Settings"
        description="Configure API gateway endpoints, telemetry refresh rates, and notification webhooks."
        breadcrumbs={['SentinelX', 'Operations', 'Settings']}
      />

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Settings className="h-4 w-4 text-blue-600" /> Gateway Connection Settings
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4 max-w-xl text-xs">
          <Input
            label="API Gateway Base URL"
            defaultValue={process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'}
            disabled
          />
          <Input
            label="FastAPI ML Inference URL"
            defaultValue="http://localhost:8000"
            disabled
          />
          <Button variant="primary" size="sm">
            Save System Configurations
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
