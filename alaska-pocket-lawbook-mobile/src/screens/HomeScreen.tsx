import React, { useMemo, useState } from 'react';
import { FlatList, SectionList, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '../navigation/types';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { SearchBar } from '../components/SearchBar';
import { ClassificationRow } from '../components/ClassificationRow';
import { StatuteCard } from '../components/StatuteCard';
import { LegalInfoDisclaimer, NoVerifiedResultNotice } from '../components/Disclaimer';
import { getClassificationsByJurisdiction } from '../data/legalData';
import { searchLegalEntries } from '../utils/search';

type Props = NativeStackScreenProps<RootStackParamList, 'Home'>;

const alaskaClassifications = getClassificationsByJurisdiction('ALASKA');
const federalClassifications = getClassificationsByJurisdiction('FEDERAL');

export function HomeScreen({ navigation }: Props) {
  const [query, setQuery] = useState('');
  const trimmedQuery = query.trim();
  const results = useMemo(
    () => (trimmedQuery ? searchLegalEntries(trimmedQuery) : []),
    [trimmedQuery]
  );

  const sections = [
    { title: 'Alaska law', data: alaskaClassifications },
    { title: 'Federal law', data: federalClassifications },
  ];

  return (
    <SafeAreaView style={styles.safeArea} edges={['top', 'left', 'right']}>
      <View style={styles.header}>
        <Text style={typography.screenTitle}>Alaska's Pocket Lawbook</Text>
        <View style={styles.searchSpacing}>
          <SearchBar value={query} onChangeText={setQuery} />
        </View>
      </View>

      {trimmedQuery ? (
        <FlatList
          contentContainerStyle={styles.listContent}
          data={results}
          keyExtractor={(item) => item.id}
          renderItem={({ item }) => <StatuteCard entry={item} showClassification />}
          ListEmptyComponent={<NoVerifiedResultNotice />}
          keyboardShouldPersistTaps="handled"
        />
      ) : (
        <SectionList
          contentContainerStyle={styles.listContent}
          sections={sections}
          keyExtractor={(item) => item.id}
          renderSectionHeader={({ section }) => (
            <Text style={styles.sectionHeading}>{section.title}</Text>
          )}
          renderItem={({ item }) => (
            <ClassificationRow
              classification={item}
              onPress={() => navigation.navigate('Classification', { classificationId: item.id })}
            />
          )}
          ListFooterComponent={
            <View style={styles.footer}>
              <LegalInfoDisclaimer />
            </View>
          }
          stickySectionHeadersEnabled={false}
          keyboardShouldPersistTaps="handled"
        />
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: colors.ground,
  },
  header: {
    paddingHorizontal: 16,
    paddingTop: 8,
    paddingBottom: 12,
  },
  searchSpacing: {
    marginTop: 12,
  },
  listContent: {
    paddingHorizontal: 16,
    paddingBottom: 24,
  },
  sectionHeading: {
    ...typography.sectionHeading,
    color: colors.ink,
    marginTop: 16,
    marginBottom: 10,
  },
  footer: {
    marginTop: 8,
  },
});
