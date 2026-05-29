import React from 'react';
import { useNavigate } from 'react-router-dom';
import { User, ShieldCheck, CreditCard, KeyRound, Smartphone, ToggleLeft, ToggleRight, Ban, MessageSquare, PieChart, Info, HelpCircle, LogOut, ChevronRight, Store } from 'lucide-react';
import toast from 'react-hot-toast';

import { useAuthStore } from '../../store/authStore';
import { useHaptic } from '../../hooks/useHaptic';

import TopBar from '../../components/layout/TopBar';
import Button from '../../components/ui/Button';
import Avatar from '../../components/ui/Avatar';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';

export const ProfileScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  const user = useAuthStore(state => state.user);
  const logout = useAuthStore(state => state.logout);
  const updateProfile = useAuthStore(state => state.updateProfile);

  const handleLogout = () => {
    triggerHaptic('heavy');
    logout();
    toast.success('Logged out successfully!');
    navigate('/auth/login');
  };

  const toggleMerchantRole = () => {
    triggerHaptic('medium');
    if (!user) return;
    
    const hasMerchant = user.roles?.includes('ROLE_MERCHANT');
    let nextRoles = [...user.roles];
    
    if (hasMerchant) {
      nextRoles = nextRoles.filter(r => r !== 'ROLE_MERCHANT');
      updateProfile({ roles: nextRoles });
      toast.success('Switched back to Customer profile');
      navigate('/home');
    } else {
      nextRoles.push('ROLE_MERCHANT');
      updateProfile({ roles: nextRoles });
      toast.success('Registered as Merchant successfully!');
      navigate('/merchant');
    }
  };

  const handleSectionClick = (action, label) => {
    triggerHaptic('light');
    if (action) {
      action();
    } else {
      toast(`${label} configuration loaded`, { icon: '⚙️' });
    }
  };

  const profileGroups = [
    {
      title: 'Account Settings',
      items: [
        { label: 'KYC Verification', icon: ShieldCheck, value: 'VERIFIED', action: () => toast.success('KYC is fully verified!') },
        { label: 'Bank Accounts', icon: CreditCard, action: () => toast('HDFC and SBI links active', { icon: '🏛️' }) }
      ]
    },
    {
      title: 'Security',
      items: [
        { label: 'Change MPIN', icon: KeyRound },
        { label: 'Linked Devices', icon: Smartphone, value: '1 Device' }
      ]
    },
    {
      title: 'Payments',
      items: [
        { label: 'Payment Limits', icon: KeyRound, value: '₹1,00,000/day' },
        { label: 'Blocked Contacts', icon: Ban }
      ]
    },
    {
      title: 'AI Insights',
      items: [
        { label: 'AI Chat History', icon: MessageSquare, action: () => navigate('/ai/chat') },
        { label: 'Expense Statements', icon: PieChart, action: () => navigate('/ai/expenses') }
      ]
    },
    {
      title: 'Support',
      items: [
        { label: 'Disputes & Support', icon: HelpCircle },
        { label: 'About UPI Mesh', icon: Info, value: 'v1.4.2' }
      ]
    }
  ];

  const isMerchant = user?.roles?.includes('ROLE_MERCHANT');

  return (
    <div className="flex-1 flex flex-col bg-[#0A0A0F] text-white select-none">
      <TopBar title="My Profile" showBack={false} />

      <div className="flex-1 p-5 overflow-y-auto no-scrollbar flex flex-col gap-6">
        
        {/* Core Card Info header */}
        <div className="flex flex-col items-center justify-center text-center mt-2">
          <Avatar name={user?.name || 'User'} size="lg" className="shadow-glow-primary" />
          
          <div className="flex items-center gap-1.5 mt-3">
            <span className="text-base font-extrabold tracking-wide text-white">{user?.name || 'User'}</span>
            <span className="bg-[#00E676]/10 text-[#00E676] text-[8px] font-extrabold tracking-widest border border-[#00E676]/20 px-2 py-0.5 rounded-md uppercase">
              Verified
            </span>
          </div>

          <span className="text-[10px] text-textSecondary font-semibold amount-font mt-1.5">{user?.upiId}</span>
          <span className="text-[10px] text-textSecondary/70 font-medium tracking-wide mt-0.5">{user?.email} | +91 {user?.phone}</span>
        </div>

        {/* Merchant toggle portal */}
        <Card
          variant="glass"
          onClick={toggleMerchantRole}
          className="p-4 flex items-center justify-between border border-primary/20 bg-[#12121A]/50 cursor-pointer hover:bg-white/[0.04]"
          animate={false}
        >
          <div className="flex items-center gap-3 text-left">
            <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center text-primary">
              <Store className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs font-bold text-white tracking-wide">
                {isMerchant ? 'Merchant mode enabled' : 'Become a Merchant'}
              </h4>
              <p className="text-[10px] text-textSecondary mt-0.5">
                {isMerchant ? 'Tap to switch back to customer view' : 'Tap to register shop and accept payments'}
              </p>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-textSecondary" />
        </Card>

        {isMerchant && (
          <Button
            onClick={() => { triggerHaptic('light'); navigate('/merchant'); }}
            variant="gradient"
            className="w-full py-3.5 text-xs font-bold flex items-center justify-center gap-2"
          >
            <Store className="w-4 h-4" />
            Open Merchant Dashboard
          </Button>
        )}

        {/* Section Groups */}
        <div className="flex flex-col gap-5">
          {profileGroups.map((group, groupIdx) => (
            <div key={groupIdx} className="flex flex-col gap-2.5">
              <h4 className="text-[9px] font-bold text-textSecondary uppercase tracking-widest text-left pl-1">
                {group.title}
              </h4>
              
              <div className="flex flex-col gap-2">
                {group.items.map((item, itemIdx) => {
                  const Svg = item.icon;
                  return (
                    <div
                      key={itemIdx}
                      onClick={() => handleSectionClick(item.action, item.label)}
                      className="p-3.5 rounded-2xl bg-white/[0.01] hover:bg-white/[0.03] active:bg-white/[0.05] border border-white/[0.04] flex items-center justify-between cursor-pointer transition-colors"
                    >
                      <div className="flex items-center gap-3.5 text-left">
                        <div className="p-2 bg-white/5 rounded-xl border border-white/5 text-textSecondary">
                          <Svg className="w-4 h-4" />
                        </div>
                        <span className="text-xs font-bold text-white tracking-wide">{item.label}</span>
                      </div>
                      
                      <div className="flex items-center gap-1.5 text-xs">
                        {item.value && (
                          <span className="text-[10px] font-bold text-textSecondary bg-white/5 border border-white/5 px-2.5 py-0.5 rounded-md uppercase amount-font">
                            {item.value}
                          </span>
                        )}
                        <ChevronRight className="w-4 h-4 text-textSecondary" />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          ))}
        </div>

        {/* Logout button */}
        <button
          onClick={handleLogout}
          className="w-full py-4 rounded-2xl bg-red-500/10 border border-red-500/20 text-xs font-extrabold text-danger hover:bg-red-500/20 active:scale-[0.98] transition-all flex items-center justify-center gap-2 mb-6"
        >
          <LogOut className="w-4 h-4" />
          Logout from UPI Mesh
        </button>
      </div>
    </div>
  );
};

export default ProfileScreen;
