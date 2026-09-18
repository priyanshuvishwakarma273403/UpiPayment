import React from 'react';
import { View, Text } from 'react-native';

export default function PaymentConfirmScreen() {
  return (
    <View className="flex-1 bg-background justify-center items-center px-6">
      <Text className="text-white text-xl font-bold font-mono">Authorize UPI Transfer</Text>
      <Text className="text-slate-400 text-xs mt-2">Enter 6-digit MPIN to complete payment</Text>
    </View>
  );
}
