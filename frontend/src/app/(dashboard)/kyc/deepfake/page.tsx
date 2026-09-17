'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Video, Mic, ShieldAlert, Sparkles, Activity, CheckCircle2 } from 'lucide-react';
import { useToast } from '@/lib/useToast';

export default function DeepfakeKycPage() {
  const { toast } = useToast();
  const [analyzing, setAnalyzing] = useState(false);

  const runSpectrumAnalysis = () => {
    setAnalyzing(true);
    setTimeout(() => {
      setAnalyzing(false);
      toast('Deepfake Audio Spectrum Analysis Complete', 'Facial mesh confidence 99.2%. No synthetic voice clone detected.', 'success');
    }, 800);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="AI Video-KYC Synthetic Voice & Deepfake Detector"
        description="Real-time audio frequency spectrum analysis, facial landmark mesh tracking, and generative AI deepfake detection."
        breadcrumbs={['SentinelX', 'KYC Operations', 'Deepfake Detector']}
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Live Video Feed Mockup & Spectrum */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-bold flex items-center justify-between">
                <span className="flex items-center gap-2">
                  <Video className="h-4 w-4 text-indigo-600 dark:text-indigo-400" /> Video-KYC Live Stream & Facial Landmark Mesh
                </span>
                <Badge variant="success" className="font-mono text-[10px]">
                  LIVENESS CONFIRMED (99.2%)
                </Badge>
              </CardTitle>
            </CardHeader>

            <CardContent className="space-y-4">
              {/* Video Mockup Canvas */}
              <div className="relative w-full h-80 rounded-2xl bg-slate-950 border border-slate-800 p-4 flex flex-col justify-between text-white shadow-2xl overflow-hidden">
                <div className="absolute inset-0 bg-[radial-gradient(#334155_1px,transparent_1px)] [background-size:20px_20px] opacity-25" />

                {/* Facial Mesh Landmark Overlay */}
                <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                  <div className="h-48 w-48 rounded-full border-2 border-emerald-400/60 border-dashed flex items-center justify-center relative animate-pulse">
                    <span className="font-mono text-[10px] text-emerald-400 bg-slate-900/90 px-2 py-0.5 rounded border border-emerald-800">
                      68 FACIAL LANDMARKS VERIFIED
                    </span>
                  </div>
                </div>

                <div className="relative z-10 flex justify-between font-mono text-xs">
                  <span className="text-emerald-400 font-bold flex items-center gap-1.5">
                    <Activity className="h-4 w-4 animate-spin" /> Live Liveness Tracking
                  </span>
                  <span className="text-slate-400">Subject: Anand Kumar Sharma</span>
                </div>

                <div className="relative z-10 font-mono text-[11px] bg-slate-900/90 border border-slate-800 p-2.5 rounded-xl flex justify-between items-center">
                  <span>Audio Spectrum: Natural Vocal Formant (No TTS Synthesis)</span>
                  <Button size="xs" variant="primary" onClick={runSpectrumAnalysis} isLoading={analyzing}>
                    Run Deepfake Audit
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right 1 Col: Deepfake Probability Stats */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-bold">Generative AI Biomarkers</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-xs font-mono">
              <div className="p-3 rounded-lg bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-1.5">
                <div className="flex justify-between">
                  <span className="text-slate-500">Synthetic Voice Clone:</span>
                  <Badge variant="success">0.08% RISK</Badge>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Deepfake Face Swap:</span>
                  <Badge variant="success">0.02% RISK</Badge>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Playback Attack:</span>
                  <Badge variant="success">0.12% RISK</Badge>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
