# Design Guidance

The visual and interaction system for Pocket Legal Advice. Read `AI-REQUIREMENTS.md` first — several
rules there (persistent disclaimer, abstention as a destination, deadline prominence) are design
obligations, not just engineering ones.

---

## Who is holding the phone

Design for the worst plausible context, not the demo context:

- They are frightened, and possibly have been awake all night.
- It may not be their phone. It may be a relative's, in a parking lot outside a jail.
- Battery is low. The screen may be cracked. The light is bad — harsh sun or a dim room.
- Someone may be watching over their shoulder.
- They have minutes, not an evening.
- They may read English as a second language, or with difficulty.

Every design decision below follows from that. The app is not a place to browse. It answers one
question and gets out of the way.

---

## Principles

**1. One job per screen.** Describe what happened → see what's verified → know what to do next.
Nothing competes with the primary action on any screen.

**2. Typeface encodes provenance.** Verified legal content is set in the serif. App chrome, controls
and labels are set in the sans. The distinction is load-bearing: the serif means *a human lawyer
reviewed this and it has a citation behind it*. Nothing else is ever set in the serif — and since
generated text never reaches the screen at all (AI rule 1), the serif can be trusted absolutely.

**3. Screenshot-first.** People will screenshot the action steps and send them to family or an
attorney. Every guidance screen must survive being cropped to a screenshot: the disclaimer, the
jurisdiction, and the review date travel with the content, not in a header bar three screens up.

**4. The core path works offline.** Know-your-rights content, the emergency routes and any previously
retrieved steps must be available with no signal — the situations where this app matters most are
often the ones with no connectivity. Only classification requires the network.

**5. No account for core value.** Nothing essential sits behind a sign-up. Requiring an identity from
someone seeking legal help is both a barrier and a liability.

**6. Show uncertainty honestly, and make it look deliberate.** The "we can't verify this" screen gets
the same design care as the success screen. It should read as integrity, not as a dead end — which
means it always carries an onward route to human help.

**7. Look like neither the police nor a product.** Avoid badge-adjacent iconography, navy-and-gold
authority styling, and anything that could be mistaken for an official government app. Equally avoid
growth-app gloss — gradients, mascots, celebratory animation. The register is a reference work: calm,
plain, unhurried.

**8. Quick exit.** A persistent control that leaves the app immediately and clears the visible state.
Standard practice in safety tooling, and cheap to build.

---

## Color

Drawn from the materials of the subject: ledger ink, court stamps, notarial seals. Semantic colors are
separate from the accent and never used decoratively.

| Token | Light | Dark | Use |
| --- | --- | --- | --- |
| `ground` | `#F7F6F3` | `#12151B` | Page background. Warm-neutral paper, not cream. |
| `surface` | `#FFFFFF` | `#191D25` | Cards, sheets. |
| `ink` | `#141821` | `#E8E9EC` | Primary text. Near-black with a slate-blue bias. |
| `ink-soft` | `#4A5260` | `#A8AEB9` | Secondary text. |
| `accent` | `#1F4E5F` | `#63B3C4` | Ledger teal. Primary actions, links, focus. |
| `seal` | `#2E6F4E` | `#6BAF87` | **Verified.** Reserved exclusively for reviewed content. |
| `docket` | `#8A6A1F` | `#C8A24A` | Caution, deadline approaching, stale content. |
| `stamp` | `#8C2F2F` | `#D97D7D` | Emergency, expired deadline, destructive actions. |

**Rules.**
- `seal` green is reserved. It marks verified content and nothing else — never a generic success
  toast, never a brand flourish. Its meaning must stay exact.
- Red (`stamp`) is never the app's accent. It appears only for genuine urgency, so that when it
  appears it is believed.
- Both themes are first-class. Dark mode is likely to be the common case at 2am.
- Contrast floor is WCAG AA for body text and AAA for anything on the emergency path.

### Material 3 mapping

`primary` → `accent` · `surface`/`background` → `ground`/`surface` · `error` → `stamp`.
`seal` and `docket` live outside the Material scheme as custom theme extensions, because Material has
no role that carries "a lawyer signed off on this."

---

## Type

No webfonts — the artifact CSP and offline-first both argue against network-loaded faces, and a silent
fallback would break the provenance rule above. Use platform faces deliberately.

- **Serif (verified content, headings):** Palatino-class. Georgia is the reliable Android fallback.
  Used for statute text, action steps, and screen titles.
- **Sans (UI):** the platform default. Labels, buttons, navigation, disclaimers.
- **Mono:** violation keys, content versions, timestamps, anything a developer or reviewer reads.

**Scale.** Body text no smaller than 16sp, and the app must remain usable at 200% system font scaling
without truncation — that is a release criterion, not a nice-to-have. Line length near 65 characters.
Line height 1.5 minimum for body content; legal text is dense and needs the air.

**Copy.** Short sentences. Active voice. Second person. Name things as the person recognizes them —
"the report the police wrote," not "the arresting officer's incident documentation." Target 6th–8th
grade reading level (AI rule 17). Citations sit alongside plain language, never replace it.

---

## Layout and interaction

- **Tap targets 48dp minimum**, with generous spacing on the emergency path — assume shaking hands.
- **Vertical, single-column, scrollable.** No horizontal paging for critical content, no carousels.
- **Deadlines pin to the top** of any screen that has one, above the steps.
- **The disclaimer sits with the content**, in-flow at the foot of the guidance block.
- **Loading states are honest:** show that a classification is running and that it may take a moment.
  Never fake progress.
- **Motion is minimal.** Transitions only where they explain a spatial relationship. Respect
  `prefers-reduced-motion`. Nothing celebratory — there is nothing to celebrate here.

## Accessibility

Non-negotiable, same tier as the AI rules — the audience skews toward people already
disadvantaged in the system this app touches.

- Every element labelled for TalkBack, with legal content read as continuous prose rather than
  fragments.
- Full functionality at 200% font scale and with display zoom on.
- Color is never the sole carrier of meaning — the verified state has an icon and a text label, not
  just the green.
- Full keyboard/switch-access traversal.
- Content available in Spanish at launch for any jurisdiction where that is a meaningful share of the
  affected population; translations pass the same attorney review as English (AI rule 14).

---

## The three core screens

**Describe.** A single text field, a single button, the disclaimer. Before first transmission, the
redacted-payload preview (AI rule 9). Nothing else.

**Verified guidance.** Deadline first if one exists. Then the verified block: seal-marked header,
statute text in serif, numbered action steps. Jurisdiction, review date and disclaimer at the foot,
inside the screenshot boundary.

**No verified match.** Equal design weight to the success screen. States plainly that we have no
reviewed guidance for this, that we are not going to guess, and routes onward to legal aid. This is
the screen that earns trust — treat it as the flagship, not the fallback.

---

## Anti-patterns

- Chat bubbles, typing indicators, or anything implying a conversation with an assistant.
- Confidence percentages shown to users. Internally they gate the result; externally they invite
  people to act on a 61% match.
- Streaks, badges, engagement mechanics. Nobody should be a daily active user of this app.
- Push notifications, except a deadline the user explicitly asked to be reminded about.
- Dismissible disclaimers.
- Placeholder or filler legal content in any build that can reach a real person — including TestFlight,
  internal betas and demos. If it is not reviewed, it is not shown.
