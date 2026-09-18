import React from 'react';
import { View, Text } from 'react-native';

export default function PayScreen() {
  return (
    <View className="flex-1 bg-background px-4 pt-12">
      <Text className="text-white text-xl font-bold font-mono">Send Money</Text>
      <Text className="text-slate-400 text-xs mt-1">Instant transfer via UPI ID, Mobile, or Bank Account</Text>
    </View>
  );
}
