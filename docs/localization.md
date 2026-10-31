# Mobile localization

Reviewed on 2026-10-31. The enabled languages remain Spanish and English.
Spanish copy uses natural wording familiar in Mexico, complete accents and
characters (`áéíóúüñ¿¡`); English uses US spelling and sentence case. User-entered
notes and real center names must retain their original spelling. The corrected
center names in this change belong to the sample catalog.

## Sources of truth

- `shared/.../domain/Models.kt`: `SupportedLocale` is the enabled-language
  registry, with canonical BCP-47 codes and native display names. Android and
  iOS settings enumerate it; selecting Spanish always shows `Español`, even in
  an English interface. Device preferences, manual override and persistence
  use the same registry. Regional/script tags are matched from most specific
  to least specific; English remains the fallback.
- Android `res/values*/strings.xml` and `plurals.xml`: all app UI copy, typed
  resource references and native plural selection. No duplicate English/
  Spanish display-text dictionaries remain in Kotlin UI code. Language splits
  are disabled in the app bundle so the manual selector works offline after a
  Play install too, following [Android's language-change guidance](https://developer.android.com/guide/app-bundle/configure-base#handling_language_changes).
- iOS `<code>.lproj/Localizable.strings`, `Localizable.stringsdict` and
  `InfoPlist.strings`: UI copy, native plurals and system permission text.
  Missing translated UI keys fall back to the English bundle. The release
  check requires complete catalogs, so a raw key is never an accepted
  translation. `AppLocale` wraps the shared registry instead of maintaining
  a separate enum or a Spanish-versus-English branch.
- `LocalizedText.values`: translated catalog content keyed by language code,
  with existing deterministic English/available-source fallback. It already
  accepts additional language entries; no automatic translation is performed.
- `shared/.../data/NotificationCopy.kt`: draft localized notification text.
  Its test requires a title and body for every enabled language and type.
  This does not enable notification delivery.

Use complete messages with placeholders; do not join a number with a translated
sentence fragment. Remaining quantities use native plural rules, including zero,
one and many. Integer metrics use native number formatting for the selected
language. Dates, currency and distances should also use native formatters when
those views are implemented; unused manual distance/points string builders were
removed. Choosing the system language currently resolves to the supported base
language; a separate device-region preference for number/date formats is not yet
implemented.

Search uses native Unicode decomposition/diacritic folding on temporary keys,
so `ninos`, `niños` and decomposed `niños` find the same sample center. Stored
and displayed text is never stripped of accents or ñ. This is intentionally a
permissive search match, not a linguistic collation rule for every future locale.

## Adding a language

1. Prepare reviewed translations before enabling the language. Add one registry
   entry with its BCP-47 code and native name, for example `fr` / `Français`.
2. Copy and translate every UI key into Android `values-fr/strings.xml` and
   iOS `fr.lproj/Localizable.strings`. Preserve placeholder positions and types.
   Use BCP-47 Android folder qualifiers for script/region variants, such as
   `values-b+pt+BR`. Escape platform syntax while preserving UTF-8 characters.
3. Supply native plural categories for that language in Android `plurals.xml`
   and iOS `Localizable.stringsdict`; include `other` and every applicable form
   (`zero`, `one`, `two`, `few`, `many`). Translate `InfoPlist.strings` too.
4. Add the language to `knownRegions` in `iosApp/project.yml`, then run
   `xcodegen generate --spec iosApp/project.yml`. Add notification translations
   to `NotificationCopy` using `LocalizedText(mapOf(...))`. Add catalog
   translations when available; untranslated source content may use fallback.
5. Run `sh scripts/check-localization.sh --self-test`, shared tests and native
   localization tests. The checker discovers all registry entries and rejects
   missing/duplicate keys, damaged Unicode, non-NFC resources, placeholder
   differences and Android/iOS copy differences.
6. Review that language on both platforms: language persistence, system
   preference order, fallback, plural forms, numbers, long text, search,
   keyboard and screen reader. Layout direction follows the active language;
   an RTL language still requires a visual/navigation audit before release.

Keep source files UTF-8 (enforced by `.editorconfig`). Do not remove accents to
work around fonts or encoding problems. Do not enable an untranslated language
merely because the registry can represent it.

## Verification for this revision

The focused coverage includes Unicode content preservation, accent-insensitive
search on JVM and iOS, registry resolution, notification coverage, Android/iOS
singular/plural resources and the existing native language/navigation checks.
Current results are recorded in [localization verification](testing/localization-verification.md).
This review covers the mobile apps and their shared content. The separately
developed web portal retains its own localization workstream.
