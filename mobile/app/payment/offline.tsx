import React from 'react';
import { View, Text } from 'react-native';

export default function OfflineMeshPaymentScreen() {
  return (
    <View className="flex-1 bg-background justify-center items-center px-6">
      <Text className="text-white text-xl font-bold font-mono">Offline Mesh Payment</Text>
      <Text className="text-slate-400 text-xs mt-2 text-center">
        Sign transaction locally with RSA-2048 private key without internet connectivity
      </Text>
    </View>
  );
}
