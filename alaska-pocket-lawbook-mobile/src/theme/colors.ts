// Alaska's flag is gold stars on a field of deep blue, so the app takes its two
// signal colors from it: blue carries Alaska law, gold carries federal law.
// Mirrors app/src/main/java/com/pocketlawbook/alaska/ui/theme/Theme.kt (light scheme)
// so the mobile app matches the native app's branding.
export const colors = {
  flagBlue: '#1B4570',
  flagGold: '#A87209',

  ink: '#0E2233',
  ground: '#EDF1F5',
  surface: '#FFFFFF',
  surfaceVariant: '#E3EAF1',
  onSurfaceVariant: '#33495B',
  outline: '#C9D6E1',
  error: '#A3302A',

  white: '#FFFFFF',
} as const;

export function jurisdictionColor(jurisdiction: 'ALASKA' | 'FEDERAL'): string {
  return jurisdiction === 'FEDERAL' ? colors.flagGold : colors.flagBlue;
}
