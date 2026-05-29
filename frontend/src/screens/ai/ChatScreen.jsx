import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Send, Bot, Sparkles, HelpCircle, ArrowLeft } from 'lucide-react';
import toast from 'react-hot-toast';

import { useHaptic } from '../../hooks/useHaptic';
import ChatBubble from '../../components/ai/ChatBubble';
import TypingIndicator from '../../components/ai/TypingIndicator';
import TopBar from '../../components/layout/TopBar';
import Button from '../../components/ui/Button';

export const ChatScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  
  const [messages, setMessages] = useState([]);
  const [inputVal, setInputVal] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const scrollRef = useRef(null);

  // Suggested quick prompts chips
  const quickChips = [
    { label: "Show my spending this month", type: 'expenses' },
    { label: "Why was my payment blocked?", type: 'blocked' },
    { label: "How to add money to wallet?", type: 'add' }
  ];

  // Load welcome greeting
  useEffect(() => {
    setMessages([
      {
        id: 'msg_welcome',
        text: 'Hello! I am your UPI Mesh AI Assistant. 🤖\nHow can I help you manage your funds today?\n\nYou can ask me about expenses, transaction safety, or add money help.',
        isUser: false,
        timestamp: Date.now()
      }
    ]);
  }, []);

  // Auto-scroll chat body on new message
  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, isTyping]);

  const handleSendMessage = (text) => {
    if (!text.trim() || isTyping) return;
    
    triggerHaptic('light');

    // Add user message
    const userMessage = {
      id: `msg_u_${Date.now()}`,
      text: text,
      isUser: true,
      timestamp: Date.now()
    };
    
    setMessages(prev => [...prev, userMessage]);
    setInputVal('');
    setIsTyping(true);

    // AI thinking delay
    setTimeout(() => {
      let replyText = '';

      if (text.toLowerCase().includes('spending') || text.toLowerCase().includes('expense')) {
        replyText = "Here is a quick summary of your spending this month:\n\n🍔 Food & Dining: ₹4,620.00 (30%)\n🛍️ Shopping: ₹3,850.00 (25%)\n🚗 Travel: ₹3,080.00 (20%)\n🎬 Entertainment: ₹2,310.00 (15%)\n\nI can analyze this further on the Expense Insights page. Let me redirect you!";
        setTimeout(() => {
          triggerHaptic('medium');
          navigate('/ai/expenses');
        }, 3000);
      } else if (text.toLowerCase().includes('blocked') || text.toLowerCase().includes('fraud') || text.toLowerCase().includes('flagged')) {
        replyText = "Payments are usually blocked by the UPI Mesh Guard when the system detects unusual parameters (e.g. transfers to a completely new merchant at odd hours, or suspicious bank activity).\n\nIf you have a blocked payment, please check the Fraud Alert panel to review the security explanation and authorize it if you confirm it is safe.";
        setTimeout(() => {
          triggerHaptic('medium');
          navigate('/fraud-alert');
        }, 3500);
      } else if (text.toLowerCase().includes('add money') || text.toLowerCase().includes('wallet')) {
        replyText = "Adding money is quick and secure:\n1. Open your Home Screen.\n2. Tap 'Add Money' on the main balance card.\n3. Type your amount, select your bank, and proceed.\n\nLet me take you directly to the Add Money page!";
        setTimeout(() => {
          triggerHaptic('light');
          navigate('/wallet/add-money');
        }, 3000);
      } else {
        replyText = "I understand! UPI Mesh manages instant, secure payments with high-speed gateways. You can navigate the App using the tabs below, scan any QR code, or view transaction status logs in History.";
      }

      const aiMessage = {
        id: `msg_ai_${Date.now()}`,
        text: replyText,
        isUser: false,
        timestamp: Date.now()
      };

      setMessages(prev => [...prev, aiMessage]);
      setIsTyping(false);
      triggerHaptic('success');
    }, 1500);
  };

  return (
    <div className="flex-1 flex flex-col bg-[#0A0A0F] text-white h-full relative select-none">
      
      {/* Bot Specific Header */}
      <div className="h-16 px-4 bg-[#12121A]/90 backdrop-blur-md flex items-center justify-between border-b border-white/[0.05] sticky top-10 z-40">
        <div className="flex items-center gap-3">
          {/* Pulsing AI Logo Sphere */}
          <div className="relative w-10 h-10 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center">
            <Bot className="w-5 h-5 text-primary" />
            <span className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-success rounded-full border-2 border-[#12121A] animate-pulse" />
          </div>
          <div className="flex flex-col text-left">
            <span className="text-xs font-bold text-white tracking-wide">UPI Mesh Assistant</span>
            <span className="text-[9px] text-success font-semibold tracking-wider uppercase">Active Security Agent</span>
          </div>
        </div>
        
        {/* Navigation back */}
        <button
          onClick={() => { triggerHaptic('light'); navigate('/home'); }}
          className="p-1.5 rounded-xl bg-white/5 border border-white/5 text-textSecondary hover:text-white"
        >
          Home
        </button>
      </div>

      {/* Message Thread Body */}
      <div className="flex-1 overflow-y-auto no-scrollbar px-4 py-3 flex flex-col">
        {messages.map((message) => (
          <ChatBubble key={message.id} message={message} isUser={message.isUser} />
        ))}
        
        {isTyping && <TypingIndicator />}
        
        {/* Scroll Anchor */}
        <div ref={scrollRef} />
      </div>

      {/* Suggested Quick Prompt Chips Container */}
      <div className="px-4 py-2 border-t border-white/[0.03] bg-[#0A0A0F] flex flex-col gap-2 z-20">
        <div className="flex gap-2 overflow-x-auto no-scrollbar py-1">
          {quickChips.map((chip, i) => (
            <button
              key={i}
              onClick={() => handleSendMessage(chip.label)}
              className="px-3.5 py-2 rounded-xl bg-white/[0.02] border border-white/[0.04] text-[10px] font-bold text-secondary hover:bg-white/[0.04] hover:text-white active:scale-95 transition-all flex-shrink-0"
            >
              {chip.label}
            </button>
          ))}
        </div>

        {/* Input panel bar */}
        <div className="flex gap-2 items-center py-2 pb-safe-bottom">
          <input
            type="text"
            placeholder="Type your question..."
            value={inputVal}
            onChange={(e) => setInputVal(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter') handleSendMessage(inputVal); }}
            className="flex-1 py-3.5 px-4 rounded-2xl bg-white/[0.03] border border-white/[0.04] text-xs text-white placeholder-textSecondary outline-none focus:border-primary/50"
          />
          <button
            onClick={() => handleSendMessage(inputVal)}
            disabled={!inputVal.trim() || isTyping}
            className="w-12 h-12 rounded-2xl bg-btn-grad text-white flex items-center justify-center hover:brightness-110 active:scale-90 transition-all border border-white/10 shadow-glow-primary disabled:opacity-50 disabled:pointer-events-none"
            aria-label="Send message"
          >
            <Send className="w-4.5 h-4.5" />
          </button>
        </div>
      </div>
    </div>
  );
};

export default ChatScreen;
