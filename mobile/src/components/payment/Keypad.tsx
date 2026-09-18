import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';

interface KeypadProps {
  onPressDigit: (digit: string) => void;
  onDelete: () => void;
}

export function Keypad({ onPressDigit, onDelete }: KeypadProps) {
  const keys = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '', '0', '⌫'];

  return (
    <View className="flex-row flex-wrap justify-between w-full max-w-[280px]">
      {keys.map((k, index) => (
        <TouchableOpacity
          key={index}
          onPress={() => (k === '⌫' ? onDelete() : k ? onPressDigit(k) : null)}
          disabled={!k}
          className="w-[30%] h-14 justify-center items-center my-1 rounded-xl bg-surface active:bg-slate-800"
        >
          <Text className="text-white text-xl font-bold font-mono">{k}</Text>
        </TouchableOpacity>
      ))}
    </View>
  );
}
