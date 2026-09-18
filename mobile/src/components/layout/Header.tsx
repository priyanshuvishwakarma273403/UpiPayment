import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { ChevronLeft } from 'lucide-react-native';
import { useRouter } from 'expo-router';

interface HeaderProps {
  title: string;
  showBack?: boolean;
}

export function Header({ title, showBack = false }: HeaderProps) {
  const router = useRouter();

  return (
    <View className="h-14 flex-row items-center justify-between px-4 border-b border-border bg-background">
      {showBack ? (
        <TouchableOpacity onPress={() => router.back()} className="p-2">
          <ChevronLeft color="#ffffff" size={24} />
        </TouchableOpacity>
      ) : (
        <View className="w-8" />
      )}
      <Text className="text-white text-base font-bold font-mono">{title}</Text>
      <View className="w-8" />
    </View>
  );
}
