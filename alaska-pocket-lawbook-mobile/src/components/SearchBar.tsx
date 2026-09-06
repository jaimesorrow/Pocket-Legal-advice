import React from 'react';
import { StyleSheet, TextInput, View } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';

export function SearchBar({
  value,
  onChangeText,
  placeholder = 'Search statute number, citation, title, or keyword',
}: {
  value: string;
  onChangeText: (text: string) => void;
  placeholder?: string;
}) {
  return (
    <View style={styles.container}>
      <TextInput
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={colors.onSurfaceVariant}
        style={styles.input}
        autoCapitalize="none"
        autoCorrect={false}
        returnKeyType="search"
        accessibilityLabel="Search Alaska and federal law"
        accessibilityHint="Search by statute number, citation, law title, or plain-language keyword"
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.surface,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.outline,
    paddingHorizontal: 14,
    minHeight: 52,
    justifyContent: 'center',
  },
  input: {
    ...typography.body,
    color: colors.ink,
    minHeight: 44,
  },
});
