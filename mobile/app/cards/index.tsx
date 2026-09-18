import React from 'react';
import { View, Text } from 'react-native';

export default function VirtualCardsScreen() {
  return (
    <View className="flex-1 bg-background justify-center items-center px-6">
      <Text className="text-white text-xl font-bold font-mono">Virtual RuPay Cards</Text>
      <Text className="text-slate-400 text-xs mt-2 text-center">
        Manage 3D cards, contactless limits, and RSA-2048 non-repudiation keys
      </Text>
    </View>
  );
}
