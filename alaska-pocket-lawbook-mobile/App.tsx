import { StatusBar } from 'expo-status-bar';
import React, { useMemo, useState } from 'react';
import {
  Linking,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import {
  ALASKA_CLASSIFICATIONS,
  CLASSIFICATIONS,
  FEDERAL_CLASSIFICATIONS,
  LEGAL_ENTRIES,
  NO_VERIFIED_MATCH_MESSAGE,
  entriesForClassification,
  searchLegalEntries,
} from './src/data';
import { colors, spacing } from './src/theme';
import { Jurisdiction, LegalEntry, Screen } from './src/types';

const LEGAL_DISCLAIMER =
  'Legal information only — not legal advice. Using this app does not create an attorney-client relationship.';

export default function App() {
  const [stack, setStack] = useState<Screen[]>([{ kind: 'HOME' }]);
  const current = stack[stack.length - 1] ?? { kind: 'HOME' as const };

  const push = (screen: Screen) => setStack((previous) => [...previous, screen]);
  const pop = () => setStack((previous) => (previous.length > 1 ? previous.slice(0, -1) : previous));
  const goHome = () => setStack([{ kind: 'HOME' }]);

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar style="dark" />
      <AppHeader canGoBack={stack.length > 1} onBack={pop} onHome={goHome} />
      <View style={styles.disclaimerBar}>
        <Text style={styles.disclaimerText}>{LEGAL_DISCLAIMER}</Text>
      </View>

      {current.kind === 'HOME' && (
        <HomeScreen
          onOpenClassification={(classificationId) =>
            push({ kind: 'CLASSIFICATION', classificationId })
          }
          onOpenEntry={(entryId) => push({ kind: 'ENTRY', entryId })}
        />
      )}

      {current.kind === 'CLASSIFICATION' && (
        <ClassificationScreen
          classificationId={current.classificationId}
          onOpenEntry={(entryId) => push({ kind: 'ENTRY', entryId })}
        />
      )}

      {current.kind === 'ENTRY' && <EntryScreen entryId={current.entryId} />}
    </SafeAreaView>
  );
}

function AppHeader({
  canGoBack,
  onBack,
  onHome,
}: {
  canGoBack: boolean;
  onBack: () => void;
  onHome: () => void;
}) {
  return (
    <View style={styles.header}>
      <View style={styles.headerSide}>
        {canGoBack ? (
          <Pressable
            accessibilityRole="button"
            accessibilityLabel="Go back"
            hitSlop={8}
            onPress={onBack}
            style={styles.headerButton}
          >
            <Text style={styles.headerButtonText}>‹ Back</Text>
          </Pressable>
        ) : null}
      </View>
      <Pressable accessibilityRole="button" onPress={onHome} style={styles.brandButton}>
        <Text style={styles.brand}>Alaska's Pocket Lawbook</Text>
      </Pressable>
      <View style={styles.headerSide} />
    </View>
  );
}

function HomeScreen({
  onOpenClassification,
  onOpenEntry,
}: {
  onOpenClassification: (classificationId: string) => void;
  onOpenEntry: (entryId: string) => void;
}) {
  const [query, setQuery] = useState('');
  const results = useMemo(() => searchLegalEntries(query), [query]);
  const searching = query.trim().length > 0;

  return (
    <ScrollView
      contentContainerStyle={styles.page}
      keyboardShouldPersistTaps="handled"
      accessibilityLabel="Legal information library"
    >
      <Text style={styles.eyebrow}>ALASKA + FEDERAL LAW</Text>
      <Text style={styles.heroTitle}>Find the law. Check the source.</Text>
      <Text style={styles.heroBody}>
        Search verified library entries by statute number, legal citation, title, or everyday words.
      </Text>

      <View style={styles.searchWrap}>
        <Text style={styles.searchLabel}>Search the lawbook</Text>
        <TextInput
          value={query}
          onChangeText={setQuery}
          placeholder="Try AS 34.03.070, eviction, police search, overtime…"
          placeholderTextColor="#6B7C8B"
          autoCapitalize="none"
          autoCorrect={false}
          returnKeyType="search"
          style={styles.searchInput}
          accessibilityLabel="Search statutes, citations, and legal topics"
        />
      </View>

      {searching ? (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Search results</Text>
          <Text style={styles.sectionNote}>
            {results.length} verified {results.length === 1 ? 'match' : 'matches'}
          </Text>
          {results.length > 0 ? (
            results.map((entry) => (
              <LegalEntryCard key={entry.id} entry={entry} onPress={() => onOpenEntry(entry.id)} />
            ))
          ) : (
            <NoVerifiedMatch />
          )}
        </View>
      ) : (
        <>
          <ClassificationSection
            title="Alaska law"
            subtitle="State-specific information appears first."
            classifications={ALASKA_CLASSIFICATIONS}
            onOpen={onOpenClassification}
          />
          <ClassificationSection
            title="Federal law"
            subtitle="United States constitutional, statutory, and regulatory topics."
            classifications={FEDERAL_CLASSIFICATIONS}
            onOpen={onOpenClassification}
          />
        </>
      )}

      <LegalSafetyCard />
    </ScrollView>
  );
}

function ClassificationSection({
  title,
  subtitle,
  classifications,
  onOpen,
}: {
  title: string;
  subtitle: string;
  classifications: typeof ALASKA_CLASSIFICATIONS;
  onOpen: (classificationId: string) => void;
}) {
  return (
    <View style={styles.section}>
      <Text style={styles.sectionTitle}>{title}</Text>
      <Text style={styles.sectionNote}>{subtitle}</Text>
      <View style={styles.listCard}>
        {classifications.map((classification, index) => {
          const count = entriesForClassification(classification.id).length;
          return (
            <Pressable
              key={classification.id}
              accessibilityRole="button"
              onPress={() => onOpen(classification.id)}
              style={({ pressed }) => [
                styles.classificationRow,
                index < classifications.length - 1 && styles.rowBorder,
                pressed && styles.pressed,
              ]}
            >
              <View style={styles.rowTextWrap}>
                <Text style={styles.classificationTitle}>{classification.title}</Text>
                <Text style={styles.classificationMeta}>
                  {count > 0 ? `${count} verified ${count === 1 ? 'entry' : 'entries'}` : 'Library section'}
                </Text>
              </View>
              <Text style={styles.chevron}>›</Text>
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

function ClassificationScreen({
  classificationId,
  onOpenEntry,
}: {
  classificationId: string;
  onOpenEntry: (entryId: string) => void;
}) {
  const classification = CLASSIFICATIONS.find((item) => item.id === classificationId);
  const entries = entriesForClassification(classificationId);

  if (!classification) {
    return (
      <ScrollView contentContainerStyle={styles.page}>
        <NoVerifiedMatch />
      </ScrollView>
    );
  }

  return (
    <ScrollView contentContainerStyle={styles.page}>
      <JurisdictionBadge jurisdiction={classification.jurisdiction} />
      <Text style={styles.heroTitle}>{classification.title}</Text>
      <Text style={styles.heroBody}>
        Entries are separated by statute or source. Open an entry to see its summary, scope, exceptions,
        related citations, review status, and official source.
      </Text>

      <View style={styles.section}>
        {entries.length > 0 ? (
          entries.map((entry) => (
            <LegalEntryCard key={entry.id} entry={entry} onPress={() => onOpenEntry(entry.id)} />
          ))
        ) : (
          <View style={styles.warningCard}>
            <Text style={styles.warningTitle}>Verified content is still being added</Text>
            <Text style={styles.warningText}>{NO_VERIFIED_MATCH_MESSAGE}</Text>
          </View>
        )}
      </View>
      <LegalSafetyCard />
    </ScrollView>
  );
}

function LegalEntryCard({ entry, onPress }: { entry: LegalEntry; onPress: () => void }) {
  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      style={({ pressed }) => [styles.entryCard, pressed && styles.pressed]}
    >
      <View style={styles.entryTopLine}>
        <JurisdictionBadge jurisdiction={entry.jurisdiction} compact />
        <Text style={styles.citation}>{entry.citation}</Text>
      </View>
      <Text style={styles.entryTitle}>{entry.title}</Text>
      <Text numberOfLines={4} style={styles.entrySummary}>
        {entry.approvedSummary}
      </Text>
      <Text style={styles.sourceHint}>Official source available →</Text>
    </Pressable>
  );
}

function EntryScreen({ entryId }: { entryId: string }) {
  const entry = LEGAL_ENTRIES.find((item) => item.id === entryId);

  if (!entry) {
    return (
      <ScrollView contentContainerStyle={styles.page}>
        <NoVerifiedMatch />
      </ScrollView>
    );
  }

  const classification = CLASSIFICATIONS.find((item) => item.id === entry.classification);

  return (
    <ScrollView contentContainerStyle={styles.page}>
      <JurisdictionBadge jurisdiction={entry.jurisdiction} />
      <Text style={styles.citationLarge}>{entry.citation}</Text>
      <Text style={styles.heroTitle}>{entry.title}</Text>
      <Text style={styles.classificationBreadcrumb}>
        {classification?.title ?? 'Legal information'} · {entry.sourceType.replaceAll('_', ' ')}
      </Text>

      <DetailSection title="Plain-language summary">
        <Text style={styles.bodyText}>{entry.approvedSummary}</Text>
      </DetailSection>

      <DetailSection title="Who this rule applies to">
        <Text style={styles.bodyText}>{entry.appliesTo}</Text>
      </DetailSection>

      <DetailSection title="Important exceptions and limits">
        {entry.exceptions.map((item) => (
          <View key={item} style={styles.bulletRow}>
            <Text style={styles.bullet}>•</Text>
            <Text style={styles.bulletText}>{item}</Text>
          </View>
        ))}
      </DetailSection>

      <DetailSection title="Related citations">
        {entry.relatedCitations.map((citation) => (
          <Text key={citation} style={styles.relatedCitation}>
            {citation}
          </Text>
        ))}
      </DetailSection>

      <View style={styles.reviewCard}>
        <Text style={styles.reviewTitle}>Content review</Text>
        <Text style={styles.reviewText}>Last reviewed: {entry.lastReviewedDate}</Text>
        <Text style={styles.reviewText}>Version: {entry.contentVersion}</Text>
        <Text style={styles.reviewStatus}>
          {entry.reviewStatus === 'ATTORNEY_REVIEWED'
            ? 'Attorney reviewed'
            : entry.reviewStatus === 'NOT_ATTORNEY_REVIEWED'
              ? 'Not attorney reviewed'
              : 'Review required'}
        </Text>
      </View>

      <Pressable
        accessibilityRole="link"
        onPress={() => Linking.openURL(entry.officialUrl)}
        style={({ pressed }) => [styles.sourceButton, pressed && styles.sourceButtonPressed]}
      >
        <Text style={styles.sourceButtonText}>Open official source</Text>
      </Pressable>

      <LegalSafetyCard />
    </ScrollView>
  );
}

function DetailSection({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <View style={styles.detailSection}>
      <Text style={styles.detailTitle}>{title}</Text>
      {children}
    </View>
  );
}

function JurisdictionBadge({
  jurisdiction,
  compact = false,
}: {
  jurisdiction: Jurisdiction;
  compact?: boolean;
}) {
  const federal = jurisdiction === 'FEDERAL';
  return (
    <View
      accessibilityLabel={federal ? 'Federal law' : 'Alaska law'}
      style={[
        styles.badge,
        federal ? styles.federalBadge : styles.alaskaBadge,
        compact && styles.badgeCompact,
      ]}
    >
      <Text style={[styles.badgeText, federal ? styles.federalBadgeText : styles.alaskaBadgeText]}>
        {federal ? 'FEDERAL' : 'ALASKA'}
      </Text>
    </View>
  );
}

function NoVerifiedMatch() {
  return (
    <View style={styles.warningCard}>
      <Text style={styles.warningTitle}>No verified match found</Text>
      <Text style={styles.warningText}>{NO_VERIFIED_MATCH_MESSAGE}</Text>
    </View>
  );
}

function LegalSafetyCard() {
  return (
    <View style={styles.safetyCard}>
      <Text style={styles.safetyTitle}>About this lawbook</Text>
      <Text style={styles.safetyText}>
        This app covers Alaska state law and United States federal law only. It provides legal
        information, not legal advice, and does not create an attorney-client relationship. Law can
        change, and a general summary may not fit your facts. Check the official source and review date.
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: colors.background,
  },
  header: {
    minHeight: 58,
    paddingHorizontal: spacing.sm,
    backgroundColor: colors.surface,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: colors.border,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  headerSide: {
    width: 76,
  },
  headerButton: {
    minHeight: 44,
    justifyContent: 'center',
  },
  headerButtonText: {
    color: colors.alaskaBlue,
    fontSize: 16,
    fontWeight: '700',
  },
  brandButton: {
    minHeight: 44,
    justifyContent: 'center',
    alignItems: 'center',
    flexShrink: 1,
  },
  brand: {
    color: colors.ink,
    fontSize: 17,
    fontWeight: '800',
    textAlign: 'center',
  },
  disclaimerBar: {
    backgroundColor: colors.alaskaBlue,
    paddingHorizontal: spacing.md,
    paddingVertical: 8,
  },
  disclaimerText: {
    color: '#FFFFFF',
    fontSize: 12,
    lineHeight: 17,
    textAlign: 'center',
    fontWeight: '600',
  },
  page: {
    padding: spacing.lg,
    paddingBottom: 48,
  },
  eyebrow: {
    color: colors.alaskaBlue,
    fontSize: 12,
    fontWeight: '900',
    letterSpacing: 1.2,
    marginBottom: spacing.xs,
  },
  heroTitle: {
    color: colors.ink,
    fontSize: 30,
    lineHeight: 36,
    fontWeight: '900',
  },
  heroBody: {
    color: colors.muted,
    fontSize: 16,
    lineHeight: 24,
    marginTop: spacing.sm,
  },
  searchWrap: {
    marginTop: spacing.xl,
  },
  searchLabel: {
    color: colors.ink,
    fontSize: 14,
    fontWeight: '800',
    marginBottom: 8,
  },
  searchInput: {
    minHeight: 54,
    borderWidth: 2,
    borderColor: colors.alaskaBlue,
    borderRadius: 14,
    backgroundColor: colors.surface,
    paddingHorizontal: 16,
    paddingVertical: 12,
    color: colors.ink,
    fontSize: 16,
  },
  section: {
    marginTop: spacing.xl,
  },
  sectionTitle: {
    color: colors.ink,
    fontSize: 22,
    fontWeight: '900',
  },
  sectionNote: {
    color: colors.muted,
    fontSize: 14,
    lineHeight: 20,
    marginTop: 4,
    marginBottom: spacing.sm,
  },
  listCard: {
    backgroundColor: colors.surface,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.border,
    overflow: 'hidden',
  },
  classificationRow: {
    minHeight: 62,
    paddingHorizontal: spacing.md,
    paddingVertical: 12,
    flexDirection: 'row',
    alignItems: 'center',
  },
  rowBorder: {
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: colors.border,
  },
  rowTextWrap: {
    flex: 1,
    paddingRight: spacing.sm,
  },
  classificationTitle: {
    color: colors.ink,
    fontSize: 16,
    lineHeight: 21,
    fontWeight: '800',
  },
  classificationMeta: {
    color: colors.muted,
    fontSize: 12,
    marginTop: 3,
  },
  chevron: {
    color: colors.alaskaBlue,
    fontSize: 30,
    lineHeight: 32,
  },
  entryCard: {
    backgroundColor: colors.surface,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.border,
    padding: spacing.md,
    marginBottom: spacing.sm,
    minHeight: 148,
  },
  entryTopLine: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: 8,
  },
  citation: {
    color: colors.muted,
    fontSize: 13,
    fontWeight: '800',
  },
  citationLarge: {
    color: colors.alaskaBlue,
    fontSize: 17,
    fontWeight: '900',
    marginTop: spacing.md,
    marginBottom: spacing.xs,
  },
  entryTitle: {
    color: colors.ink,
    fontSize: 18,
    lineHeight: 24,
    fontWeight: '900',
    marginTop: spacing.sm,
  },
  entrySummary: {
    color: colors.muted,
    fontSize: 14,
    lineHeight: 21,
    marginTop: 8,
  },
  sourceHint: {
    color: colors.alaskaBlue,
    fontSize: 13,
    fontWeight: '800',
    marginTop: spacing.sm,
  },
  classificationBreadcrumb: {
    color: colors.muted,
    fontSize: 13,
    fontWeight: '700',
    marginTop: spacing.sm,
    textTransform: 'capitalize',
  },
  detailSection: {
    marginTop: spacing.xl,
    backgroundColor: colors.surface,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.border,
    padding: spacing.md,
  },
  detailTitle: {
    color: colors.ink,
    fontSize: 16,
    fontWeight: '900',
    marginBottom: spacing.sm,
  },
  bodyText: {
    color: colors.ink,
    fontSize: 16,
    lineHeight: 25,
  },
  bulletRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 10,
  },
  bullet: {
    color: colors.alaskaBlue,
    fontSize: 18,
    lineHeight: 24,
    marginRight: 9,
    fontWeight: '900',
  },
  bulletText: {
    color: colors.ink,
    fontSize: 15,
    lineHeight: 23,
    flex: 1,
  },
  relatedCitation: {
    color: colors.alaskaBlue,
    fontSize: 15,
    lineHeight: 24,
    fontWeight: '800',
  },
  badge: {
    alignSelf: 'flex-start',
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderWidth: 1,
  },
  badgeCompact: {
    paddingHorizontal: 9,
    paddingVertical: 4,
  },
  alaskaBadge: {
    backgroundColor: colors.alaskaBlueSoft,
    borderColor: colors.alaskaBlue,
  },
  federalBadge: {
    backgroundColor: colors.federalGoldSoft,
    borderColor: colors.federalGold,
  },
  badgeText: {
    fontSize: 11,
    letterSpacing: 0.8,
    fontWeight: '900',
  },
  alaskaBadgeText: {
    color: colors.alaskaBlue,
  },
  federalBadgeText: {
    color: '#6A4700',
  },
  warningCard: {
    borderWidth: 1,
    borderColor: colors.warningBorder,
    borderRadius: 16,
    backgroundColor: colors.warningBackground,
    padding: spacing.md,
    marginTop: spacing.sm,
  },
  warningTitle: {
    color: colors.warningText,
    fontSize: 16,
    fontWeight: '900',
  },
  warningText: {
    color: colors.warningText,
    fontSize: 14,
    lineHeight: 21,
    marginTop: 7,
  },
  reviewCard: {
    marginTop: spacing.xl,
    borderLeftWidth: 4,
    borderLeftColor: colors.federalGold,
    backgroundColor: colors.surface,
    padding: spacing.md,
    borderRadius: 12,
  },
  reviewTitle: {
    color: colors.ink,
    fontSize: 15,
    fontWeight: '900',
    marginBottom: 6,
  },
  reviewText: {
    color: colors.muted,
    fontSize: 13,
    lineHeight: 20,
  },
  reviewStatus: {
    color: colors.danger,
    fontSize: 13,
    lineHeight: 20,
    fontWeight: '900',
    marginTop: 4,
  },
  sourceButton: {
    minHeight: 54,
    marginTop: spacing.lg,
    borderRadius: 14,
    backgroundColor: colors.alaskaBlue,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: spacing.md,
  },
  sourceButtonPressed: {
    opacity: 0.82,
  },
  sourceButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '900',
  },
  safetyCard: {
    marginTop: spacing.xl,
    borderTopWidth: 1,
    borderTopColor: colors.border,
    paddingTop: spacing.lg,
  },
  safetyTitle: {
    color: colors.ink,
    fontSize: 14,
    fontWeight: '900',
  },
  safetyText: {
    color: colors.muted,
    fontSize: 13,
    lineHeight: 20,
    marginTop: 6,
  },
  pressed: {
    opacity: 0.72,
  },
});
