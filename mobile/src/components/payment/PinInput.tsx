import React from 'react';
import { View } from 'react-native';

interface PinInputProps {
  pin: string;
  length?: number;
}

export function PinInput({ pin, length = 6 }: PinInputProps) {
  return (
    <View className="flex-row justify-center items-center space-x-3 my-4">
      {Array.from({ length }).map((_, idx) => (
        <View
          key={idx}
          className={`h-4 w-4 rounded-full ${
            idx < pin.length ? 'bg-indigo-500 scale-110' : 'border border-slate-700 bg-surface'
          }`}
        />
      ))}
    </View>
  );
}
