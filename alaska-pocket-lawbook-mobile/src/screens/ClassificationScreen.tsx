import React, { useLayoutEffect } from 'react';
import { FlatList, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '../navigation/types';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { StatuteRow } from '../components/StatuteRow';
import { JurisdictionBadge } from '../components/JurisdictionBadge';
import { getClassificationById, getEntriesByClassification } from '../data/legalData';

type Props = NativeStackScreenProps<RootStackParamList, 'Classification'>;

export function ClassificationScreen({ route, navigation }: Props) {
  const { classificationId } = route.params;
  const classification = getClassificationById(classificationId);
  const entries = getEntriesByClassification(classificationId);

  useLayoutEffect(() => {
    navigation.setOptions({ title: classification?.name ?? 'Classification' });
  }, [navigation, classification]);

  if (!classification) {
    return (
      <SafeAreaView style={styles.safeArea}>
        <Text style={typography.body}>This classification could not be found.</Text>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.safeArea} edges={['left', 'right', 'bottom']}>
      <FlatList
        contentContainerStyle={styles.listContent}
        data={entries}
        keyExtractor={(item) => item.id}
        ListHeaderComponent={
          <View style={styles.header}>
            <JurisdictionBadge jurisdiction={classification.jurisdiction} />
            <Text style={[typography.screenTitle, styles.headerTitle]}>{classification.name}</Text>
          </View>
        }
        renderItem={({ item, index }) => (
          <StatuteRow
            entry={item}
            onPress={() =>
              navigation.navigate('StatuteDetail', {
                entryIds: entries.map((e) => e.id),
                index,
              })
            }
          />
        )}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: colors.ground,
  },
  listContent: {
    paddingHorizontal: 16,
    paddingBottom: 24,
  },
  header: {
    paddingTop: 12,
    paddingBottom: 8,
  },
  headerTitle: {
    marginTop: 8,
    color: colors.ink,
  },
});
