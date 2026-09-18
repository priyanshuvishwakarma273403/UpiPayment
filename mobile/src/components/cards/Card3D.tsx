import React from 'react';
import { View, Text } from 'react-native';

interface Card3DProps {
  cardHolder?: string;
  upiId?: string;
  cardNumber?: string;
}

export function Card3D({
  cardHolder = 'VIKRAM ADITYA',
  upiId = 'vikram@upimesh',
  cardNumber = '•••• •••• •••• 8842',
}: Card3DProps) {
  return (
    <View className="w-full aspect-[1.586/1] rounded-2xl bg-surface border border-border p-5 justify-between">
      <View className="flex-row justify-between items-center">
        <Text className="text-white text-xs font-mono font-bold tracking-widest">UPI MESH</Text>
        <Text className="text-indigo-400 text-xs font-mono">RuPay Platinum</Text>
      </View>

      <Text className="text-white text-lg font-mono tracking-widest">{cardNumber}</Text>

      <View className="flex-row justify-between items-end">
        <View>
          <Text className="text-slate-400 text-[10px] font-mono uppercase">Holder</Text>
          <Text className="text-white text-xs font-mono font-bold">{cardHolder}</Text>
        </View>
        <Text className="text-emerald-400 text-[10px] font-mono">RSA-2048 SEAL</Text>
      </View>
    </View>
  );
}
