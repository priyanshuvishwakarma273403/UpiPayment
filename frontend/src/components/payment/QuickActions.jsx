import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowUpRight, Scan, Download, Users, PhoneCall, MoreHorizontal, CreditCard, RefreshCw, Landmark, HelpCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import { useHaptic } from '../../hooks/useHaptic';
import BottomSheet from '../ui/BottomSheet';

export const QuickActions = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  const [isMoreOpen, setIsMoreOpen] = useState(false);

  const actions = [
    {
      id: 'send',
      name: 'Send Money',
      icon: ArrowUpRight,
      color: 'bg-primary/15 text-primary border-primary/20',
      action: () => navigate('/send'),
    },
    {
      id: 'scan',
      name: 'Scan QR',
      icon: Scan,
      color: 'bg-secondary/15 text-secondary border-secondary/20',
      action: () => navigate('/scan'),
    },
    {
      id: 'request',
      name: 'Request',
      icon: Download,
      color: 'bg-[#FFD93D]/15 text-[#FFD93D] border-[#FFD93D]/20',
      action: () => navigate('/request'),
    },
    {
      id: 'contacts',
      name: 'Pay Contacts',
      icon: Users,
      color: 'bg-success/15 text-success border-success/20',
      action: () => navigate('/send?tab=contacts'),
    },
    {
      id: 'recharge',
      name: 'Recharge',
      icon: PhoneCall,
      color: 'bg-orange-500/15 text-orange-400 border-orange-500/20',
      action: () => toast('Mobile Recharge is a placeholder module', { icon: '📱' }),
    },
    {
      id: 'more',
      name: 'More',
      icon: MoreHorizontal,
      color: 'bg-white/5 text-textSecondary border-white/10',
      action: () => setIsMoreOpen(true),
    },
  ];

  const handleAction = (item) => {
    triggerHaptic('light');
    item.action();
  };

  const moreServices = [
    { name: 'Credit Cards', icon: CreditCard, color: 'text-indigo-400', desc: 'Pay bills & view offers' },
    { name: 'Self Transfer', icon: RefreshCw, color: 'text-emerald-400', desc: 'Transfer between accounts' },
    { name: 'Bank Accounts', icon: Landmark, color: 'text-amber-400', desc: 'Check bank connections' },
    { name: 'Recharge & Bills', icon: PhoneCall, color: 'text-orange-400', desc: 'Electricity, Gas, Mobile' },
    { name: 'Help Support', icon: HelpCircle, color: 'text-cyan-400', desc: 'Raise transaction dispute' },
  ];

  return (
    <div className="w-full py-4 select-none">
      {/* Horizontal Rail */}
      <div className="flex gap-4 overflow-x-auto no-scrollbar px-1 py-1">
        {actions.map((act) => {
          const Icon = act.icon;
          return (
            <button
              key={act.id}
              type="button"
              onClick={() => handleAction(act)}
              className="flex flex-col items-center gap-2 flex-shrink-0 focus:outline-none group active:scale-95 transition-transform duration-100"
              style={{ width: '72px' }}
            >
              <div className={`w-12 h-12 rounded-2xl flex items-center justify-center border transition-all duration-200 group-hover:brightness-110 shadow-sm ${act.color}`}>
                <Icon className="w-5 h-5" />
              </div>
              <span className="text-[10px] font-bold text-center tracking-wide text-textSecondary group-hover:text-white line-clamp-1 truncate w-full">
                {act.name}
              </span>
            </button>
          );
        })}
      </div>

      {/* "More Actions" Drawer Bottom Sheet */}
      <BottomSheet isOpen={isMoreOpen} onClose={() => setIsMoreOpen(false)} title="Explore Payments & Services">
        <div className="grid grid-cols-1 gap-3.5 my-2">
          {moreServices.map((service, index) => {
            const Svg = service.icon;
            return (
              <div
                key={index}
                onClick={() => {
                  triggerHaptic('light');
                  toast.success(`${service.name} coming soon!`);
                  setIsMoreOpen(false);
                }}
                className="flex items-center gap-4 p-4 rounded-2xl bg-white/[0.03] active:bg-white/[0.08] border border-white/[0.04] transition-colors cursor-pointer"
              >
                <div className={`p-2.5 rounded-xl bg-white/[0.03] ${service.color}`}>
                  <Svg className="w-5 h-5" />
                </div>
                <div className="flex-1">
                  <h4 className="text-sm font-bold text-white tracking-wide">{service.name}</h4>
                  <p className="text-xs text-textSecondary mt-0.5">{service.desc}</p>
                </div>
              </div>
            );
          })}
        </div>
      </BottomSheet>
    </div>
  );
};

export default QuickActions;
