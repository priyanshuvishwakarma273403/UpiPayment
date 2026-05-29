import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { PieChart, Pie, Cell, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip } from 'recharts';
import { ArrowLeft, Sparkles, TrendingUp, TrendingDown, HelpCircle, ArrowUpRight } from 'lucide-react';
import toast from 'react-hot-toast';

import { useHaptic } from '../../hooks/useHaptic';
import TopBar from '../../components/layout/TopBar';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import { EXPENSE_CATEGORIES } from '../../utils/constants';
import { formatAmount } from '../../utils/formatAmount';

export const ExpenseScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  
  const [timeframe, setTimeframe] = useState('MONTH'); // 'WEEK', 'MONTH', 'YEAR'

  const handleAskAi = () => {
    triggerHaptic('light');
    toast.success('Loading chatbot parameters...');
    navigate('/ai/chat');
  };

  // Mock Bar Chart Data (Daily spend trend)
  const barData = [
    { day: '05/20', amount: 350 },
    { day: '05/21', amount: 720 },
    { day: '05/22', amount: 150 },
    { day: '05/23', amount: 0 },
    { day: '05/24', amount: 1200 },
    { day: '05/25', amount: 349 },
    { day: '05/26', amount: 450 },
    { day: '05/27', amount: 2500 },
    { day: '05/28', amount: 750 },
    { day: '05/29', amount: 100 }
  ];

  const totalSpent = EXPENSE_CATEGORIES.reduce((acc, c) => acc + (c.value * 150), 0); // e.g. ₹15,000 total

  return (
    <div className="flex-1 flex flex-col bg-[#0A0A0F] text-white select-none">
      {/* Top Header */}
      <TopBar title="AI Expense Insights" />

      <div className="flex-1 p-5 overflow-y-auto no-scrollbar flex flex-col gap-6">
        
        {/* Toggle selectors */}
        <div className="w-full flex bg-white/[0.03] p-1 rounded-2xl border border-white/[0.04]">
          {['WEEK', 'MONTH', 'YEAR'].map((tf) => (
            <button
              key={tf}
              onClick={() => { triggerHaptic('light'); setTimeframe(tf); }}
              className={`flex-1 py-2 text-[10px] font-bold tracking-widest rounded-xl transition-colors duration-150 uppercase ${
                timeframe === tf ? 'bg-primary text-white shadow-sm' : 'text-textSecondary hover:text-white'
              }`}
            >
              {tf}
            </button>
          ))}
        </div>

        {/* Donut Donut Donut Chart Frame */}
        <Card variant="glass" className="p-5 flex flex-col items-center">
          <h4 className="text-xs font-bold text-textSecondary uppercase tracking-widest text-left w-full mb-4">
            Category Breakdown
          </h4>
          
          <div className="w-full h-48 relative flex items-center justify-center">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={EXPENSE_CATEGORIES}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={75}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {EXPENSE_CATEGORIES.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} stroke="none" />
                  ))}
                </Pie>
              </PieChart>
            </ResponsiveContainer>
            
            {/* Center total overlay */}
            <div className="absolute flex flex-col items-center justify-center">
              <span className="text-[9px] font-bold text-textSecondary uppercase tracking-wider">Total Spent</span>
              <span className="text-sm font-extrabold amount-font mt-0.5">{formatAmount(totalSpent)}</span>
            </div>
          </div>

          {/* Color-coded Legend mapping */}
          <div className="grid grid-cols-2 gap-3 w-full mt-4 text-[10px] font-bold">
            {EXPENSE_CATEGORIES.map((cat, i) => (
              <div key={i} className="flex items-center gap-2 border border-white/[0.03] bg-white/[0.01] p-2 rounded-xl">
                <div className="w-2.5 h-2.5 rounded-full flex-shrink-0" style={{ backgroundColor: cat.color }} />
                <span className="text-textSecondary truncate max-w-[80px]">{cat.name}</span>
                <span className="text-white amount-font ml-auto">{cat.value}%</span>
              </div>
            ))}
          </div>
        </Card>

        {/* Interactive AI insights card */}
        <Card variant="glass" className="p-5 border border-primary/20 flex flex-col gap-3 bg-white/[0.01]">
          <div className="flex items-center gap-2 text-primary">
            <Sparkles className="w-4 h-4 fill-current animate-pulse" />
            <h4 className="text-xs font-bold uppercase tracking-widest">AI Expense Guard Advisor</h4>
          </div>
          <p className="text-xs text-textSecondary leading-relaxed text-left">
            Your spending increased by <span className="text-danger font-semibold">12% in Food & Dining</span> this week, largely driven by dining transactions at late hours. 
            However, your <span className="text-success font-semibold">Shopping categories decreased by 8%</span>, keeping your overall budget within safe parameters.
          </p>
        </Card>

        {/* Daily Spending Trend bar chart */}
        <Card variant="glass" className="p-5">
          <h4 className="text-xs font-bold text-textSecondary uppercase tracking-widest text-left mb-4">
            Daily Spend Trend
          </h4>
          <div className="w-full h-36 amount-font text-[10px] pr-4">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={barData}>
                <XAxis dataKey="day" stroke="rgba(255,255,255,0.2)" tickLine={false} />
                <YAxis stroke="rgba(255,255,255,0.2)" tickLine={false} />
                <Tooltip 
                  contentStyle={{ background: '#12121A', border: '1px solid rgba(255,255,255,0.08)', borderRadius: '12px', color: '#FFF' }}
                  cursor={{ fill: 'rgba(255,255,255,0.02)' }}
                />
                <Bar dataKey="amount" fill="#6C63FF" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Proceed Action chatbot connector */}
        <Button
          onClick={handleAskAi}
          variant="gradient"
          className="w-full py-4 text-sm font-bold flex items-center justify-center gap-2"
        >
          <HelpCircle className="w-4 h-4" />
          Ask AI assistant about expenses
        </Button>
      </div>
    </div>
  );
};

export default ExpenseScreen;
// 
