import React from 'react';
import { View, Text } from 'react-native';

export default function ProfileScreen() {
  return (
    <View className="flex-1 bg-background px-4 pt-12">
      <Text className="text-white text-xl font-bold font-mono">Profile & Security</Text>
      <Text className="text-slate-400 text-xs mt-1">KYC tier, linked bank accounts, and cryptographic keys</Text>
    </View>
  );
}
