import React from 'react';
import { AccessibilityRole, Pressable, StyleSheet, Text, View } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { Classification } from '../data/types';

export function ClassificationRow({
  classification,
  onPress,
}: {
  classification: Classification;
  onPress: () => void;
}) {
  return (
    <Pressable
      onPress={onPress}
      accessibilityRole={'button' as AccessibilityRole}
      style={({ pressed }) => [styles.row, pressed && styles.rowPressed]}
    >
      <View style={styles.textWrap}>
        <Text style={styles.name}>{classification.name}</Text>
      </View>
      <Text style={styles.chevron}>›</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: colors.surface,
    borderRadius: 12,
    paddingVertical: 16,
    paddingHorizontal: 16,
    marginBottom: 10,
    minHeight: 56,
    borderWidth: 1,
    borderColor: colors.outline,
  },
  rowPressed: {
    backgroundColor: colors.surfaceVariant,
  },
  textWrap: {
    flex: 1,
  },
  name: {
    ...typography.body,
    fontWeight: '600',
    color: colors.ink,
  },
  chevron: {
    fontSize: 22,
    color: colors.onSurfaceVariant,
    marginLeft: 8,
  },
});
