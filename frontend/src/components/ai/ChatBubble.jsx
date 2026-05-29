import React from 'react';
import { motion } from 'framer-motion';

export const ChatBubble = ({ message, isUser = false }) => {
  return (
    <div className={`w-full flex ${isUser ? 'justify-end' : 'justify-start'} my-2.5`}>
      <motion.div
        initial={{ opacity: 0, y: 10, scale: 0.96 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        transition={{ type: 'spring', stiffness: 300, damping: 25 }}
        className={`max-w-[80%] p-3.5 rounded-2xl text-xs font-medium leading-relaxed ${
          isUser
            ? 'bg-btn-grad text-white rounded-br-xs border border-white/10 shadow-glow-primary'
            : 'bg-white/[0.04] backdrop-blur-md text-white rounded-bl-xs border border-white/[0.06] shadow-glass'
        }`}
      >
        <div>
          {/* Support line breaks in messages */}
          {message.text.split('\n').map((line, idx) => (
            <p key={idx} className={idx > 0 ? 'mt-1' : ''}>
              {line}
            </p>
          ))}
        </div>
        
        <span className={`block text-[9px] opacity-40 text-right mt-1.5 amount-font`}>
          {new Date(message.timestamp || Date.now()).toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit'
          })}
        </span>
      </motion.div>
    </div>
  );
};

export default ChatBubble;
