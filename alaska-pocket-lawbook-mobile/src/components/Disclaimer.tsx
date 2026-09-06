import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { colors } from '../theme/colors';
import { typography } from '../theme/typography';

export function LegalInfoDisclaimer() {
  return (
    <View style={styles.container} accessibilityRole="text">
      <Text style={styles.text}>
        This app provides legal information, not legal advice, and using it does not create an
        attorney-client relationship. It covers Alaska state law and U.S. federal law only.
      </Text>
    </View>
  );
}

export function NotAttorneyReviewedNotice() {
  return (
    <View style={[styles.container, styles.warning]} accessibilityRole="text">
      <Text style={[styles.text, styles.warningText]}>
        This entry has not yet been reviewed by a licensed attorney. Verify it against the
        official source below before relying on it.
      </Text>
    </View>
  );
}

export function NoVerifiedResultNotice() {
  return (
    <View style={[styles.container, styles.warning]} accessibilityRole="text">
      <Text style={[styles.text, styles.warningText, typography.body]}>
        We could not find a verified match. This does not mean no law applies. Try different
        words, search the statute number, or contact a qualified attorney or legal aid
        organization.
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.surfaceVariant,
    borderRadius: 10,
    padding: 12,
  },
  warning: {
    backgroundColor: '#F7ECEA',
    borderWidth: 1,
    borderColor: colors.error,
  },
  text: {
    ...typography.caption,
    color: colors.onSurfaceVariant,
  },
  warningText: {
    color: colors.error,
  },
});
