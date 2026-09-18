import React from 'react';
import { View, Text } from 'react-native';

interface BadgeProps {
  label: string;
  variant?: 'safe' | 'warning' | 'critical' | 'info';
}

export function Badge({ label, variant = 'info' }: BadgeProps) {
  const variantStyles = {
    safe: 'bg-emerald-500/20 border-emerald-500/30 text-emerald-400',
    warning: 'bg-amber-500/20 border-amber-500/30 text-amber-400',
    critical: 'bg-rose-500/20 border-rose-500/30 text-rose-400',
    info: 'bg-indigo-500/20 border-indigo-500/30 text-indigo-400',
  };

  return (
    <View className={`px-2.5 py-1 rounded-full border self-start ${variantStyles[variant]}`}>
      <Text className="text-[10px] font-mono font-bold tracking-wide">{label}</Text>
    </View>
  );
}
