'use client';

import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { Network, ShieldAlert, AlertTriangle, CheckCircle2, Lock, ArrowRight, Eye, RefreshCw } from 'lucide-react';

interface NodeInfo {
  id: string;
  name: string;
  type: 'sender' | 'mule' | 'beneficiary' | 'device';
  riskScore: number;
  status: string;
  transCount: number;
  volume: string;
  details: string;
}

export function NetworkGraphVisualizer() {
  const [selectedNodeId, setSelectedNodeId] = useState<string>('mule-1');
  const [isFrozen, setIsFrozen] = useState(false);

  const nodes: Record<string, NodeInfo> = {
    'sender-1': {
      id: 'sender-1',
      name: 'UPI-SENDER (User #9841)',
      type: 'sender',
      riskScore: 240,
      status: 'SAFE',
      transCount: 14,
      volume: '₹ 45,000',
      details: 'Legitimate savings account compromised via phishing SMS.',
    },
    'mule-1': {
      id: 'mule-1',
      name: 'MULE-HUB (#RING-8832)',
      type: 'mule',
      riskScore: 940,
      status: 'CRITICAL',
      transCount: 184,
      volume: '₹ 14,80,000',
      details: 'High-velocity structuring hub. 18 incoming UPI payments immediately forwarded within 45 seconds.',
    },
    'beneficiary-1': {
      id: 'beneficiary-1',
      name: 'BENEFICIARY (Offshore Merchant)',
      type: 'beneficiary',
      riskScore: 890,
      status: 'SUSPICIOUS',
      transCount: 88,
      volume: '₹ 12,50,000',
      details: 'Synthetic merchant entity registered with disposable GSTIN.',
    },
    'device-1': {
      id: 'device-1',
      name: 'SHARED DEVICE (IMEI #88391)',
      type: 'device',
      riskScore: 990,
      status: 'BLOCKED',
      transCount: 312,
      volume: '₹ 28,00,000',
      details: 'Single emulator hardware signature tied to 14 distinct UPI VPAs.',
    },
  };

  const selectedNode = nodes[selectedNodeId] || nodes['mule-1'];

  return (
    <div className="rounded-2xl border border-slate-800 bg-slate-950 p-6 shadow-2xl space-y-5 text-white">
      {/* Visualizer Top Bar */}
      <div className="flex items-center justify-between border-b border-slate-800/80 pb-3">
        <div className="flex items-center gap-2">
          <div className="h-8 w-8 rounded-lg bg-indigo-500/20 text-indigo-400 border border-indigo-500/30 flex items-center justify-center">
            <Network className="h-4 w-4" />
          </div>
          <div>
            <div className="text-xs font-bold font-mono text-white flex items-center gap-2">
              Multi-Hop Graph Topology Visualizer
              {isFrozen && (
                <span className="text-[9px] font-mono bg-rose-500/20 text-rose-400 px-1.5 py-0.2 rounded border border-rose-500/30 animate-pulse">
                  RING FROZEN
                </span>
              )}
            </div>
            <div className="text-[10px] text-slate-400 font-mono">Cluster #MULE-8832 • Real-Time Community Detection</div>
          </div>
        </div>

        <button
          onClick={() => setIsFrozen(!isFrozen)}
          className={`px-3 py-1.5 rounded-lg text-xs font-mono font-bold transition-all ${
            isFrozen
              ? 'bg-emerald-600 hover:bg-emerald-500 text-white'
              : 'bg-rose-600 hover:bg-rose-500 text-white shadow-lg shadow-rose-600/20'
          }`}
        >
          {isFrozen ? 'Release Freeze' : 'Emergency Freeze Ring'}
        </button>
      </div>

      {/* Interactive SVG Canvas */}
      <div className="relative h-64 sm:h-72 w-full rounded-xl bg-slate-900/90 border border-slate-800 overflow-hidden flex items-center justify-center">
        {/* Background Grid */}
        <div className="absolute inset-0 bg-[linear-gradient(to_right,#1e293b_1px,transparent_1px),linear-gradient(to_bottom,#1e293b_1px,transparent_1px)] bg-[size:2rem_2rem] opacity-25 pointer-events-none" />

        {/* SVG Linking Lines with Animated Particles */}
        <svg className="absolute inset-0 w-full h-full pointer-events-none">
          <line x1="20%" y1="50%" x2="50%" y2="35%" stroke="#6366f1" strokeWidth="2" strokeDasharray="4" className="animate-pulse" />
          <line x1="50%" y1="35%" x2="80%" y2="50%" stroke="#f43f5e" strokeWidth="2.5" />
          <line x1="50%" y1="35%" x2="50%" y2="80%" stroke="#f59e0b" strokeWidth="2" strokeDasharray="3" />
        </svg>

        {/* Interactive Node 1: Sender */}
        <div
          onClick={() => setSelectedNodeId('sender-1')}
          className={`absolute left-[15%] top-[40%] cursor-pointer -translate-x-1/2 -translate-y-1/2 transition-transform duration-200 hover:scale-110 ${
            selectedNodeId === 'sender-1' ? 'scale-110 z-20' : 'z-10'
          }`}
        >
          <div className="p-3 rounded-full bg-blue-500/20 border-2 border-blue-500 text-blue-300 text-center font-mono text-[10px] shadow-lg backdrop-blur-md">
            <div>SENDER</div>
            <div className="text-[9px] text-blue-200">₹45k</div>
          </div>
        </div>

        {/* Interactive Node 2: Mule Hub */}
        <div
          onClick={() => setSelectedNodeId('mule-1')}
          className={`absolute left-[50%] top-[28%] cursor-pointer -translate-x-1/2 -translate-y-1/2 transition-transform duration-200 hover:scale-110 ${
            selectedNodeId === 'mule-1' ? 'scale-110 z-20' : 'z-10'
          }`}
        >
          <div className="relative p-4 rounded-full bg-rose-500/20 border-2 border-rose-500 text-rose-300 text-center font-mono text-xs shadow-xl shadow-rose-500/20 backdrop-blur-md animate-pulse">
            <div className="font-bold">MULE-HUB</div>
            <div className="text-[10px] text-rose-200 font-bold">Risk: 940</div>
            <span className="absolute -top-1 -right-1 h-3 w-3 rounded-full bg-rose-500" />
          </div>
        </div>

        {/* Interactive Node 3: Beneficiary */}
        <div
          onClick={() => setSelectedNodeId('beneficiary-1')}
          className={`absolute left-[80%] top-[40%] cursor-pointer -translate-x-1/2 -translate-y-1/2 transition-transform duration-200 hover:scale-110 ${
            selectedNodeId === 'beneficiary-1' ? 'scale-110 z-20' : 'z-10'
          }`}
        >
          <div className="p-3 rounded-full bg-purple-500/20 border-2 border-purple-500 text-purple-300 text-center font-mono text-[10px] shadow-lg backdrop-blur-md">
            <div>MERCHANT</div>
            <div className="text-[9px] text-purple-200">Offshore</div>
          </div>
        </div>

        {/* Interactive Node 4: Shared Device */}
        <div
          onClick={() => setSelectedNodeId('device-1')}
          className={`absolute left-[50%] top-[75%] cursor-pointer -translate-x-1/2 -translate-y-1/2 transition-transform duration-200 hover:scale-110 ${
            selectedNodeId === 'device-1' ? 'scale-110 z-20' : 'z-10'
          }`}
        >
          <div className="p-2.5 rounded-full bg-amber-500/20 border-2 border-amber-500 text-amber-300 text-center font-mono text-[10px] shadow-lg backdrop-blur-md">
            <div>DEVICE ID</div>
            <div className="text-[9px] text-amber-200">14 Wallets</div>
          </div>
        </div>

        {/* Instructions pill */}
        <div className="absolute bottom-2 left-3 text-[10px] font-mono text-slate-500">
          Click any node to inspect entity forensics
        </div>
      </div>

      {/* Node Inspector Details Box */}
      <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="font-mono text-xs font-bold text-white">{selectedNode.name}</span>
            <span
              className={`text-[9px] font-mono px-2 py-0.5 rounded-full border ${
                selectedNode.status === 'CRITICAL'
                  ? 'bg-rose-500/20 text-rose-300 border-rose-500/40'
                  : selectedNode.status === 'BLOCKED'
                  ? 'bg-amber-500/20 text-amber-300 border-amber-500/40'
                  : 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40'
              }`}
            >
              {selectedNode.status}
            </span>
          </div>

          <div className="text-xs font-mono">
            <span className="text-slate-400">Risk Score: </span>
            <span className="font-bold text-rose-400">{selectedNode.riskScore} / 1000</span>
          </div>
        </div>

        <p className="text-xs text-slate-300 leading-relaxed">
          {selectedNode.details}
        </p>

        <div className="grid grid-cols-2 gap-3 pt-2 border-t border-slate-800 text-xs font-mono">
          <div>
            <span className="text-[10px] text-slate-500 block">Total Volume Exposed</span>
            <span className="font-bold text-white">{selectedNode.volume}</span>
          </div>
          <div>
            <span className="text-[10px] text-slate-500 block">Connected Inbound / Outbound</span>
            <span className="font-bold text-indigo-400">{selectedNode.transCount} Transactions</span>
          </div>
        </div>
      </div>
    </div>
  );
}
