'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Input } from '@/components/ui/Input';
import { Search, ShieldAlert, ExternalLink, RefreshCw, Link as LinkIcon, Lock } from 'lucide-react';
import { useToast } from '@/lib/useToast';

interface CryptoTx {
  txHash: string;
  chain: 'ETHEREUM' | 'BITCOIN' | 'TRON' | 'SOLANA';
  fromWallet: string;
  toWallet: string;
  amountCrypto: string;
  usdEquivalent: number;
  mixerUsed: boolean;
  sanctionMatch: boolean;
}

export default function BlockchainAmlPage() {
  const { toast } = useToast();
  const [walletQuery, setWalletQuery] = useState('0x71C...9A10');
  const [loading, setLoading] = useState(false);

  const sampleCryptoTxs: CryptoTx[] = [
    {
      txHash: '0x89a1c4e7...22b10',
      chain: 'ETHEREUM',
      fromWallet: '0x71C...9A10 (OFAC Sanctioned)',
      toWallet: '0x33B...8812 (Tornado Cash Mixer)',
      amountCrypto: '42.5 ETH',
      usdEquivalent: 148000,
      mixerUsed: true,
      sanctionMatch: true,
    },
    {
      txHash: '0x11c090...44f99',
      chain: 'TRON',
      fromWallet: 'T9x...22K',
      toWallet: 'T88...91L',
      amountCrypto: '120,000 USDT',
      usdEquivalent: 120000,
      mixerUsed: false,
      sanctionMatch: false,
    },
  ];

  const handleScan = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      toast('On-Chain Blockchain Scan Complete', `Scanned wallet ${walletQuery} across 4 chains. Identified 1 Tornado Cash link.`, 'warning');
    }, 600);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Crypto & Cross-Border Web3 AML Blockchain Scanner"
        description="On-chain transaction tracing, Tornado Cash mixer detection, and OFAC sanctioned crypto wallet screening."
        breadcrumbs={['SentinelX', 'AML Operations', 'Crypto Scanner']}
      />

      <Card>
        <CardHeader>
          <CardTitle className="text-sm font-bold">On-Chain Wallet & Transaction Tracing</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <form onSubmit={handleScan} className="flex gap-3">
            <div className="flex-1">
              <Input
                placeholder="Enter Crypto Wallet Address or Transaction Hash (e.g. 0x71C...)..."
                value={walletQuery}
                onChange={(e) => setWalletQuery(e.target.value)}
                leftIcon={<Search className="h-4 w-4 text-slate-400" />}
              />
            </div>
            <Button type="submit" variant="primary" isLoading={loading} rightIcon={<Search className="h-4 w-4" />}>
              Scan Blockchain
            </Button>
          </form>

          {/* Crypto Ledger */}
          <div className="space-y-3">
            {sampleCryptoTxs.map((tx) => (
              <div key={tx.txHash} className="p-3.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 font-mono text-xs space-y-2">
                <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-200 dark:border-slate-800 pb-2">
                  <div className="flex items-center gap-2">
                    <LinkIcon className="h-4 w-4 text-indigo-500" />
                    <span className="font-bold text-slate-900 dark:text-slate-100">{tx.chain}</span>
                    <span className="text-[11px] text-slate-500">{tx.txHash}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    {tx.mixerUsed && <Badge variant="danger">Tornado Cash Mixer</Badge>}
                    {tx.sanctionMatch && <Badge variant="danger">OFAC Sanctions Match</Badge>}
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-[11px]">
                  <div>From: <span className="text-rose-600 font-semibold">{tx.fromWallet}</span></div>
                  <div>To: <span className="text-amber-600 font-semibold">{tx.toWallet}</span></div>
                  <div>Amount: <strong className="text-slate-900 dark:text-slate-100">{tx.amountCrypto} (${tx.usdEquivalent.toLocaleString()} USD)</strong></div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
