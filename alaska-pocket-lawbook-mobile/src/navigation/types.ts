export type RootStackParamList = {
  Home: undefined;
  Classification: { classificationId: string };
  /** entryIds is the ordered list being browsed (a classification's entries, or a search
   * result set) so Previous/Next can step through it regardless of where it came from. */
  StatuteDetail: { entryIds: string[]; index: number };
};
