import React from 'react';
import { TouchableOpacity, Text, ActivityIndicator, ViewStyle, TextStyle } from 'react-native';

interface ButtonProps {
  title: string;
  onPress: () => void;
  variant?: 'primary' | 'secondary' | 'outline' | 'danger';
  isLoading?: boolean;
  disabled?: boolean;
}

export function Button({ title, onPress, variant = 'primary', isLoading = false, disabled = false }: ButtonProps) {
  return (
    <TouchableOpacity
      onPress={onPress}
      disabled={disabled || isLoading}
      className={`h-12 rounded-xl flex-row items-center justify-center px-4 ${
        variant === 'primary' ? 'bg-primary' : 'bg-surface border border-border'
      } ${disabled ? 'opacity-50' : ''}`}
    >
      {isLoading ? (
        <ActivityIndicator color="#ffffff" />
      ) : (
        <Text className="text-white font-bold text-sm font-mono">{title}</Text>
      )}
    </TouchableOpacity>
  );
}
