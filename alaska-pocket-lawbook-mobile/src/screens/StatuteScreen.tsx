import React, { useLayoutEffect } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '../navigation/types';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { StatuteCard } from '../components/StatuteCard';
import { LEGAL_ENTRIES } from '../data/legalData';

type Props = NativeStackScreenProps<RootStackParamList, 'StatuteDetail'>;

export function StatuteScreen({ route, navigation }: Props) {
  const { entryIds, index } = route.params;
  const entryId = entryIds[index];
  const entry = LEGAL_ENTRIES.find((e) => e.id === entryId);

  const hasPrevious = index > 0;
  const hasNext = index < entryIds.length - 1;

  useLayoutEffect(() => {
    navigation.setOptions({ title: entry?.citation ?? 'Statute' });
  }, [navigation, entry]);

  if (!entry) {
    return (
      <SafeAreaView style={styles.safeArea}>
        <Text style={typography.body}>This entry could not be found.</Text>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.safeArea} edges={['left', 'right', 'bottom']}>
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <StatuteCard entry={entry} showClassification />
      </ScrollView>

      <View style={styles.footer}>
        <Pressable
          accessibilityRole="button"
          accessibilityLabel="Previous statute"
          disabled={!hasPrevious}
          onPress={() => navigation.setParams({ index: index - 1 })}
          style={[styles.navButton, styles.navButtonLeft, !hasPrevious && styles.navButtonDisabled]}
        >
          <Text style={[styles.navButtonText, !hasPrevious && styles.navButtonTextDisabled]}>
            ‹ Previous
          </Text>
        </Pressable>
        <Pressable
          accessibilityRole="button"
          accessibilityLabel="Next statute"
          disabled={!hasNext}
          onPress={() => navigation.setParams({ index: index + 1 })}
          style={[styles.navButton, styles.navButtonRight, !hasNext && styles.navButtonDisabled]}
        >
          <Text style={[styles.navButtonText, !hasNext && styles.navButtonTextDisabled]}>
            Next ›
          </Text>
        </Pressable>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: colors.ground,
  },
  scrollContent: {
    padding: 16,
    paddingBottom: 8,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderTopWidth: 1,
    borderTopColor: colors.outline,
    backgroundColor: colors.surface,
  },
  navButton: {
    flex: 1,
    minHeight: 48,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.flagBlue,
  },
  navButtonLeft: {
    marginRight: 8,
    alignItems: 'flex-start',
    paddingLeft: 16,
  },
  navButtonRight: {
    marginLeft: 8,
    alignItems: 'flex-end',
    paddingRight: 16,
  },
  navButtonDisabled: {
    backgroundColor: colors.surfaceVariant,
  },
  navButtonText: {
    ...typography.button,
    color: colors.white,
  },
  navButtonTextDisabled: {
    color: colors.onSurfaceVariant,
  },
});
