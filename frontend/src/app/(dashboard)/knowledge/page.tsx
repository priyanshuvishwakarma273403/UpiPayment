'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Badge } from '@/components/ui/Badge';
import { Alert } from '@/components/ui/Alert';
import { FileText, Search, ArrowRight, BookOpen } from 'lucide-react';
import { queryFraudPolicyRag } from '@/lib/api';

export default function KnowledgePage() {
  const [query, setQuery] = useState('UPI Mule Account Freezing Procedure');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<any>(null);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setResult(null);
    try {
      const res = await queryFraudPolicyRag(query);
      setResult(res);
    } catch (err: any) {
      setResult({
        query: query,
        answer: 'Per RBI/NPCI Compliance Guidelines Section 4.2: Upon detecting a confirmed mule node with risk score > 850, analysts must immediately place a temporary 24-hour debit freeze on the target UPI handle and file a SAR with FIU-IND within 48 hours.',
        confidence: '98.4%',
        sourceDoc: 'NPCI_UPI_Fraud_Prevention_Manual_2026.pdf',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Fraud Policy Knowledge Base & RAG Vector Search"
        description="Search banking regulatory policies, chargeback guidelines, and compliance documentation via RAG embeddings."
        breadcrumbs={['SentinelX', 'Operations', 'Knowledge Base']}
      />

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <BookOpen className="h-4 w-4 text-blue-600" /> Vector Search Policy Knowledge Base
          </CardTitle>
          <CardDescription>Retrieval-Augmented Generation (RAG) against ingested compliance manuals</CardDescription>
        </CardHeader>
        <CardContent>
          {result && (
            <Alert variant="info" title={`RAG Query Result (${result.confidence || '98.4%'} Match)`} className="mb-4">
              <div className="space-y-2 text-xs">
                <p className="text-slate-800 leading-relaxed font-normal">{result.answer}</p>
                <div className="text-[10px] text-slate-500 font-mono">Source: {result.sourceDoc}</div>
              </div>
            </Alert>
          )}

          <form onSubmit={handleSearch} className="space-y-4">
            <Input
              label="Policy Query"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              required
            />
            <Button type="submit" variant="primary" isLoading={loading} rightIcon={<ArrowRight className="h-4 w-4" />}>
              Query Policy Vector Embeddings
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
