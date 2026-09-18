import React from 'react';
import { View, ViewProps } from 'react-native';

export function Card({ children, className = '', ...props }: ViewProps) {
  return (
    <View className={`rounded-2xl bg-surface border border-border p-4 ${className}`} {...props}>
      {children}
    </View>
  );
}
