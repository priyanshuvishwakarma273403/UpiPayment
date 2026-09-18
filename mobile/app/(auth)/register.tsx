import React from 'react';
import { View, Text } from 'react-native';

export default function RegisterScreen() {
  return (
    <View className="flex-1 bg-background justify-center items-center px-6">
      <Text className="text-white text-2xl font-bold font-mono">Create Account</Text>
      <Text className="text-slate-400 text-sm mt-2">Claim your personalized @upimesh handle</Text>
    </View>
  );
}
