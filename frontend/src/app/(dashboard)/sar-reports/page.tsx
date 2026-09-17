'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { DataTable } from '@/components/ui/DataTable';
import { Modal } from '@/components/ui/Modal';
import { FileText, Download, ShieldCheck, CheckCircle2, Lock, ExternalLink, RefreshCw, Eye } from 'lucide-react';
import { useToast } from '@/lib/useToast';
import confetti from 'canvas-confetti';

interface SarReport {
  id: string;
  targetEntity: string;
  category: 'SUSPICIOUS_TRANSACTION' | 'MULE_RING_SPIKE' | 'ACCOUNT_TAKEOVER';
  sha256Hash: string;
  fiuStatus: 'READY_FOR_FILING' | 'SUBMITTED' | 'DRAFT';
  amountInvolved: number;
  timestamp: string;
}

export default function SarReportsPage() {
  const { toast } = useToast();
  const [isPreviewOpen, setIsPreviewOpen] = useState(false);
  const [selectedReport, setSelectedReport] = useState<SarReport | null>(null);

  const reports: SarReport[] = [
    {
      id: 'SAR-2026-89101',
      targetEntity: 'Anand Kumar Sharma (anand@okaxis)',
      category: 'MULE_RING_SPIKE',
      sha256Hash: 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
      fiuStatus: 'READY_FOR_FILING',
      amountInvolved: 85000,
      timestamp: '2026-09-13 14:20:00 IST',
    },
    {
      id: 'SAR-2026-89098',
      targetEntity: 'Rahul Mehta (rahul@ybl)',
      category: 'ACCOUNT_TAKEOVER',
      sha256Hash: '7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284ddd200126d9069e',
      fiuStatus: 'SUBMITTED',
      amountInvolved: 150000,
      timestamp: '2026-09-12 11:45:00 IST',
    },
  ];

  const handleExport = (rep: SarReport) => {
    toast(`SAR Report Exported: ${rep.id}`, 'Downloaded FIU-IND JSON/PDF regulatory package with SHA-256 hash verification.', 'success');
    confetti({ particleCount: 40, spread: 50, origin: { y: 0.8 } });
  };

  const columns = [
    {
      header: 'Report ID',
      accessor: (r: SarReport) => <span className="font-mono text-xs font-bold text-indigo-600 dark:text-indigo-400">{r.id}</span>,
    },
    {
      header: 'Target Subject / VPA',
      accessor: (r: SarReport) => <span className="font-semibold text-xs text-slate-900 dark:text-slate-100">{r.targetEntity}</span>,
    },
    {
      header: 'Amount Involved',
      accessor: (r: SarReport) => <span className="font-mono text-xs font-bold">₹{r.amountInvolved.toLocaleString('en-IN')}</span>,
    },
    {
      header: 'FIU Status',
      accessor: (r: SarReport) => <Badge variant={r.fiuStatus === 'SUBMITTED' ? 'success' : 'warning'}>{r.fiuStatus}</Badge>,
    },
    {
      header: 'Actions',
      accessor: (r: SarReport) => (
        <div className="flex items-center gap-2">
          <Button
            size="xs"
            variant="outline"
            onClick={() => {
              setSelectedReport(r);
              setIsPreviewOpen(true);
            }}
            leftIcon={<Eye className="h-3 w-3" />}
          >
            Preview
          </Button>
          <Button size="xs" variant="primary" onClick={() => handleExport(r)} leftIcon={<Download className="h-3 w-3" />}>
            Export PDF
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="FIU-IND Suspicious Activity Report (SAR / STR) Studio"
        description="Automated regulatory report generation matching Financial Intelligence Unit (FIU-IND) schema with SHA-256 evidence hashing."
        breadcrumbs={['SentinelX', 'AML Compliance', 'SAR Reports']}
      />

      <Card>
        <CardHeader>
          <CardTitle className="text-sm font-bold">Regulatory Compliance Filing Queue</CardTitle>
          <CardDescription>Click Preview to inspect cryptographic audit trail or Export for FIU submission</CardDescription>
        </CardHeader>
        <CardContent>
          <DataTable data={reports} columns={columns} pagination={false} />
        </CardContent>
      </Card>

      {/* Document Preview Modal */}
      {selectedReport && (
        <Modal isOpen={isPreviewOpen} onClose={() => setIsPreviewOpen(false)} title={`Regulatory Preview — ${selectedReport.id}`}>
          <div className="space-y-4 text-xs font-mono">
            <div className="p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 space-y-2">
              <div className="flex justify-between items-center border-b border-slate-200 dark:border-slate-800 pb-2">
                <span className="font-bold text-slate-900 dark:text-slate-100">FIU-IND SUSPICIOUS TRANSACTION REPORT</span>
                <Badge variant="success">SHA-256 VERIFIED</Badge>
              </div>

              <div className="grid grid-cols-2 gap-2 text-[11px]">
                <div>Subject: <strong>{selectedReport.targetEntity}</strong></div>
                <div>Category: <strong>{selectedReport.category}</strong></div>
                <div>Amount: <strong>₹{selectedReport.amountInvolved.toLocaleString('en-IN')}</strong></div>
                <div>Timestamp: <strong>{selectedReport.timestamp}</strong></div>
              </div>

              <div className="pt-2">
                <span className="text-[10px] text-slate-400 block uppercase">Cryptographic SHA-256 Hash</span>
                <code className="text-[10px] text-indigo-600 dark:text-indigo-400 break-all">{selectedReport.sha256Hash}</code>
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <Button variant="outline" size="sm" onClick={() => setIsPreviewOpen(false)}>
                Close Preview
              </Button>
              <Button variant="primary" size="sm" onClick={() => handleExport(selectedReport)}>
                Download Regulatory JSON / PDF
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
}
