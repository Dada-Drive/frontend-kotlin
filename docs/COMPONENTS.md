# Design System v2 — Components Catalog

Composants Compose disponibles pour les écrans S5+ (refonte redesign).
Tous les composants sont dans `app/src/main/java/tn/turbodrive/presentation/components/designsystem/`.

## Foundation

| Token | Source | Description |
|---|---|---|
| `AppTypography.*` | `core/theme/AppTypography.kt` (R-4.2) | Inter 14 styles : displayLarge/Medium, headingL/M/S, bodyL/M/S, labelL/M/S, button, bodyStrong, smallStr, monoL/M |
| `LocalAppColors.current.*` | `core/theme/AppColors.kt` | 48 couleurs sémantiques : primary, onPrimary, surface, textPrimary… |
| `AppSpacing.*` | `core/designsystem/spacing/AppSpacing.kt` | xxs–xxxxl, cardRadius=16dp, buttonRadius=999dp, screenHorizontal=20dp |
| `AppRadius.*` | idem | s=6dp, m=12dp, l=20dp, full=999dp |
| `AppIconSize.*` | idem | s=16dp, m=24dp, l=32dp, xl=48dp |
| `AppMotion.*` | `core/designsystem/tokens/AppMotion.kt` | DURATION_FAST_MS=150, DURATION_NORMAL_MS=250, DURATION_SLOW_MS=400 |
| `AppIcon.*` | `core/designsystem/tokens/AppIcon.kt` (R-4.3) | 94 vector drawables Lucide-based — `painterResource(AppIcon.xxx)` |

---

## Components (R-4.4)

### ServiceCategoryTile

Tuile de sélection d'un type de service (taxi, livraison, premium).
Source : `turbodrive_redesign/screens-categories.jsx`.

**API** :
```kotlin
ServiceCategoryTile(
    icon: Painter,             // painterResource(AppIcon.car)
    label: String,             // "Taxi"
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
)
```

**Usage** :
```kotlin
ServiceCategoryTile(
    icon = painterResource(AppIcon.car),
    label = stringResource(R.string.service_taxi),
    isSelected = currentService == ServiceType.Taxi,
    onClick = { onServiceSelect(ServiceType.Taxi) },
)
```

**Visuels** : 104×138dp, radius 12dp. Default = border 1dp neutre + ombre légère. Selected = fond primary teinté (8% alpha) + border 1.5dp primary + icon/label primary. Disabled = 40% alpha.

**Tests** : `ServiceCategoryTileBaselineTest` — 3 états × 2 thèmes = 6 PNG.

---

### PriceToggle

Segmented control multi-options (TND/USD, Cash/Wallet, Aujourd'hui/Semaine…).
Source : `turbodrive_redesign/design-system.jsx` (Segmented, L367-391).

**API** :
```kotlin
PriceToggle<T>(
    options: List<T>,          // listOf("TND", "USD")
    selected: T,               // "TND"
    onSelect: (T) -> Unit,
    optionLabel: (T) -> String,// { it }
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
)
```

**Usage** :
```kotlin
PriceToggle(
    options = listOf("TND", "USD"),
    selected = selectedCurrency,
    onSelect = { viewModel.setCurrency(it) },
    optionLabel = { it },
)
```

**Visuels** : 40dp height, pill radius. Active = fond textPrimary (ink) + texte onPrimary, sliding pill 150ms. Inactive = fond surfaceMuted + texte textHint. Disabled = 40% alpha.

**Tests** : `PriceToggleBaselineTest` — 3 états × 2 thèmes = 6 PNG.

---

### PriceStepper

Stepper numérique tarif avec boutons pill −/+ pour la négociation chauffeur.
Source : `turbodrive_redesign/screens-driver.jsx` (PriceStepper, L350-414).

Différent de `DesignStepper` (Int + IntRange générique) : Double, step configurable, suffix "TND", tooltip boundary hint.

**API** :
```kotlin
PriceStepper(
    value: Double,
    onValueChange: (Double) -> Unit,
    min: Double,
    max: Double,
    step: Double = 1.0,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    formatter: (Double) -> String = { "%.0f".format(it) },
    suffix: String = "TND",
    boundaryHint: String? = null,   // affiché quand value == min ou max
)
```

**Usage** :
```kotlin
PriceStepper(
    value = negotiatedFare,
    onValueChange = { viewModel.setFare(it) },
    min = 5.0,
    max = estimatedFare * 1.5,
    boundaryHint = if (atLimit) "Limite ± 1 TND" else null,
)
```

**Visuels** : boutons 78×44dp pill. Prix 28sp bold + suffix 14sp 600wt. Bouton disabled = surfaceMuted bg + textDisabled. Boundary hint = petite pill textPrimary bg au-dessus.

**Tests** : `PriceStepperBaselineTest` — 4 états × 2 thèmes = 8 PNG (default, atMin, atMax, disabled).

---

### LinearProgressTimer

Barre de progression linéaire 4dp pour la validité 30s d'une offre (drain) ou le cooldown post-envoi (fill).
Source : `turbodrive_redesign/screens-driver.jsx` (SentState/CooldownState, L479-560).

> **Note planning R-4.4** : Le brief initial parlait d'un timer circulaire. La source redesign utilise uniquement des barres linéaires. Choix confirmé par l'utilisateur : LinearProgressTimer (barre 4dp).

**API** :
```kotlin
LinearProgressTimer(
    durationMs: Long,
    modifier: Modifier = Modifier,
    isRunning: Boolean = true,
    fillFromStart: Boolean = false,  // false = drain (offre), true = fill (cooldown)
    onTick: ((remainingMs: Long) -> Unit)? = null,
    onFinish: () -> Unit = {},
    progressOverride: Float? = null, // test-only, fige la barre
)
```

**Usage** :
```kotlin
LinearProgressTimer(
    durationMs = 30_000L,
    onFinish = { viewModel.onOfferExpired(offer.id) },
    modifier = Modifier.fillMaxWidth(),
)
```

**Couleur** : ≥ 40% → successGreen, 20–40% → warningOrange, < 20% → errorRed (transition 250ms).
Texte : secondes restantes en `monoM/labelS` à droite.

**Tests** : `LinearProgressTimerBaselineTest` — 3 états × 2 thèmes = 6 PNG (start 100%, mid 50%, end 5%).

---

### StackedOffersList

Liste scrollable de cartes d'offres de course pour le flux chauffeur.
Source : `turbodrive_redesign/screens-rider.jsx` (S16Offers + OfferCard, L705-790).

> **Note planning R-4.4** : Le brief décrivait une pile swipeable (Tinder-like). La source redesign utilise une liste verticale scrollable. Choix confirmé par l'utilisateur : LazyColumn. Le nom "Stacked" est conservé pour la traçabilité ACTION_PLAN.

**Model** :
```kotlin
data class RideOffer(
    val id: String,
    val pickupAddress: String,
    val dropoffAddress: String,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val fare: Double,
    val validityRemainingMs: Long = 30_000L,
)
```

**API** :
```kotlin
StackedOffersList(
    offers: List<RideOffer>,
    onAccept: (RideOffer) -> Unit,
    onReject: (RideOffer) -> Unit,
    modifier: Modifier = Modifier,
    emptyContent: @Composable () -> Unit = { DefaultOffersEmptyState() },
)
```

**Usage** :
```kotlin
StackedOffersList(
    offers = availableRides.map { it.toRideOffer() },
    onAccept = { viewModel.acceptOffer(it) },
    onReject = { viewModel.rejectOffer(it) },
)
```

**Chaque OfferCard** : pickup → dropoff, distance + ETA, fare en gros, LinearProgressTimer 30s (drain), boutons Refuser (outline 1 part) + Accepter · {fare} TND (solid 2 parts).

**Tests** : `StackedOffersListBaselineTest` — 4 états × 2 thèmes = 8 PNG (empty, single, three, five).

---

## Composants existants (design system)

| Composant | Fichier | API rapide |
|---|---|---|
| `PrimaryButton` | `core/designsystem/components/PrimaryButton.kt` | `(text, onClick, isLoading, enabled)` |
| `SecondaryButton` | `core/designsystem/components/SecondaryButton.kt` | `(text, onClick)` |
| `AppTextField` | `core/designsystem/components/AppTextField.kt` | `(value, onValueChange, label, …)` |
| `DesignCard` | `DesignSurfaces.kt` | wrapper Surface avec cardRadius + AppShadow |
| `DesignChip` | `DesignSurfaces.kt` | `(label, selected, onClick, accent)` |
| `DesignToggle` | `DesignSurfaces.kt` | Switch boolean `(checked, onCheckedChange, onlineStyle)` |
| `DesignStepper` | `DesignSurfaces.kt` | Int stepper générique `(value, onValueChange, range)` |
| `DesignBottomSheet` | `DesignSurfaces.kt` | Modal bottom sheet wrapper |
| `LoadingDotsSpinner` | `LoadingDotsSpinner.kt` | Animated dots loading indicator |
| `SkeletonViews` | `SkeletonViews.kt` | Shimmer skeleton composables |
