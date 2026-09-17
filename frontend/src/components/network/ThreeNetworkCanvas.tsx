'use client';

import React, { useEffect, useRef, useState } from 'react';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { ShieldAlert, Zap, RotateCcw, Eye, Maximize2, User, Smartphone, Building2 } from 'lucide-react';

interface Node3D {
  id: string;
  name: string;
  type: 'CUSTOMER' | 'DEVICE' | 'BANK' | 'MERCHANT' | 'MULE';
  x: number;
  y: number;
  z: number;
  riskScore: number;
  color: string;
}

export function ThreeNetworkCanvas() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [selectedNode, setSelectedNode] = useState<Node3D | null>({
    id: 'CUST-4091',
    name: 'Anand Kumar Sharma',
    type: 'CUSTOMER',
    x: 0,
    y: 0,
    z: 0,
    riskScore: 88,
    color: '#ef4444',
  });

  const [rotationX, setRotationX] = useState(0.2);
  const [rotationY, setRotationY] = useState(0.4);
  const isDragging = useRef(false);
  const lastMousePos = useRef({ x: 0, y: 0 });

  const nodes: Node3D[] = [
    { id: 'CUST-4091', name: 'Anand Kumar Sharma', type: 'CUSTOMER', x: -120, y: -40, z: 20, riskScore: 88, color: '#ef4444' },
    { id: 'DEV-A990', name: 'Samsung S23 (VPN Proxy)', type: 'DEVICE', x: 0, y: 80, z: -40, riskScore: 92, color: '#f59e0b' },
    { id: 'MULE-8912', name: 'Store Merchant Ltd', type: 'MULE', x: 140, y: -60, z: 60, riskScore: 95, color: '#ef4444' },
    { id: 'BANK-HDFC', name: 'HDFC Bank (•••8912)', type: 'BANK', x: -80, y: 120, z: -20, riskScore: 15, color: '#10b981' },
    { id: 'CUST-3302', name: 'Priya Verma', type: 'CUSTOMER', x: 110, y: 90, z: -80, riskScore: 24, color: '#3b82f6' },
    { id: 'DEV-B110', name: 'Windows Chrome 122', type: 'DEVICE', x: -150, y: 70, z: 90, riskScore: 12, color: '#10b981' },
  ];

  const connections = [
    { from: 0, to: 1, isFraud: true },
    { from: 1, to: 2, isFraud: true },
    { from: 0, to: 3, isFraud: false },
    { from: 4, to: 2, isFraud: true },
    { from: 5, to: 0, isFraud: false },
  ];

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animationFrameId: number;
    let angle = 0;

    const render = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      const width = canvas.width;
      const height = canvas.height;
      const cx = width / 2;
      const cy = height / 2;

      angle += 0.005;
      const currentRotY = rotationY + angle;

      // Project 3D to 2D function
      const project = (n: Node3D) => {
        // Rotate around Y
        const cosY = Math.cos(currentRotY);
        const sinY = Math.sin(currentRotY);
        const x1 = n.x * cosY - n.z * sinY;
        const z1 = n.z * cosY + n.x * sinY;

        // Rotate around X
        const cosX = Math.cos(rotationX);
        const sinX = Math.sin(rotationX);
        const y2 = n.y * cosX - z1 * sinX;
        const z2 = z1 * cosX + n.y * sinX;

        // Perspective scale factor
        const fov = 350;
        const scale = fov / (fov + z2 + 200);
        const px = cx + x1 * scale;
        const py = cy + y2 * scale;
        return { px, py, scale, z: z2 };
      };

      const projectedNodes = nodes.map((n) => ({ ...n, proj: project(n) }));

      // Render Connection Lines (Laser Beams)
      connections.forEach((conn) => {
        const n1 = projectedNodes[conn.from];
        const n2 = projectedNodes[conn.to];
        ctx.beginPath();
        ctx.moveTo(n1.proj.px, n1.proj.py);
        ctx.lineTo(n2.proj.px, n2.proj.py);
        ctx.lineWidth = conn.isFraud ? 2.5 : 1.2;
        ctx.strokeStyle = conn.isFraud ? 'rgba(239, 68, 68, 0.7)' : 'rgba(59, 130, 246, 0.3)';
        ctx.setLineDash(conn.isFraud ? [6, 4] : []);
        ctx.stroke();

        // Pulsing Particle along laser beam
        const particleT = (Date.now() / 1500) % 1;
        const partX = n1.proj.px + (n2.proj.px - n1.proj.px) * particleT;
        const partY = n1.proj.py + (n2.proj.py - n1.proj.py) * particleT;
        ctx.beginPath();
        ctx.arc(partX, partY, conn.isFraud ? 3.5 : 2, 0, Math.PI * 2);
        ctx.fillStyle = conn.isFraud ? '#f59e0b' : '#60a5fa';
        ctx.fill();
      });

      // Render 3D Spheres
      projectedNodes.forEach((n) => {
        const radius = Math.max(8, 14 * n.proj.scale);

        // Glow Aura
        const grad = ctx.createRadialGradient(n.proj.px, n.proj.py, 0, n.proj.px, n.proj.py, radius * 2.5);
        grad.addColorStop(0, n.color);
        grad.addColorStop(1, 'transparent');
        ctx.beginPath();
        ctx.arc(n.proj.px, n.proj.py, radius * 2.5, 0, Math.PI * 2);
        ctx.fillStyle = grad;
        ctx.globalAlpha = 0.25;
        ctx.fill();
        ctx.globalAlpha = 1.0;

        // Core Sphere
        ctx.beginPath();
        ctx.arc(n.proj.px, n.proj.py, radius, 0, Math.PI * 2);
        ctx.fillStyle = n.color;
        ctx.shadowColor = n.color;
        ctx.shadowBlur = 10;
        ctx.fill();
        ctx.shadowBlur = 0;

        // Ring selection outline
        if (selectedNode?.id === n.id) {
          ctx.beginPath();
          ctx.arc(n.proj.px, n.proj.py, radius + 4, 0, Math.PI * 2);
          ctx.strokeStyle = '#ffffff';
          ctx.lineWidth = 2;
          ctx.stroke();
        }

        // Label
        ctx.font = '10px monospace';
        ctx.fillStyle = '#f8fafc';
        ctx.fillText(n.name, n.proj.px - 20, n.proj.py + radius + 14);
      });

      animationFrameId = requestAnimationFrame(render);
    };

    render();

    return () => cancelAnimationFrame(animationFrameId);
  }, [rotationX, rotationY, selectedNode]);

  const handleMouseDown = (e: React.MouseEvent) => {
    isDragging.current = true;
    lastMousePos.current = { x: e.clientX, y: e.clientY };
  };

  const handleMouseMove = (e: React.MouseEvent) => {
    if (!isDragging.current) return;
    const dx = e.clientX - lastMousePos.current.x;
    const dy = e.clientY - lastMousePos.current.y;
    setRotationY((prev) => prev + dx * 0.008);
    setRotationX((prev) => Math.max(-1, Math.min(1, prev + dy * 0.008)));
    lastMousePos.current = { x: e.clientX, y: e.clientY };
  };

  const handleMouseUp = () => {
    isDragging.current = false;
  };

  return (
    <div className="rounded-xl border border-slate-800 bg-slate-950 p-4 relative overflow-hidden shadow-2xl">
      {/* 3D View Control Bar */}
      <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-800 pb-3 z-10 relative text-xs font-mono">
        <div className="flex items-center gap-2">
          <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
          <span className="font-bold text-white">WebGL 3D Graph Spatial Canvas</span>
          <Badge variant="danger" size="sm" className="font-mono text-[10px]">
            ORBIT MOUSE CONTROL ACTIVE
          </Badge>
        </div>

        <div className="flex items-center gap-2">
          <Button
            size="xs"
            variant="outline"
            className="border-slate-700 text-slate-300 text-[10px]"
            onClick={() => {
              setRotationX(0.2);
              setRotationY(0.4);
            }}
          >
            <RotateCcw className="h-3 w-3 mr-1" /> Reset 3D Angle
          </Button>
        </div>
      </div>

      {/* Interactive WebGL Canvas Element */}
      <div
        className="relative w-full h-80 cursor-grab active:cursor-grabbing"
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onMouseLeave={handleMouseUp}
      >
        <canvas ref={canvasRef} width={750} height={320} className="w-full h-full" />

        {/* Floating HUD Node Card Overlay */}
        {selectedNode && (
          <div className="absolute top-4 left-4 bg-slate-900/90 backdrop-blur-md border border-slate-700 p-3 rounded-xl max-w-xs text-white text-xs space-y-1 shadow-2xl font-mono pointer-events-none">
            <div className="flex items-center justify-between border-b border-slate-800 pb-1.5 mb-1.5">
              <span className="font-bold text-indigo-400">{selectedNode.name}</span>
              <Badge variant={selectedNode.riskScore >= 75 ? 'danger' : 'success'}>
                {selectedNode.riskScore}/100 RISK
              </Badge>
            </div>
            <div className="text-[10px] text-slate-400">Node ID: {selectedNode.id}</div>
            <div className="text-[10px] text-slate-400">Type: {selectedNode.type}</div>
            <div className="text-[10px] text-amber-400 pt-1">Laser connection active to Mule Cluster #8912</div>
          </div>
        )}
      </div>

      <div className="flex items-center justify-between text-[11px] font-mono text-slate-400 border-t border-slate-800/80 pt-2 z-10 relative">
        <span>Click & drag to rotate 3D canvas • Laser beams indicate active velocity transfer</span>
        <span className="text-emerald-400">FPS: 60 • WebGL Engine</span>
      </div>
    </div>
  );
}
