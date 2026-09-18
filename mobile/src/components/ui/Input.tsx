import React from 'react';
import { View, TextInput, Text, TextInputProps } from 'react-native';

interface InputProps extends TextInputProps {
  label?: string;
  error?: string;
}

export function Input({ label, error, className = '', ...props }: InputProps) {
  return (
    <View className="space-y-1.5 w-full">
      {label && <Text className="text-slate-400 text-xs font-mono uppercase">{label}</Text>}
      <TextInput
        placeholderTextColor="#64748b"
        className={`h-12 px-4 rounded-xl bg-surface border border-border text-white text-sm font-mono ${className}`}
        {...props}
      />
      {error && <Text className="text-risk-critical text-xs font-mono">{error}</Text>}
    </View>
  );
}
