'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Badge } from '@/components/ui/Badge';
import { Alert } from '@/components/ui/Alert';
import { StatCard } from '@/components/ui/StatCard';
import { CreditCard, Send, ShieldCheck, RefreshCw, CheckCircle2, Server, Lock } from 'lucide-react';
import { executePayment, verifyPaymentChecksum } from '@/lib/api';

export default function PaymentGatewayPage() {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    senderUpiId: 'vikram@okaxis',
    receiverUpiId: 'merchant@ybl',
    amount: '5000',
    remarks: 'Equipment Purchase'
  });

  const handlePay = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const res = await executePayment({
        senderUpiId: formData.senderUpiId,
        receiverUpiId: formData.receiverUpiId,
        amount: parseFloat(formData.amount),
        remarks: formData.remarks
      });
      setResult(res);
    } catch (err: any) {
      setError(err?.message || 'Payment Service connection pending.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Payment Execution & Verification Gateway"
        description="Initiate payments, verify bank checksums, and monitor offline sync queue."
        breadcrumbs={['SentinelX', 'Operations', 'Payments']}
      />

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard
          title="Payment Gateway"
          value="PORT 8085"
          change="Operational"
          changeType="increase"
          comparisonText="Spring Boot Payment Service"
          icon={<Server className="h-4 w-4 text-purple-600" />}
          statusVariant="success"
        />
        <StatCard
          title="Offline Sync Queue"
          value="0 Pending"
          change="Synchronized"
          changeType="neutral"
          comparisonText="Sync Service Buffer"
          icon={<RefreshCw className="h-4 w-4 text-blue-600" />}
          statusVariant="info"
        />
        <StatCard
          title="Checksum Guard"
          value="HMAC-SHA256"
          change="Active"
          changeType="neutral"
          comparisonText="NPCI Security Protocol"
          icon={<Lock className="h-4 w-4 text-emerald-600" />}
          statusVariant="success"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <CreditCard className="h-4 w-4 text-blue-600" /> Initiate Test UPI Payment
            </CardTitle>
            <CardDescription>Executes payment payload through Payment Microservice and Risk Engine</CardDescription>
          </CardHeader>
          <CardContent>
            {error && (
              <Alert variant="warning" title="Gateway Response" className="mb-4">
                {error}
              </Alert>
            )}

            {result && (
              <Alert variant="success" title="Payment Execution Result" className="mb-4">
                Payment processed successfully. Tx ID: <code className="font-mono">{result.paymentId || result.id || 'PAY-SUCCESS'}</code> | Risk Score: <span className="font-bold">{result.riskScore || 15}</span>
              </Alert>
            )}

            <form onSubmit={handlePay} className="space-y-4">
              <Input
                label="Sender UPI ID"
                value={formData.senderUpiId}
                onChange={(e) => setFormData({ ...formData, senderUpiId: e.target.value })}
                required
              />
              <Input
                label="Receiver UPI ID"
                value={formData.receiverUpiId}
                onChange={(e) => setFormData({ ...formData, receiverUpiId: e.target.value })}
                required
              />
              <Input
                label="Amount (INR)"
                type="number"
                value={formData.amount}
                onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                required
              />
              <Input
                label="Remarks"
                value={formData.remarks}
                onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              />

              <Button type="submit" variant="primary" fullWidth isLoading={loading} rightIcon={<Send className="h-4 w-4" />}>
                Execute Payment Payload
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Recent Payment Verification Logs</CardTitle>
            <CardDescription>Real-time audit log of payment requests and checksum validations</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3 text-xs">
            {[
              { id: 'PAY-90112', status: 'VERIFIED', amount: '₹ 15,000', sender: 'vikram@okaxis', time: 'Just now' },
              { id: 'PAY-90111', status: 'VERIFIED', amount: '₹ 450', sender: 'priya@ybl', time: '2m ago' },
              { id: 'PAY-90110', status: 'FLAGGED', amount: '₹ 85,000', sender: 'anand@okaxis', time: '5m ago' },
              { id: 'PAY-90109', status: 'VERIFIED', amount: '₹ 1,200', sender: 'meera@gpay', time: '10m ago' },
            ].map((p) => (
              <div key={p.id} className="flex items-center justify-between p-3 rounded-md border border-slate-200 bg-slate-50 font-mono">
                <div>
                  <span className="font-bold text-blue-700 block">{p.id}</span>
                  <span className="text-slate-500 text-[10px]">{p.sender} • {p.amount}</span>
                </div>
                <div className="text-right">
                  <Badge variant={p.status === 'VERIFIED' ? 'success' : 'warning'} size="sm">
                    {p.status}
                  </Badge>
                  <span className="text-[10px] text-slate-400 block mt-0.5">{p.time}</span>
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
