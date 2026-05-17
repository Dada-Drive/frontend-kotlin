# Design Migration v1 → v2

> Généré le 2026-05-17 dans le cadre de R-4.1 (D0 baseline pré-refonte).
> Ce document est la référence pour les phases **R-4.2** (Inter fonts), **R-4.3** (SVG icons), **R-4.4** (nouveaux composants) et **R-4.5** (rename tokens) du Sprint S4, ainsi que pour les sprints S5/S6 (D2-D10).
> Spec v2 source : `design-system.md` §2.1-2.7.

---

## 1. Snapshots baseline v1

5 écrans capturés en `app/src/test/snapshots/images/` avec suffixe `*BaselineTest_*` (light + dark), soit **10 PNG**.

| Écran | Test class | Stratégie | Couverture |
|---|---|---|---|
| SplashScreen | `SplashScreenBaselineTest` | `SplashScreenLayout(alpha = 1f)` stateless | ✅ light + dark |
| WelcomeScreen | `WelcomeScreenBaselineTest` | callbacks vides + `AuthState.Idle` | ✅ light + dark |
| NameEntryScreen | `NameEntryScreenBaselineTest` | stub VM mockk (state `Idle`) | ✅ light + dark |
| RoleSelectionScreen | `RoleSelectionScreenBaselineTest` | stub VM mockk (state `Idle`) | ✅ light + dark |
| WalletScreen | `WalletScreenBaselineTest` | stub VM mockk (loaded happy path) | ✅ light + dark |

### Limitations baseline v1

6 écrans **non snapshottés** en v1. À traiter en R-4.4 (décomposition en sous-Layouts stateless) ou via setup spécifique :

| Écran | Raison | Plan de couverture |
|---|---|---|
| OnboardingScreen | Utilise `rememberLauncherForActivityResult` → requiert `LocalActivityResultRegistryOwner` ; tentative de stub a échoué (NPE LifecycleRegistry) | Extraire `OnboardingPageContent` stateless en R-4.4 |
| PhoneScreen | `authViewModel: AuthViewModel` paramètre obligatoire ; flow OTP intégré complexe | Décomposer en `PhonePadLayout` + `OtpVerifyLayout` en R-4.4 |
| DriverSetupScreen | VM avec deps Cloudinary + repos ; multi-step wizard | Snapshotter chaque step (DriverPersonalStep, DriverLicenseStep, DriverVehicleStep) déjà extraits |
| DriverHomeScreen | `profileViewModel` obligatoire + 3 VMs Hilt + dépendance HereSDK | Refactor en R-5.3 |
| MapScreen | 4 VMs Hilt + HereMapViewComposable Android SDK | Refactor en R-5.3 |
| OTP (intégré dans PhoneScreen) | n/a | Couvert par décomposition PhoneScreen |

---

## 2. Table mapping couleurs v1 → v2

Couleurs source : [`AppColors.kt`](../app/src/main/java/tn/turbodrive/core/theme/AppColors.kt) (50 fields, post R-2.3) + [`MapColorTokens.kt`](../app/src/main/java/tn/turbodrive/core/theme/MapColorTokens.kt) (6 tokens transitionnels).
Spec v2 : `design-system.md` §2.1 (light) / §2.2 (dark).

### Tokens primaires (rename ou keep)

| Token v1 | Light | Dark | Token v2 | Action R-4.5 |
|---|---|---|---|---|
| `primary` | `#0A0A0A` | `#F4F4F0` | `primary` | keep |
| `onPrimary` | `#FFFFFF` | `#0A0A0A` | `onPrimary` | keep |
| `background` | `#F6F5F1` | `#0A0A0C` | `bg` | **rename** |
| `surface` | `#FFFFFF` | `#161618` | `surface` | keep |
| `surfaceMuted` | `#F1EFEA` | `#1F1F22` | `surfaceAlt` | **rename** |
| `primaryDisabled` | `#EAE7E0` | `#28282D` | `surfaceDeep` | **rename + resemantize** |
| `textPrimary` | `#0A0A0A` | `#F4F4F0` | `text` | **rename** |
| `textSecondary` | `#5C5C5C` | `#A8A8A2` | `textMuted` | **rename** |
| `textHint` | `#8A8A85` | `#6E6E68` | `textSubtle` | **rename** |
| `textDisabled` | `#B5B3AC` | `#454540` | `textDisabled` | keep |
| `successGreen` | `#16A34A` | `#22C55E` | `accent` | **rename ⚠ breaking** |
| `successContainer` | `#DCFCE7` | `#14532D` | `accentSoft` | **rename** (ajouté en R-2.3 batch1) |
| `errorRed` | `#DC2626` | `#EF4444` | `error` | **rename** |
| `errorContainer` | `#FEE2E2` | `#3B0E13` | `errorSoft` | **rename** |
| `warningOrange` | `#D97706` | `#F59E0B` | `warning` | **rename** |
| `infoBlue` | `#2563EB` | `#3B82F6` | `info` | **rename** |
| `border` | `#E5E2D8` | `#2A2A2E` | `border` | keep |
| `outlineLight` | `#CFCABE` | `#3A3A40` | `borderStrong` | **rename** |
| `divider` | `#EDEAE0` | `#22222A` | `divider` | keep |
| `googleRed` | `#EA4335` | `#EA4335` | `google` | **rename** |
| `facebookBlue` | `#1877F2` | `#1877F2` | `facebook` | **rename** |
| `ratingYellow` | `#FFC107` | `#FFC107` | `star` | **rename** (ajouté en R-2.3 batch2-5) |
| `surfaceOverlaySemi` | `#00000080` | `#00000080` | `overlayDim` | **rename** |
| `coinSilver` | `#C0C0C0` | `#9B9B9B` | `coinSilver` | keep (custom hors spec) |
| `coinGold` | `#D4AF37` | `#B89530` | `coinGold` | keep (custom hors spec) |

### Tokens v1 deprecated (à supprimer après R-4.5)

Doublons / alias redondants avec d'autres tokens — supprimer pour éviter la confusion :

| Token v1 deprecated | Doublon de | Action |
|---|---|---|
| `secondary` / `onSecondary` | `textMuted` / `onPrimary` | remove |
| `onBackground` | `text` | remove |
| `lightSurface` | `surface` | remove |
| `darkSurface` | `surfaceAlt` | remove |
| `inputBackground` / `darkInput` / `lightInput` / `inputUnderline` | `surface` / `surfaceAlt` / `border` | remove (utiliser tokens génériques) |
| `textLabel` / `textTertiary` / `textCaption` | `textMuted` / `textSubtle` | remove |
| `greyHint` / `greyLabel` / `dividerGrey` | `textSubtle` / `textMuted` / `divider` | remove |
| `buttonBackground` / `buttonText` / `buttonDisabledBackground` / `buttonDisabledText` | `primary` / `onPrimary` / `surfaceDeep` / `textDisabled` | remove |
| `dragHandle` | `textSubtle` | remove |
| `onErrorContainer` | `error` | remove |
| `surfaceElevated` | `surface` | remove |
| `locationMarkerBlue` / `locationMarkerBlueDark` / `locationBlueLight` / `locationCirclePrecision` | (map tokens) | déplacer dans `MapColorTokens` |

### Tokens v2 à créer en R-4.5 (n'existent pas en v1)

| Token v2 | Light | Dark | Usage |
|---|---|---|---|
| `accentInk` | `#0A7A2A` | `#4ADE80` | Texte sur fond `accentSoft` (badges succès) |
| `warningSoft` | `#FEF3C7` | `#3A2A0E` | Fond banner warning |
| `infoSoft` | `#DBEAFE` | `#1E3A5F` | Fond banner info |
| `inkSoft` | `#B5B3AC` | `#454540` | Disabled fills (alias de `textDisabled` sémantique différent) |
| `inkSubtle` | `#CFCABE` | `#3A3A40` | Fills très atténués |
| `onInk` | `#FFFFFF` | `#0A0A0A` | Texte sur fond ink |
| `whatsapp` | `#25D366` | `#25D366` | Brand WhatsApp |
| `destination` | (alias `error`) | (alias `error`) | Pin destination carte |
| `mapLand` | `#F2EFE9` | `#2A2A2A` | Fond carte territoire |
| `mapWater` | `#C9E8F5` | `#1A3A4A` | Eau sur carte |
| `mapRoad` | `#FFFFFF` | `#3A3A3A` | Routes principales |
| `mapPath` | `#1A73E8` | `#4A90D9` | Itinéraire actif (remplace `MapColorTokens.routeActiveBlue`) |

### MapColorTokens v1 → AppColorScheme v2

`MapColorTokens.kt` (créé en R-2.3 batch3 comme transitionnel) sera **dissous** en R-4.5 :

| MapColorTokens v1 | Token v2 | Action |
|---|---|---|
| `routeActiveBlue` (`#2D79FF`) | `mapPath` | déplacer dans `AppColorScheme` |
| `routeSecondLeg` (`#43A047`) | `accent` (avec alpha) ou nouveau `mapPathAlt` | à décider |
| `scheduleAccent` (`#4FC3C8`) | nouveau `scheduleTeal` (custom) | garder, hors spec |
| `darkPanelSurface` (`#2C2C2C`) | `surfaceDeep` dark | remplacer |
| `pinIntermediate` (`#1A1A1A`) | `text` dark | remplacer |
| `connectorGrey` (`#8E8E93`) | `textSubtle` | remplacer |

---

## 3. Typographie v1 → v2

Source : [`AppTypography.kt`](../app/src/main/java/tn/turbodrive/core/theme/AppTypography.kt) (13 styles, `FontFamily.Default`).
Spec v2 : `design-system.md` §3 (14 styles, **Inter** font family).

### Mapping

| Style v1 | Size v1 | Weight v1 | Style v2 | Size v2 | Weight v2 | Delta |
|---|---|---|---|---|---|---|
| `displayLarge` | 36 | 700 | `displayL` | 36 | 700 | rename |
| `displayMedium` | 30 | 700 | `displayM` | 30 | 700 | rename |
| `headingL` | 26 | 600 | `headingL` | 26 | 600 | keep |
| `headingM` | 20 | 600 | `headingM` | 20 | 600 | keep |
| `headingS` | 18 | 600 | `headingS` | 18 | 600 | keep |
| `bodyL` | 18 | 400 | `bodyL` | 18 | 400 | keep |
| `bodyM` | 16 | 400 | `bodyM` | 16 | 400 | keep |
| `bodyS` | 14 | 400 | `bodyS` | 14 | 400 | keep |
| `labelL` | 16 | 500 | `labelL` | 16 | 500 | keep |
| `labelM` | 14 | 500 | `labelM` | 14 | 500 | keep |
| `labelS` | 12 | 500 | `labelS` | 12 | 500 | keep |
| `monoL` | 28 | 700 | `monoL` | 28 | 700 | keep |
| `monoM` | 20 | 500 | `monoM` | 20 | 500 | keep |

### Styles v2 à créer

| Style v2 | Size | Weight | Usage |
|---|---|---|---|
| `button` | 16 | 600 | Texte des CTA (actuellement utilise `labelL`) |
| `bodyStrong` | 16 | 600 | Body emphasized |
| `smallStr` | 12 | 600 | Caption emphasized |

### Action transverse R-4.2 (Inter fonts)

- Ajouter `res/font/inter_regular.ttf`, `inter_medium.ttf`, `inter_semibold.ttf`, `inter_bold.ttf` (depuis `turbodrive_redesign/`)
- Remplacer `FontFamily.Default` par `FontFamily(Font(R.font.inter_*, weight = X))` dans `AppTypography.kt`
- Ajouter letter-spacing par token (spec v2 prescrit `-0.025em` pour `displayLarge`, etc.)

---

## 4. Spacing v1 → v2

Source : [`AppSpacing.kt`](../app/src/main/java/tn/turbodrive/core/designsystem/spacing/AppSpacing.kt).
Spec v2 : `design-system.md` §4.

### Mapping (déjà aligné v2 — pas d'action requise)

| Token v1 | Value (dp) | Token v2 | Action |
|---|---|---|---|
| `xxs` / `xs` | 4 | `xs` | keep |
| `s` | 8 | `s` | keep |
| `m` / `sm` | 12 | `m` | keep |
| `l` / `md` | 16 | `l` | keep |
| `xl` / `lg` | 24 | `xl` | keep |
| `xxl` | 32 | `xxl` | keep |
| `xxxl` | 48 | `xxxl` | keep |
| `xxxxl` | 64 | `xxxxl` | keep |
| `screenH` | 20 | `screenH` | keep (custom) |
| `screenVertical` | 24 | `screenV` | rename éventuel |

### Component-specific spacing (à déplacer dans tokens dédiés ?)

| Constante actuelle | Value | Plan R-4.4 |
|---|---|---|
| `inputRadius` | 12.dp | déjà dans `AppRadius.m` |
| `buttonRadius` | 999.dp | déjà dans `AppRadius.full` |
| `buttonHeight` | 56.dp | nouveau token `AppSize.buttonHeightL` |
| `cardRadius` | 16.dp | proche de `AppRadius.l` (20) — décider unification |
| `sheetRadius` | 20.dp | déjà dans `AppRadius.l` |

---

## 5. Radius v1 → v2

Source : [`AppRadius.kt`](../app/src/main/java/tn/turbodrive/core/designsystem/spacing/AppRadius.kt).
Spec v2 : `design-system.md` §5.

| Token v1 | Value v1 | Token v2 | Value v2 | Delta |
|---|---|---|---|---|
| `s` | 6 | `rS` | 8 | **rename + bump +2dp** |
| `m` | 12 | `rM` | 12 | rename |
| `l` | 20 | `rL` | 20 | rename |
| `xl` | 32 | `rXL` | 32 | rename |
| `full` | 999 | `rFull` | 999 | rename |

---

## 6. Motion v1 → v2

Source : [`AppMotion.kt`](../app/src/main/java/tn/turbodrive/core/designsystem/tokens/AppMotion.kt).
Spec v2 : `design-system.md` §6.

### Durations

| Token v1 | Value v1 | Token v2 | Value v2 | Delta |
|---|---|---|---|---|
| `DURATION_FAST_MS` | 150ms | `fast` | 120ms | **-30ms** |
| `DURATION_NORMAL_MS` | 250ms | `base` | 180ms | **-70ms** |
| `DURATION_SLOW_MS` | 400ms | `slow` | 400ms | keep |

### Springs

| Token v1 | Config v1 | Token v2 | Action |
|---|---|---|---|
| `springDefault` | `MediumBouncy` + `StiffnessMedium` | `springBounce` | rename |
| `springLow` | `NoBouncy` + `StiffnessLow` | `springPanel` | rename |

### Easing curves v2 à ajouter

| Token v2 | Curve | Usage |
|---|---|---|
| `easeOut` | `CubicBezier(0.2f, 0f, 0f, 1f)` | Entrée éléments |
| `easeIn` | `CubicBezier(0.4f, 0f, 1f, 1f)` | Sortie éléments |
| `linear` | `LinearEasing` | Progress, loaders |

---

## 7. Shadows v1 → v2

**Aucun token shadow défini en v1.** Spec v2 prescrit 6 tokens à créer (`design-system.md` §7) :

| Token v2 | Layer 1 | Layer 2 | Usage |
|---|---|---|---|
| `control` | 0/1/3 rgba(0,0,0,0.08) | 0/1/2 rgba(0,0,0,0.04) | Boutons map, contrôles |
| `search` | 0/2/8 rgba(0,0,0,0.08) | 0/1/3 rgba(0,0,0,0.04) | Search bars |
| `toast` | 0/2/8 rgba(0,0,0,0.08) | 0/1/3 rgba(0,0,0,0.04) | Toasts, snackbars |
| `card` | 0/4/16 rgba(0,0,0,0.08) | 0/2/6 rgba(0,0,0,0.04) | Cards, panels |
| `sheet` | 0/8/32 rgba(0,0,0,0.12) | 0/4/12 rgba(0,0,0,0.04) | Bottom sheets, modals |
| `pop` | 0/-8/32 rgba(0,0,0,0.12) | 0/-4/12 rgba(0,0,0,0.04) | Tooltips, popovers |

**Action R-4.5** : créer `app/src/main/java/tn/turbodrive/core/designsystem/tokens/AppShadow.kt` avec `Modifier.shadow*()` extension functions.

---

## 8. Icons v1 → v2

**91 icônes SVG** à porter depuis `turbodrive_redesign/` en R-4.3.

État actuel : la plupart des écrans utilisent `androidx.compose.material.icons.*` (icônes Material standard). Remplacement progressif par les SVG custom.

Inventaire détaillé : voir R-4.3 (hors scope R-4.1).

---

## 9. Plan d'application par phase R-4.x

| Phase | Scope | Dépendances |
|---|---|---|
| **R-4.1** (cette phase) | Baseline snapshots + ce doc | R-2.3 (tokens nettoyés) |
| **R-4.2** | Inter fonts → AppTypography | — |
| **R-4.3** | 91 SVG icons → AppIcon | turbodrive_redesign/ |
| **R-4.4** | 5 nouveaux composants v2 + décomposer écrans complexes (Onboarding, Phone, etc.) en sous-Layouts snapshottables | R-4.2, R-4.3 |
| **R-4.5** | Rename tokens v1 → v2 selon §2-7 ci-dessus + create AppShadow + dissoudre MapColorTokens | R-4.4 |
| **R-4.6** | Detekt rule `ForbiddenColorLiteral` + cleanup tokens deprecated | R-4.5 |
| **S5 (R-5.x)** | Refonte Map + Rider Home avec tokens v2 | R-4.5 |
| **S6 (R-6.x)** | Refonte Driver Home + Wallet/Profile/Settings avec tokens v2 | R-4.5 |

---

## 10. Vérification baseline

```bash
# Reproduire les baselines
./gradlew :app:recordPaparazziDebug

# Valider qu'aucune régression visuelle
./gradlew :app:verifyPaparazziDebug

# Compter les PNG baseline
ls app/src/test/snapshots/images/ | grep "BaselineTest" | wc -l
# → 10 (5 écrans × 2 thèmes)
```

Drift visuel attendu après chaque phase R-4.x — re-record après validation manuelle.

---

## Notes finales

- Le baseline v1 est **frozen** au commit qui suit R-4.1. Toute modification ultérieure doit être justifiée et validée par diff Paparazzi.
- Le doc sera mis à jour à la fin de chaque R-4.x pour cocher les actions effectuées.
- Les 6 écrans non snapshottés en v1 seront couverts au plus tard en R-4.4 (décomposition) ou explicitement marqués hors scope si le coût dépasse le bénéfice.
