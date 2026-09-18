'use client';

import React, { useState, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  ShieldCheck,
  Zap,
  Lock,
  Cpu,
  Layers,
  Activity,
  Bot,
  CheckCircle2,
  Terminal,
  ArrowRight,
  Database,
  Radio,
  Server
} from 'lucide-react';

interface Stage {
  id: number;
  title: string;
  badge: string;
  badgeColor: string;
  description: string;
  metrics: { label: string; value: string }[];
  codeTitle: string;
  codeSnippet: string;
  icon: React.ComponentType<{ className?: string }>;
}

export function StickyScrollReveal() {
  const [activeStageIndex, setActiveStageIndex] = useState(0);

  const stages: Stage[] = [
    {
      id: 1,
      title: '1. Client RSA-2048 Digital Signing',
      badge: 'Client WebCrypto',
      badgeColor: 'bg-indigo-500/20 text-indigo-400 border-indigo-500/30',
      description:
        'Every transaction payload is digitally signed at the client edge using native browser WebCrypto RSA-2048 private keys, ensuring cryptographic non-repudiation and zero replay capability.',
      metrics: [
        { label: 'Algorithm', value: 'SHA256withRSA' },
        { label: 'Key Size', value: '2048-bit' },
        { label: 'Nonce Entropy', value: '128-bit CSPRNG' },
      ],
      codeTitle: 'src/lib/crypto/rsaSigner.ts',
      codeSnippet: `// Canonical transaction payload signing
const message = \`\${sender}|\${receiver}|\${amount}|\${timestamp}|\${nonce}\`;
const signature = await window.crypto.subtle.sign(
  { name: "RSA-PSS", saltLength: 32 },
  privateKey,
  new TextEncoder().encode(message)
);
headers["X-Signature"] = arrayBufferToBase64(signature);`,
      icon: Lock,
    },
    {
      id: 2,
      title: '2. Gateway Edge Perimeter & Token-Bucket',
      badge: 'Port 8080',
      badgeColor: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
      description:
        'Spring Cloud API Gateway intercepts request, applies reactive Redis token-bucket rate limiting (20 req/s per IP), validates JWT Bearer signatures, sanitizes client headers, and forwards verified identity downstream.',
      metrics: [
        { label: 'Throughput', value: '25,000+ TPS' },
        { label: 'Rate Limiter', value: 'Redis Token Bucket' },
        { label: 'Perimeter Filter', value: 'Zero-Trust Sanitizer' },
      ],
      codeTitle: 'Gateway/JwtAuthenticationFilter.java',
      codeSnippet: `// Strip external spoofed identity & inject trusted claims
ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
  .headers(httpHeaders -> httpHeaders.remove("X-User-Id"))
  .header("X-User-Id", claims.getSubject())
  .header("X-User-Roles", claims.get("roles", String.class))
  .header("X-Gateway-Source", HMAC_GATEWAY_ASSERTION)
  .build();`,
      icon: Server,
    },
    {
      id: 3,
      title: '3. Sub-10ms Behavioral Risk Scoring',
      badge: 'Port 8096',
      badgeColor: 'bg-amber-500/20 text-amber-400 border-amber-500/30',
      description:
        'Evaluates 9 synchronous behavioral rule strategies: velocity spikes, geofencing distance, device fingerprint mismatch, and night-time anomalous hours to compute composite risk index (0-1000).',
      metrics: [
        { label: 'Latency', value: '< 8.4 ms' },
        { label: 'Rule Strategies', value: '9 Modular Evaluators' },
        { label: 'Threshold Safe', value: '< 300 / 1000' },
      ],
      codeTitle: 'Risk/CompositeRiskScoringEngine.java',
      codeSnippet: `// Multi-signal synchronous composite evaluation
double velocityPenalty = evaluateVelocityWindow(userId, amount);
double geoDeviation = calculateHaversineDelta(lastCoords, currentCoords);
double deviceTrust = verifyDeviceFingerprint(deviceId);

int compositeScore = Math.min(1000, 
  (int)(velocityPenalty * 0.4 + geoDeviation * 0.35 + deviceTrust * 0.25)
);`,
      icon: Zap,
    },
    {
      id: 4,
      title: '4. Kafka Event Decoupling & Ledger Stream',
      badge: 'Port 9092',
      badgeColor: 'bg-purple-500/20 text-purple-400 border-purple-500/30',
      description:
        'The Payment service publishes `payment_initiated` to Apache Kafka. Downstream consumers (Wallet, Transaction, Audit, Notification) process the lifecycle asynchronously with eventual consistency guarantees.',
      metrics: [
        { label: 'Topic', value: 'payment_initiated' },
        { label: 'Partitions', value: '12 Partition Bus' },
        { label: 'Replication', value: 'Factor 3 In-Sync' },
      ],
      codeTitle: 'Payment/PaymentEventProducer.java',
      codeSnippet: `// Asynchronous event streaming to Kafka broker
PaymentInitiatedEvent event = new PaymentInitiatedEvent(
  payment.getId(), senderUpi, receiverUpi, amount, Instant.now()
);
kafkaTemplate.send("payment_initiated", payment.getId(), event)
  .whenComplete((result, ex) -> {
    if (ex == null) log.info("Event published successfully to partition {}", result.getRecordMetadata().partition());
  });`,
      icon: Radio,
    },
    {
      id: 5,
      title: '5. Atomic Dual-Ledger Balance Mutation',
      badge: 'Port 8082',
      badgeColor: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
      description:
        'Wallet Service acquires atomic row locks in MySQL (`SELECT FOR UPDATE`), validates sufficient funds, performs debit of sender and credit of receiver, and writes immutable audit doc to MongoDB.',
      metrics: [
        { label: 'Isolation', value: 'SERIALIZABLE / PESSIMISTIC' },
        { label: 'Relational DB', value: 'MySQL 8.0 InnoDB' },
        { label: 'Audit Store', value: 'MongoDB Append-Only' },
      ],
      codeTitle: 'Wallet/WalletTransactionService.java',
      codeSnippet: `@Transactional(isolation = Isolation.READ_COMMITTED)
public void executeAtomicDebitCredit(String senderId, String receiverId, BigDecimal amt) {
  Wallet sender = walletRepo.findByUserIdForUpdate(senderId)
    .orElseThrow(() -> new InsufficientBalanceException());
  sender.debit(amt);
  Wallet receiver = walletRepo.findByUserIdForUpdate(receiverId);
  receiver.credit(amt);
}`,
      icon: Database,
    },
    {
      id: 6,
      title: '6. Python ML Real-Time Inference',
      badge: 'Port 8000',
      badgeColor: 'bg-cyan-500/20 text-cyan-400 border-cyan-500/30',
      description:
        'FastAPI ML service evaluates an 8-dimensional normalized feature vector against an active Random Forest & XGBoost production model trained on millions of historical UPI transaction vectors.',
      metrics: [
        { label: 'Framework', value: 'FastAPI + Scikit-Learn' },
        { label: 'PR-AUC', value: '0.9942' },
        { label: 'Model Registry', value: 'MLflow Integrated' },
      ],
      codeTitle: 'MlService/main.py',
      codeSnippet: `@app.post("/predict")
async def predict_fraud(features: TransactionFeatureVector):
    X = np.array([[
        features.amount, features.velocity_1h, features.night_trans,
        features.distance_km, features.ratio_to_avg, features.fail_count
    ]])
    prob = float(active_model.predict_proba(X)[0][1])
    return {"fraud_probability": prob, "decision": "FLAG" if prob > 0.65 else "PASS"}`,
      icon: Cpu,
    },
    {
      id: 7,
      title: '7. AI Investigator & SHAP Attribution',
      badge: 'Port 8087',
      badgeColor: 'bg-rose-500/20 text-rose-400 border-rose-500/30',
      description:
        'Spring AI with OpenAI ChatClient translates complex model features into plain English explanations and generates SHAP waterfall charts for human compliance officers reviewing high-risk alerts.',
      metrics: [
        { label: 'XAI Method', value: 'TreeSHAP Waterfall' },
        { label: 'Copilot', value: 'Spring AI ChatClient' },
        { label: 'Evidence Gen', value: 'Automated SAR Draft' },
      ],
      codeTitle: 'AiService/FraudExplainabilityService.java',
      codeSnippet: `public FraudExplanation explainTransactionRisk(Payment payment, RiskScore score) {
  String prompt = """
    Analyze transaction %s for user %s. Amount: ₹%s. 
    Flagged rules: Velocity > 5x, Foreign IP mismatch.
    Provide actionable risk justification for compliance officer.
    """.formatted(payment.getId(), payment.getSenderUpi(), payment.getAmount());
  return aiClient.prompt(prompt).call().entity(FraudExplanation.class);
}`,
      icon: Bot,
    },
    {
      id: 8,
      title: '8. NPCI Switch Clearing & 3-Way Recon',
      badge: 'Port 8090 & 8093',
      badgeColor: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
      description:
        'Inter-bank settlement clears via simulated NPCI ISO-8583 switch. Daily Quartz reconciliation compares internal ledgers against core bank feeds and NPCI reports, flagging discrepancies for instant resolution.',
      metrics: [
        { label: 'Standard', value: 'ISO-8583 Message Bus' },
        { label: 'SLA Match', value: '100% 3-Way Match' },
        { label: 'Export', value: 'Apache POI Excel Workbook' },
      ],
      codeTitle: 'Reconciliation/ThreeWayReconciler.java',
      codeSnippet: `// 3-Way ledger vs switch vs bank reconciliation
for (Transaction tx : internalLedger) {
  BankRecord bank = bankFeed.lookup(tx.getRrn());
  NpciRecord npci = npciSwitch.lookup(tx.getRrn());
  if (tx.matches(bank) && tx.matches(npci)) {
    reconciliationReport.markMatched(tx);
  } else {
    reconciliationReport.flagDiscrepancy(tx, bank, npci);
  }
}`,
      icon: CheckCircle2,
    },
  ];

  const activeStage = stages[activeStageIndex];
  const ActiveIcon = activeStage.icon;

  return (
    <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-20">
      {/* Section Header */}
      <div className="text-center max-w-3xl mx-auto mb-16">
        <span className="inline-flex items-center gap-1.5 rounded-full bg-indigo-500/10 px-3 py-1 text-xs font-mono font-semibold text-indigo-400 ring-1 ring-inset ring-indigo-500/25 mb-4">
          <Zap className="h-3.5 w-3.5" />
          END-TO-END TELEMETRY MESH
        </span>
        <h2 className="text-3xl sm:text-4xl lg:text-5xl font-extrabold tracking-tight text-white">
          The 8-Stage <span className="bg-gradient-to-r from-blue-400 via-indigo-300 to-purple-400 bg-clip-text text-transparent">Transaction Lifecycle</span>
        </h2>
        <p className="mt-4 text-sm sm:text-base text-slate-400 leading-relaxed">
          Scroll or select a stage to see how every UPI payment flows through our synchronous perimeter, Kafka distributed ledger, and AI risk engines.
        </p>
      </div>

      {/* Interactive Sticky Showcase Container */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left Column: Interactive Stage List (6 cols) */}
        <div className="lg:col-span-6 space-y-3">
          {stages.map((stage, idx) => {
            const isSelected = idx === activeStageIndex;
            const Icon = stage.icon;

            return (
              <motion.div
                key={stage.id}
                onClick={() => setActiveStageIndex(idx)}
                whileHover={{ scale: 1.01 }}
                className={`cursor-pointer rounded-2xl p-5 border transition-all duration-300 ${
                  isSelected
                    ? 'bg-slate-900/90 border-indigo-500/60 shadow-xl shadow-indigo-500/10 ring-1 ring-indigo-500/30'
                    : 'bg-slate-900/40 border-slate-800/80 hover:bg-slate-900/70 hover:border-slate-700'
                }`}
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div
                      className={`h-9 w-9 rounded-xl flex items-center justify-center transition-colors ${
                        isSelected
                          ? 'bg-indigo-600 text-white shadow-md'
                          : 'bg-slate-800 text-slate-400'
                      }`}
                    >
                      <Icon className="h-4 w-4" />
                    </div>
                    <div>
                      <h3
                        className={`text-sm font-bold tracking-wide transition-colors ${
                          isSelected ? 'text-white' : 'text-slate-300'
                        }`}
                      >
                        {stage.title}
                      </h3>
                    </div>
                  </div>

                  <span
                    className={`text-[10px] font-mono px-2 py-0.5 rounded-full border ${stage.badgeColor}`}
                  >
                    {stage.badge}
                  </span>
                </div>

                <p className="mt-3 text-xs text-slate-400 leading-relaxed">
                  {stage.description}
                </p>

                {isSelected && (
                  <motion.div
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    className="mt-4 pt-3 border-t border-slate-800/80 grid grid-cols-3 gap-2"
                  >
                    {stage.metrics.map((m) => (
                      <div key={m.label} className="bg-slate-950/60 p-2 rounded-lg border border-slate-800/60 text-center">
                        <div className="text-[9px] text-slate-500 uppercase font-mono">{m.label}</div>
                        <div className="text-[11px] font-bold font-mono text-indigo-300 mt-0.5">{m.value}</div>
                      </div>
                    ))}
                  </motion.div>
                )}
              </motion.div>
            );
          })}
        </div>

        {/* Right Column: Sticky Code & Architecture Terminal (6 cols) */}
        <div className="lg:col-span-6 sticky top-24">
          <AnimatePresence mode="wait">
            <motion.div
              key={activeStage.id}
              initial={{ opacity: 0, y: 15, scale: 0.98 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: -15, scale: 0.98 }}
              transition={{ duration: 0.2 }}
              className="rounded-2xl border border-slate-800 bg-slate-950 shadow-2xl p-6 relative overflow-hidden"
            >
              {/* Top Terminal Bar */}
              <div className="flex items-center justify-between pb-4 border-b border-slate-800/80">
                <div className="flex items-center gap-2">
                  <div className="h-3 w-3 rounded-full bg-rose-500/80" />
                  <div className="h-3 w-3 rounded-full bg-amber-500/80" />
                  <div className="h-3 w-3 rounded-full bg-emerald-500/80" />
                  <span className="ml-2 font-mono text-xs text-slate-400 flex items-center gap-1.5">
                    <Terminal className="h-3.5 w-3.5 text-indigo-400" />
                    {activeStage.codeTitle}
                  </span>
                </div>

                <div className="flex items-center gap-2">
                  <span className="flex h-2 w-2 relative">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
                    <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500" />
                  </span>
                  <span className="text-[10px] font-mono text-emerald-400">ACTIVE TELEMETRY</span>
                </div>
              </div>

              {/* Code Snippet Box */}
              <div className="mt-4 p-4 rounded-xl bg-slate-900/90 border border-slate-800 font-mono text-xs text-slate-300 overflow-x-auto leading-relaxed">
                <pre>
                  <code>{activeStage.codeSnippet}</code>
                </pre>
              </div>

              {/* Quick Summary Pill */}
              <div className="mt-5 p-4 rounded-xl bg-indigo-950/30 border border-indigo-500/20 flex items-center justify-between text-xs">
                <div className="flex items-center gap-3">
                  <div className="h-8 w-8 rounded-lg bg-indigo-500/20 text-indigo-300 flex items-center justify-center shrink-0">
                    <ActiveIcon className="h-4 w-4" />
                  </div>
                  <div>
                    <div className="font-bold text-white">{activeStage.title}</div>
                    <div className="text-[11px] text-slate-400">Step {activeStage.id} of 8 in transaction execution pipeline</div>
                  </div>
                </div>

                <button
                  onClick={() => setActiveStageIndex((prev) => (prev + 1) % stages.length)}
                  className="p-2 rounded-lg bg-indigo-600/80 hover:bg-indigo-600 text-white transition-colors"
                  aria-label="Next Step"
                >
                  <ArrowRight className="h-4 w-4" />
                </button>
              </div>
            </motion.div>
          </AnimatePresence>
        </div>
      </div>
    </div>
  );
}
