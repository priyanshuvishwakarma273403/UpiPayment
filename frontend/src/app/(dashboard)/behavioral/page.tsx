'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Smartphone, Zap, AlertTriangle, UserCheck, Activity, ShieldAlert } from 'lucide-react';
import { useToast } from '@/lib/useToast';

export default function BehavioralPage() {
  const { toast } = useToast();
  const [touchHeatmap, setTouchHeatmap] = useState([
    { x: 45, y: 30, pressure: 0.85, isBot: false },
    { x: 50, y: 75, pressure: 0.92, isBot: false },
    { x: 50, y: 75, pressure: 0.95, isBot: true }, // Exact repeated coordinate (Bot signature)
  ]);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Biometric Behavioral Telemetry & Touch Dynamics"
        description="Real-time mobile touch pressure heatmaps, keystroke dynamics (dwell & flight time), and automated bot typing speed detection."
        breadcrumbs={['SentinelX', 'Operations', 'Behavioral Telemetry']}
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Mobile Touch Heatmap & Keystroke Dynamics */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-bold flex items-center justify-between">
                <span className="flex items-center gap-2">
                  <Smartphone className="h-4 w-4 text-indigo-600 dark:text-indigo-400" /> Mobile Screen Touch & Pressure Heatmap
                </span>
                <Badge variant="warning" className="font-mono text-[10px]">
                  HIGH PASTE FREQUENCY FLAG
                </Badge>
              </CardTitle>
              <CardDescription>Visualizing touch pressure points and paste events during checkout</CardDescription>
            </CardHeader>

            <CardContent className="space-y-4">
              {/* Phone Mockup Canvas Container */}
              <div className="relative mx-auto w-64 h-96 rounded-3xl border-4 border-slate-800 bg-slate-950 p-4 shadow-2xl overflow-hidden text-white">
                <div className="absolute top-2 left-1/2 -translate-x-1/2 h-4 w-20 bg-slate-800 rounded-full" />

                {/* Touch Pressure Heatmap Points */}
                {touchHeatmap.map((pt, i) => (
                  <div
                    key={i}
                    className={`absolute rounded-full transform -translate-x-1/2 -translate-y-1/2 ${
                      pt.isBot
                        ? 'h-12 w-12 bg-rose-500/60 border border-rose-400 animate-ping'
                        : 'h-10 w-10 bg-indigo-500/40 border border-indigo-400'
                    }`}
                    style={{ left: `${pt.x}%`, top: `${pt.y}%` }}
                  />
                ))}

                <div className="absolute bottom-4 left-4 right-4 text-center font-mono text-[10px] text-slate-400 bg-slate-900/90 p-2 rounded-lg border border-slate-800">
                  Touch Pressure: 0.95 (Excessive static pressure = Bot/Script)
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right 1 Col: Typing Speed & Bot Indicators */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-bold">Keystroke & Bot Metrics</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-xs font-mono">
              <div className="p-3 rounded-lg bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-1">
                <div className="flex justify-between text-[11px]">
                  <span className="text-slate-500">Dwell Time (Key Down):</span>
                  <span className="font-bold">12 ms (Superhuman)</span>
                </div>
                <div className="flex justify-between text-[11px]">
                  <span className="text-slate-500">Flight Time (Key to Key):</span>
                  <span className="font-bold">4 ms (Scripted)</span>
                </div>
                <div className="flex justify-between text-[11px]">
                  <span className="text-slate-500">Paste Count (UPI VPA):</span>
                  <span className="font-bold text-rose-600">4 Pastes</span>
                </div>
              </div>

              <div className="p-3 rounded-lg bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900/60 text-rose-900 dark:text-rose-300 space-y-1">
                <span className="font-bold block">Biometric Hypothesis:</span>
                AUTOMATED SCRIPT / BOT TYPING DETECTED (98.4% Confidence Score).
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
