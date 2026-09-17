'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { PublicHeader } from '@/components/layout/PublicHeader';
import { PublicFooter } from '@/components/layout/PublicFooter';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Badge } from '@/components/ui/Badge';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Alert } from '@/components/ui/Alert';
import { Mail, Building, ShieldCheck, Send, CheckCircle2 } from 'lucide-react';

export default function ContactPage() {
  const [submitted, setSubmitted] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    organization: '',
    message: ''
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitted(true);
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900 font-sans">
      <PublicHeader />

      <main className="flex-1 py-16">
        <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-12">
            <Badge variant="info" size="sm" className="mb-3 uppercase tracking-wider font-mono">
              Enterprise Inquiry
            </Badge>
            <h1 className="text-4xl font-extrabold tracking-tight text-slate-900">
              Contact Enterprise Engineering
            </h1>
            <p className="mt-3 text-sm text-slate-600">
              Connect with our payment fraud intelligence solutions architects for technical deep-dives and platform deployment.
            </p>
          </div>

          <Card className="shadow-lg border-slate-200">
            <CardHeader>
              <div>
                <CardTitle>Schedule Technical Briefing</CardTitle>
                <CardDescription>Direct line to SentinelX product engineers and security auditors</CardDescription>
              </div>
            </CardHeader>
            <CardContent>
              {submitted ? (
                <Alert variant="success" title="Inquiry Received">
                  Thank you, {formData.name || 'Analyst'}. A SentinelX Solutions Architect will contact you at {formData.email || 'your email'} shortly.
                </Alert>
              ) : (
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <Input
                      label="Full Name"
                      placeholder="Vikram Singh"
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      required
                    />
                    <Input
                      label="Corporate Email"
                      type="email"
                      placeholder="vikram@bank.com"
                      value={formData.email}
                      onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                      required
                    />
                  </div>

                  <Input
                    label="Organization / Financial Institution"
                    placeholder="National Payment Network / Bank Corp"
                    value={formData.organization}
                    onChange={(e) => setFormData({ ...formData, organization: e.target.value })}
                    required
                  />

                  <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                      Architecture Requirements & Message
                    </label>
                    <textarea
                      rows={4}
                      className="w-full rounded-md border border-slate-300 bg-white p-3 text-xs text-slate-900 focus:border-blue-600 focus:outline-none focus:ring-1 focus:ring-blue-600"
                      placeholder="Describe expected transaction TPS, cloud/on-prem deployment preference, or fraud vector concerns..."
                      value={formData.message}
                      onChange={(e) => setFormData({ ...formData, message: e.target.value })}
                      required
                    />
                  </div>

                  <Button type="submit" variant="primary" fullWidth size="md" rightIcon={<Send className="h-4 w-4" />}>
                    Submit Technical Request
                  </Button>
                </form>
              )}
            </CardContent>
          </Card>
        </div>
      </main>

      <PublicFooter />
    </div>
  );
}
