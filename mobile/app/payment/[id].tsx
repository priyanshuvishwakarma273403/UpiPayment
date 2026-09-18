import React from 'react';
import { View, Text } from 'react-native';
import { useLocalSearchParams } from 'expo-router';

export default function PaymentReceiptScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();

  return (
    <View className="flex-1 bg-background justify-center items-center px-6">
      <Text className="text-white text-xl font-bold font-mono">Payment Status</Text>
      <Text className="text-indigo-400 font-mono text-sm mt-2">Ref ID: {id || 'N/A'}</Text>
    </View>
  );
}
