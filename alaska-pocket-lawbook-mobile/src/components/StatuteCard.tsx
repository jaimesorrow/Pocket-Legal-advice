import React from 'react';
import { Linking, StyleSheet, Text, View, Pressable } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';
import { LegalEntry } from '../data/types';
import { JurisdictionBadge } from './JurisdictionBadge';
import { NotAttorneyReviewedNotice } from './Disclaimer';
import { getClassificationById } from '../data/legalData';

export function StatuteCard({ entry, showClassification = false }: { entry: LegalEntry; showClassification?: boolean }) {
  const classification = getClassificationById(entry.classificationId);

  return (
    <View style={styles.card}>
      <View style={styles.headerRow}>
        <JurisdictionBadge jurisdiction={entry.jurisdiction} />
        {showClassification && classification ? (
          <Text style={styles.classificationLabel}>{classification.name}</Text>
        ) : null}
      </View>

      <Text style={styles.citation}>{entry.citation}</Text>
      <Text style={styles.title}>{entry.title}</Text>

      <Text style={styles.body}>{entry.approvedSummary}</Text>

      <Field label="Who it applies to" value={entry.whoItAppliesTo} />
      <Field label="Important exceptions" value={entry.importantExceptions} />
      {entry.relatedCitations.length > 0 ? (
        <Field label="Related statutes" value={entry.relatedCitations.join(', ')} />
      ) : null}

      <View style={styles.metaRow}>
        <Text style={styles.meta}>Effective: {entry.effectiveDate}</Text>
        <Text style={styles.meta}>Last reviewed: {entry.lastReviewedDate}</Text>
      </View>

      {entry.reviewStatus === 'NOT_ATTORNEY_REVIEWED' ? (
        <View style={styles.noticeSpacing}>
          <NotAttorneyReviewedNotice />
        </View>
      ) : null}

      <Pressable
        accessibilityRole="button"
        style={styles.sourceButton}
        onPress={() => Linking.openURL(entry.officialUrl)}
      >
        <Text style={styles.sourceButtonText}>View official source</Text>
      </Pressable>
    </View>
  );
}

function Field({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.field}>
      <Text style={styles.fieldLabel}>{label}</Text>
      <Text style={styles.body}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.surface,
    borderRadius: 14,
    padding: 16,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: colors.outline,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  classificationLabel: {
    ...typography.caption,
    color: colors.onSurfaceVariant,
  },
  citation: {
    ...typography.label,
    color: colors.onSurfaceVariant,
    marginBottom: 2,
  },
  title: {
    ...typography.statuteTitle,
    color: colors.ink,
    marginBottom: 8,
  },
  body: {
    ...typography.body,
    color: colors.ink,
  },
  field: {
    marginTop: 10,
  },
  fieldLabel: {
    ...typography.label,
    color: colors.onSurfaceVariant,
    marginBottom: 2,
  },
  metaRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 12,
  },
  meta: {
    ...typography.caption,
    color: colors.onSurfaceVariant,
  },
  noticeSpacing: {
    marginTop: 10,
  },
  sourceButton: {
    marginTop: 14,
    backgroundColor: colors.flagBlue,
    borderRadius: 10,
    paddingVertical: 12,
    alignItems: 'center',
    minHeight: 44,
    justifyContent: 'center',
  },
  sourceButtonText: {
    ...typography.button,
    color: colors.white,
  },
});
