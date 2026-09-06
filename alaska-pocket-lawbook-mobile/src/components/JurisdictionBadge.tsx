import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { colors, jurisdictionColor } from '../theme/colors';
import { Jurisdiction } from '../data/types';

export function JurisdictionBadge({ jurisdiction }: { jurisdiction: Jurisdiction }) {
  const background = jurisdictionColor(jurisdiction);
  return (
    <View style={[styles.badge, { backgroundColor: background }]}>
      <Text style={styles.text}>{jurisdiction === 'FEDERAL' ? 'FEDERAL LAW' : 'ALASKA LAW'}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    alignSelf: 'flex-start',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 6,
  },
  text: {
    color: colors.white,
    fontSize: 12,
    fontWeight: '700',
    letterSpacing: 0.5,
  },
});
