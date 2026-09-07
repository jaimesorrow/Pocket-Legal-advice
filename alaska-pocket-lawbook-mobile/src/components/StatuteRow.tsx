import React from 'react';
import { AccessibilityRole, Pressable, StyleSheet, Text, View } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { LegalEntry } from '../data/types';
import { JurisdictionBadge } from './JurisdictionBadge';
import { getClassificationById } from '../data/legalData';

export function StatuteRow({
  entry,
  showClassification = false,
  onPress,
}: {
  entry: LegalEntry;
  showClassification?: boolean;
  onPress: () => void;
}) {
  const classification = getClassificationById(entry.classificationId);

  return (
    <Pressable
      onPress={onPress}
      accessibilityRole={'button' as AccessibilityRole}
      style={({ pressed }) => [styles.row, pressed && styles.rowPressed]}
    >
      <View style={styles.textWrap}>
        {showClassification ? (
          <View style={styles.badgeRow}>
            <JurisdictionBadge jurisdiction={entry.jurisdiction} />
            {classification ? <Text style={styles.classification}>{classification.name}</Text> : null}
          </View>
        ) : null}
        <Text style={styles.citation}>{entry.citation}</Text>
        <Text style={styles.title}>{entry.title}</Text>
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
    paddingVertical: 14,
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
    marginRight: 8,
  },
  badgeRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 6,
    gap: 8,
  },
  classification: {
    ...typography.caption,
    color: colors.onSurfaceVariant,
  },
  citation: {
    ...typography.label,
    color: colors.onSurfaceVariant,
    marginBottom: 2,
  },
  title: {
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
