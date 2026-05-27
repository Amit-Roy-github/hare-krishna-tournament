# Design — Hare Krishna Android

**Direction: premium, modern, dark-first, devotional minimal. Independent from the web app — do not borrow web colors, fonts, components, or layouts.**

The web app is functional but visually plain. The Android app is the user's daily-use spiritual tool — it sits in their hand while they chant. It should feel like Linear or Things 3, not like a tournament dashboard. Calm. Premium. A pleasure to open.

If a design decision below conflicts with a Compose default, the decision wins. If it conflicts with a stakeholder ask, the conflict gets resolved in the design channel before code is written.

---

## Direction in one sentence

A premium devotional companion: a hushed dark canvas, a single warm primary that draws the eye to the act of chanting, generous breathing room, and subtle motion that rewards every tap without ever feeling busy.

---

## What we are NOT doing

- **No web parity.** No pages from the web should look "the same on mobile." We're not porting the web — we're building a different product for a different surface.
- **No saffron-overload.** Saffron is the *primary accent*, not a wash. Most surfaces are dark; saffron lives in the counter, action buttons, focused states. Restraint = premium.
- **No 90s-Indian-website devotional clichés.** No glitter, no rangoli borders, no marquee gold text, no Comic Sans MS, no Sanskrit script in headings. Modern execution, devotional intent.
- **No light theme as the primary experience.** Light is a fallback for users who force it. The whole design is tuned for dark.
- **No Material Design 2 patterns** (raised cards with shadows, FABs, hamburger drawers). Material 3 + restraint.
- **No skeuomorphism.** A mala counter is a digital surface, not a fake bead string.

---

## Colors

Full tokens are in `app/src/main/java/com/harekrishna/ui/theme/Color.kt`. The Compose theme in `Theme.kt` maps them to M3 roles.

| Token         | Hex       | Used for |
|---------------|-----------|----------|
| InkBlack      | `#0E0B14` | App background. Warm-tinted near-black, not pure `#000`. |
| Surface       | `#19131F` | Cards, sheets, top app bar background. |
| SurfaceRaised | `#241C2D` | Pressed/hover/focus state on Surface. |
| Saffron       | `#FFA62B` | **Primary.** Counter, primary CTAs, focused inputs, key icons. Should appear on at most one element per viewport. |
| SaffronDeep   | `#1A0F00` | Text/icons placed on Saffron. |
| DevoPurple    | `#8B5CF6` | **Secondary.** Info chips, secondary buttons, links. A nod to Krishna's traditional color, modernized. |
| FestiveGold   | `#FFD700` | **Tertiary, used rarely.** Finale moments, "naam jaap hero" badge, week-ending celebration. Sparingly = special. |
| WarmWhite     | `#F5F1E8` | Primary text on dark surfaces. Slightly warm to feel inviting; pure `#FFF` feels clinical. |
| MutedSand     | `#A19A8E` | Secondary/muted text. Captions, helper text, timestamps. |
| ErrorRed      | `#EF4444` | Errors only. Not a decorative color. |

**Contrast rule**: all text-on-background pairs must clear WCAG AA (≥4.5:1 for body, ≥3:1 for large text). The palette above clears AA on InkBlack and Surface; if you add new pairings, verify with a contrast checker before shipping.

---

## Typography

Tokens in `app/src/main/java/com/harekrishna/ui/theme/Type.kt`. The intent vs the v1 mapping:

| Role     | Intended family (post-v1) | v1 fallback | Why |
|----------|---------------------------|-------------|-----|
| Display  | **Spectral** (serif, semibold) | `FontFamily.Serif` | A devotional warmth — the hero counter and welcome moments feel hand-carved, not generated. |
| Headline / Body / Label | **Inter** (sans, regular–semibold) | `FontFamily.SansSerif` | Modern, ubiquitous, neutral. Lets Spectral do the emotional work. |

When you add font files to `res/font/`, change the two `*Family` constants in `Type.kt` — no other change needed.

Sizing follows the M3 scale already in `Type.kt`. Default to body sizes; reach for `displayMedium` only for the hero counter number.

---

## Spacing & shape

- **8dp grid.** All paddings, margins, and gaps are multiples of 4 (and ideally 8).
- **Generous outer padding.** Screens use 24dp horizontal padding minimum. Premium apps breathe.
- **Corner radius scale**: 12dp for inputs/chips, 20dp for cards, 28dp for sheets, 50% (pill) for primary buttons, 50% (circle) for the counter button.
- **No drop shadows.** Elevation is conveyed by `surface` → `surfaceVariant` color shift, not by Material 2 shadows. Cleaner on dark, more modern.

---

## Motion

- **Compose defaults are the floor.** Don't disable animations.
- **Counter tap**: spring with `dampingRatio = MediumBouncy`, `stiffness = StiffnessMedium`. The number scales briefly (1f → 1.08f → 1f) on each tap; rich haptic (`HapticFeedbackType.TextHandleMove`) accompanies it. The tap should feel **delightful** without being silly.
- **Screen transitions**: 300ms fade + slight slide-up (16dp). Avoid horizontal slide unless it represents back-navigation.
- **Loading**: shimmer skeletons over circular spinners wherever possible. Spinners get used only for ≤1s operations.
- **No looping/idle animations.** Nothing should move when the user is reading.

---

## Iconography

- **Material Symbols Rounded** — softer/friendlier than Outlined or Sharp, fits the devotional warmth.
- Outline weight by default; fill weight when the icon represents the *currently active* selection.
- 24dp default size, 20dp in dense rows.
- Don't mix icon styles in one screen.

---

## Components — opinionated decisions

### Counter button (the hero)

- Large circle, ~64% of the narrower viewport dimension. Centered.
- Surface fill: subtle saffron-to-saffron-deep radial gradient. No flat fill.
- The current today count is the giant number inside, `displayLarge` weight.
- Whole circle is the tap target — not a small floating button. The user shouldn't have to aim.
- On tap: number scales 1f→1.08f→1f over 240ms + haptic + 1px ring pulse expands outward.

### Stat tiles ("Today" / "This Week")

- Two equal-width tiles below the counter, 12dp gap between.
- Surface color, 20dp corner radius. No border, no shadow.
- Two-line content: small `labelSmall` MutedSand caption ("Today" / "This Week"), large `headlineMedium` WarmWhite number.

### Top app bar

- No back button on Home (it's the root).
- Left: `bhaktName` in `titleMedium`. Right: sign-out icon (`Logout` from Material Symbols).
- Transparent background (sits over the InkBlack canvas). No bottom divider.

### Sync status bar (footer)

- Small `labelSmall` MutedSand text. Animated 12dp dot to the left:
  - **Idle / synced**: 4dp Saffron dot.
  - **Pending**: 6dp Saffron dot pulsing gently (4dp ↔ 6dp, 1.2s sine).
  - **Failed**: ErrorRed dot, no pulse, with retry icon button next to it.

### Login screen

- Same dark canvas. No card around the form — fields sit directly on `InkBlack`.
- Name dropdown styled as an `OutlinedTextField` with `trailing` chevron, modal `BottomSheet` for the picker (not a system spinner).
- Password field with show/hide eye icon, large submit button at the bottom (saffron filled, pill shape).
- Footer: `"Forgot password? Contact admin."` in MutedSand, no separate screen for it.

---

## App icon (deferred to polish phase)

Concept: a simple lotus-glyph monogram. Single-color (Saffron) foreground on a slightly textured InkBlack background. Adaptive icon — background and foreground as separate layers so Android's mask system handles all shapes consistently.

Not a priority for v1 functional milestones; the wizard's default mipmap stays until the polish phase.

---

## Accessibility — non-negotiable

- All interactive components have a minimum 48dp × 48dp touch target.
- All text passes WCAG AA contrast on its intended background.
- `contentDescription` on every icon button.
- Talkback labels on the counter announce the new count after each increment.
- Dynamic type / font-scale respected — never hardcode `sp` for body text in a way that defeats scaling.

---

## Code expectations

- Theme tokens live in `ui/theme/Color.kt` and `Type.kt`. **No hex codes inline in screens.**
- Spacing values use `Dp` extensions or are inline — but consistent within a screen. If a magic-number `Dp` appears in three places, extract it into a sibling `Dimens.kt`.
- Components shared across two or more screens live in `ui/common/`; screen-local components in the screen's own `components/` folder.
- Every public composable has a `@Preview` showing both dark and (when relevant) light themes.
