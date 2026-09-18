import React from 'react';
import { View, Text } from 'react-native';

export default function OtpVerificationScreen() {
  return (
    <View className="flex-1 bg-background justify-center items-center px-6">
      <Text className="text-white text-2xl font-bold font-mono">Verify OTP</Text>
      <Text className="text-slate-400 text-sm mt-2">Enter the 6-digit code dispatched to your phone</Text>
    </View>
  );
}
