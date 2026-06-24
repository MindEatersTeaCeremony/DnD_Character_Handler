# Project guidelines

## Design system — colors, fonts, font sizes

Colors, fonts, and font sizes (type scale / "кегли") must come **only** from the design tokens file:

- `app/src/main/assets/design_tokens.json`

Rules:
- **Colors** — use only the hex values defined under `colors.materialTheme` and `colors.overview`. Do not invent new hex values. Examples: `surfaceVariant #2A2231`, `borderMuted #30FFFFFF`, `textMuted #D2CAC2`, `accentHpTemporary #69B7FF` (blue), `accentInspiration #FFD86B` (gold), `accentHeal #8AD178` (green).
- **Fonts & font sizes** — use the `MaterialTheme.typography.*` styles, which map to the sizes defined under `typography.materialTheme` (`headlineMedium` 28sp, `titleLarge` 22sp, `titleMedium` 18sp, `bodyLarge` 16sp, `bodyMedium` 14sp, `labelMedium` 12sp). Do not hardcode arbitrary `fontSize` values.
- **Never add new colors or font sizes (кегли) on your own.** If a new color or size seems necessary, you must first request it from the project owner and get explicit approval before adding it. No new value goes into the code without that approval.
- When touching older code that uses non-token hex values, align it to the tokens.

## Git workflow

- **Do not create separate branches.** Commit directly to `main` and push immediately.
- This overrides the default "branch first on the default branch" behavior — for this project, work straight on `main`.
