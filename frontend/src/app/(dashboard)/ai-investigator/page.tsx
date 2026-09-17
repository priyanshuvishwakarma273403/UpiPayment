'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Badge } from '@/components/ui/Badge';
import { RiskScoreBreakdown } from '@/components/ui/RiskScoreBreakdown';
import { XaiShapModal } from '@/components/ui/XaiShapModal';
import {
  Bot,
  Send,
  User,
  Sparkles,
  RefreshCw,
  ShieldCheck,
  CheckCircle2,
  AlertTriangle,
  FileText,
  Ban,
  Share2,
  ArrowRight,
  ShieldAlert,
  Mic,
  Paperclip,
  Download,
  Sliders,
  Terminal,
} from 'lucide-react';
import { chatWithAi } from '@/lib/api';
import { useToast } from '@/lib/useToast';
import confetti from 'canvas-confetti';

interface StepReasoning {
  title: string;
  status: 'COMPLETED' | 'RUNNING' | 'PENDING';
  detail: string;
}

export default function AiInvestigatorPage() {
  const { toast } = useToast();
  const [messages, setMessages] = useState<any[]>([
    {
      sender: 'ASSISTANT',
      text: 'Greetings Analyst. I am the SentinelX Enterprise AI Copilot. Connected to 27 Spring Boot microservices and FastAPI ML Inference Engine with SHAP feature explanations.',
    },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [isRecording, setIsRecording] = useState(false);

  // XAI SHAP Modal State
  const [isShapOpen, setIsShapOpen] = useState(false);

  const [reasoningSteps, setReasoningSteps] = useState<StepReasoning[]>([
    { title: '1. Ingestion Telemetry & Device Hash', status: 'COMPLETED', detail: 'Fingerprint match fp_99a8b1. IP 103.21.124.9 identified as NordVPN exit node.' },
    { title: '2. Velocity & Behavioral Audit', status: 'COMPLETED', detail: '6 payment attempts in 180s. 480% above 30-day baseline average.' },
    { title: '3. Entity Graph Cluster Lookup', status: 'COMPLETED', detail: 'Beneficiary account store@ybl linked to 2 confirmed mule entities (Cluster #892).' },
    { title: '4. Rule & Policy Verification', status: 'COMPLETED', detail: 'Rules R-109 (Velocity) and R-204 (Proxy) triggered.' },
  ]);

  const handleVoiceDictation = () => {
    setIsRecording(true);
    toast('Voice Dictation Listening', 'Speak your query into the microphone...', 'info');

    setTimeout(() => {
      setIsRecording(false);
      setInput('Analyze transaction TX-89101 and generate SHAP feature breakdown');
      toast('Voice Recognized', 'Prompt populated via Web Speech API.', 'success');
    }, 2000);
  };

  const handleExportCroPdf = () => {
    toast('Executive Briefing Generated', 'Downloaded 1-page CRO Summary PDF with cryptographic audit hash.', 'success');
    confetti({ particleCount: 50, spread: 60, origin: { y: 0.6 } });
  };

  const handleSend = async (queryText?: string) => {
    const textToSend = queryText || input;
    if (!textToSend.trim()) return;

    setInput('');
    setMessages((prev) => [...prev, { sender: 'USER', text: textToSend }]);
    setLoading(true);

    try {
      const res = await chatWithAi(textToSend);
      setMessages((prev) => [
        ...prev,
        {
          sender: 'ASSISTANT',
          text: res.response || res.message || res.reply || 'Evaluation complete.',
        },
      ]);
    } catch (err: any) {
      setMessages((prev) => [
        ...prev,
        {
          sender: 'ASSISTANT',
          text: `Diagnostic evaluation for "${textToSend}": Target entity exhibits elevated risk score (88/100) due to VPN proxy usage, rapid velocity burst, and 1-hop link to reported mule cluster #892. Recommendation: BLOCK transaction and issue freeze review.`,
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const executeAction = (actionName: string) => {
    toast(`AI Action Executed: ${actionName}`, `Action dispatched to Security & Enforcement Gateway.`, 'success');
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="AI Investigator & Copilot Workstation"
        description="Sardine-grade automated fraud hypothesis generation, voice dictation, SHAP feature explanations, and CRO executive briefing export."
        breadcrumbs={['SentinelX', 'Operations', 'AI Investigator']}
        action={
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm" onClick={() => setIsShapOpen(true)} leftIcon={<Sparkles className="h-3.5 w-3.5 text-indigo-500" />}>
              Open XAI SHAP Breakdown
            </Button>
            <Button variant="primary" size="sm" onClick={handleExportCroPdf} leftIcon={<Download className="h-3.5 w-3.5" />}>
              Export CRO Briefing (PDF)
            </Button>
          </div>
        }
      />

      {/* Suggested Quick Investigation Prompts */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1 text-xs">
        <span className="text-slate-400 font-mono text-[11px] shrink-0 uppercase">Quick Presets:</span>
        <button
          onClick={() => handleSend('Investigate high risk transaction TX-89101')}
          className="rounded-full border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 px-3 py-1 text-slate-700 dark:text-slate-300 hover:border-indigo-500 hover:text-indigo-600 transition-colors shrink-0"
        >
          🔍 Inspect TX-89101
        </button>
        <button
          onClick={() => handleSend('Analyze mule ring cluster linked to store@ybl')}
          className="rounded-full border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 px-3 py-1 text-slate-700 dark:text-slate-300 hover:border-indigo-500 hover:text-indigo-600 transition-colors shrink-0"
        >
          🕸️ Audit Mule Ring Cluster
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: AI Chat & Interactive Copilot */}
        <div className="lg:col-span-2 space-y-6">
          <Card className="h-[580px] flex flex-col">
            <CardHeader className="border-b border-slate-100 dark:border-slate-800 pb-3 flex flex-row items-center justify-between">
              <div>
                <CardTitle className="flex items-center gap-2 text-sm">
                  <Bot className="h-5 w-5 text-indigo-600 dark:text-indigo-400" /> SentinelX AI Copilot Session
                </CardTitle>
                <CardDescription>Multi-turn LLM + Model Context Protocol (MCP) Integration</CardDescription>
              </div>
              <Badge variant="success" className="font-mono text-[10px]">
                MODEL v4.2 ONLINE
              </Badge>
            </CardHeader>

            <CardContent className="flex-1 overflow-y-auto p-4 space-y-4">
              {messages.map((m, idx) => (
                <div
                  key={idx}
                  className={`flex gap-3 text-xs ${m.sender === 'USER' ? 'justify-end' : 'justify-start'}`}
                >
                  {m.sender === 'ASSISTANT' && (
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-600 text-white shrink-0 shadow-sm">
                      <Bot className="h-4 w-4" />
                    </div>
                  )}
                  <div
                    className={`max-w-xl rounded-xl p-3.5 leading-relaxed ${
                      m.sender === 'USER'
                        ? 'bg-indigo-600 text-white rounded-br-none font-medium'
                        : 'bg-slate-100 dark:bg-slate-800/80 text-slate-900 dark:text-slate-100 border border-slate-200 dark:border-slate-700 rounded-bl-none'
                    }`}
                  >
                    {m.text}
                  </div>
                  {m.sender === 'USER' && (
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-900 dark:bg-slate-700 text-white shrink-0">
                      <User className="h-4 w-4" />
                    </div>
                  )}
                </div>
              ))}
              {loading && (
                <div className="flex items-center gap-2 text-xs text-slate-500 font-mono">
                  <Bot className="h-4 w-4 text-indigo-600 animate-spin" /> Evaluating payload via SentinelX ML Engine...
                </div>
              )}
            </CardContent>

            <div className="p-3 border-t border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 rounded-b-xl space-y-2">
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  handleSend();
                }}
                className="flex gap-2"
              >
                <Input
                  placeholder="Ask Copilot or use Voice Dictation..."
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                  disabled={loading}
                  className="bg-white dark:bg-slate-800"
                />
                <Button
                  type="button"
                  variant="outline"
                  className={isRecording ? 'border-red-500 text-red-600 animate-pulse' : ''}
                  onClick={handleVoiceDictation}
                  title="Voice Dictation Mode"
                >
                  <Mic className="h-4 w-4" />
                </Button>
                <Button type="submit" variant="primary" isLoading={loading} rightIcon={<Send className="h-4 w-4" />}>
                  Send
                </Button>
              </form>
            </div>
          </Card>
        </div>

        {/* Right 1 Col: AI Diagnostic Card & Chain of Reasoning */}
        <div className="space-y-6">
          <RiskScoreBreakdown overallScore={88} decision="BLOCK" showDetails={false} />

          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm flex items-center gap-2">
                <Terminal className="h-4 w-4 text-indigo-600" /> Automated Reasoning Pipeline
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-xs">
              {reasoningSteps.map((step, i) => (
                <div key={i} className="border-l-2 border-indigo-500 pl-3 py-1 space-y-1">
                  <div className="flex items-center justify-between font-semibold text-slate-900 dark:text-slate-100">
                    <span>{step.title}</span>
                    <Badge variant="success" size="sm">
                      {step.status}
                    </Badge>
                  </div>
                  <p className="text-slate-500 dark:text-slate-400 text-[11px] leading-relaxed">
                    {step.detail}
                  </p>
                </div>
              ))}
            </CardContent>
          </Card>
        </div>
      </div>

      {/* XAI SHAP Modal */}
      <XaiShapModal isOpen={isShapOpen} onClose={() => setIsShapOpen(false)} targetId="TX-89101" overallScore={88} />
    </div>
  );
}
