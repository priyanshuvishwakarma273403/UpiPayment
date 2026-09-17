'use client';

import React, { useState } from 'react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  Legend,
} from 'recharts';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Activity, ShieldCheck, Zap, Filter, ArrowUpRight } from 'lucide-react';

const areaData = [
  { time: '14:00', totalVolume: 1240, blockedFraud: 42, flaggedReview: 18 },
  { time: '14:05', totalVolume: 1450, blockedFraud: 65, flaggedReview: 24 },
  { time: '14:10', totalVolume: 1680, blockedFraud: 98, flaggedReview: 31 },
  { time: '14:15', totalVolume: 1390, blockedFraud: 54, flaggedReview: 19 },
  { time: '14:20', totalVolume: 1820, blockedFraud: 120, flaggedReview: 45 },
  { time: '14:25', totalVolume: 1540, blockedFraud: 78, flaggedReview: 28 },
  { time: '14:30', totalVolume: 1910, blockedFraud: 142, flaggedReview: 52 },
];

const histogramData = [
  { range: '0-20 (Safe)', count: 8420, fill: '#10b981' },
  { range: '21-45 (Low)', count: 3210, fill: '#3b82f6' },
  { range: '46-70 (Medium)', count: 890, fill: '#f59e0b' },
  { range: '71-85 (Elevated)', count: 420, fill: '#f97316' },
  { range: '86-100 (Critical)', count: 185, fill: '#ef4444' },
];

export function InteractiveFraudRadar() {
  const [timeframe, setTimeframe] = useState<'30m' | '1h' | '24h'>('30m');

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
      {/* 2 Cols: Real-Time Payment Stream Area Chart */}
      <Card className="lg:col-span-2">
        <CardHeader className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2">
          <div>
            <CardTitle className="flex items-center gap-2 text-sm">
              <Activity className="h-4 w-4 text-indigo-600 dark:text-indigo-400" /> Live Transaction Stream vs Blocked Fraud Volume
            </CardTitle>
            <CardDescription>Sub-second telemetry telemetry feed via Gateway</CardDescription>
          </div>

          <div className="flex items-center gap-1 bg-slate-100 dark:bg-slate-800 p-1 rounded-lg text-xs font-medium">
            <button
              onClick={() => setTimeframe('30m')}
              className={`px-2.5 py-1 rounded-md transition-colors ${
                timeframe === '30m'
                  ? 'bg-white dark:bg-slate-700 text-slate-900 dark:text-slate-100 shadow-xs font-semibold'
                  : 'text-slate-500 dark:text-slate-400 hover:text-slate-900'
              }`}
            >
              30m
            </button>
            <button
              onClick={() => setTimeframe('1h')}
              className={`px-2.5 py-1 rounded-md transition-colors ${
                timeframe === '1h'
                  ? 'bg-white dark:bg-slate-700 text-slate-900 dark:text-slate-100 shadow-xs font-semibold'
                  : 'text-slate-500 dark:text-slate-400 hover:text-slate-900'
              }`}
            >
              1h
            </button>
            <button
              onClick={() => setTimeframe('24h')}
              className={`px-2.5 py-1 rounded-md transition-colors ${
                timeframe === '24h'
                  ? 'bg-white dark:bg-slate-700 text-slate-900 dark:text-slate-100 shadow-xs font-semibold'
                  : 'text-slate-500 dark:text-slate-400 hover:text-slate-900'
              }`}
            >
              24h
            </button>
          </div>
        </CardHeader>

        <CardContent>
          <div className="h-64 w-full pt-2">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={areaData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorTotal" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#6366f1" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="colorFraud" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#ef4444" stopOpacity={0.6} />
                    <stop offset="95%" stopColor="#ef4444" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
                <XAxis dataKey="time" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0f172a',
                    borderColor: '#334155',
                    borderRadius: '8px',
                    color: '#fff',
                    fontSize: '12px',
                  }}
                />
                <Area type="monotone" dataKey="totalVolume" name="Total Transactions" stroke="#6366f1" fillOpacity={1} fill="url(#colorTotal)" strokeWidth={2} />
                <Area type="monotone" dataKey="blockedFraud" name="Blocked Fraud Volume" stroke="#ef4444" fillOpacity={1} fill="url(#colorFraud)" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </CardContent>
      </Card>

      {/* 1 Col: Risk Score Distribution Histogram */}
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-bold flex items-center justify-between">
            <span>Risk Score Distribution</span>
            <Badge variant="neutral" className="font-mono text-[10px]">
              13,125 EVALUATED
            </Badge>
          </CardTitle>
          <CardDescription>Histogram across risk engine scoring bands</CardDescription>
        </CardHeader>

        <CardContent>
          <div className="h-64 w-full pt-2">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={histogramData} margin={{ top: 10, right: 10, left: -25, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
                <XAxis dataKey="range" tick={{ fontSize: 9 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0f172a',
                    borderColor: '#334155',
                    borderRadius: '8px',
                    color: '#fff',
                    fontSize: '12px',
                  }}
                />
                <Bar dataKey="count" name="Evaluations" radius={[4, 4, 0, 0]} fill="#6366f1" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
