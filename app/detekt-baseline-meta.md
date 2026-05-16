# Detekt baseline — Residual debt meta

> Compagnon de `app/detekt-baseline.xml`. Documente la dette parkée lors de la phase
> **R-0.1** (mise en place gates ktlint/detekt avec `maxIssues: 0` et baseline figée)
> et **planifie sa résorption**. Toute NOUVELLE violation détectée hors baseline casse
> le build via `maxIssues: 0` (`detekt.yml:8`). L'existant ici est destiné à fondre au
> fil des sprints, pas à perdurer.

## Snapshot

- **Date de création** : 2026-05-16 (commit `e202952` "Update project structure" pour la
  baseline initiale ; ce fichier meta créé en post-audit S0).
- **Total findings figés** : **249** (`grep -c '<ID>' app/detekt-baseline.xml`).
- **Configuration source** :
  [`detekt.yml`](../detekt.yml) (rules) + [`app/build.gradle.kts:149-154`](build.gradle.kts) (plugin wiring).

## Top catégories (≥ 5 occurrences)

| # | Catégorie | Count | Phase de résorption visée | Date butoir | Owner |
|---|---|---:|---|---|---|
| 1 | `MagicNumber` | 150 | **R-2.3** — design tokens + extraction constants | fin sprint **S2** | — |
| 2 | `UnusedParameter` | 25 | **R-2.2** — nettoyage signatures + suppression code mort | fin sprint **S2** | — |
| 3 | `LongParameterList` | 11 | **R-2.4** — introduction `data class` payloads (`DriverSetupPayload`, `RidePayload`…) | fin sprint **S2** | — |
| 4 | `TooGenericExceptionCaught` | 10 | **R-1.x** — error envelope (`ApiResponse<T>` + typed exceptions) | fin sprint **S1** | — |
| 5 | `UnusedPrivateMember` | 7 | **R-2.2** — nettoyage code mort | fin sprint **S2** | — |

## Catégories restantes (< 5 occurrences chacune)

| Catégorie | Count | Phase visée | Notes |
|---|---:|---|---|
| `UnusedPrivateProperty` | 6 | R-2.2 | Idem `UnusedPrivateMember` |
| `TooManyFunctions` | 6 | R-5.x (refactor Composables / VM splits) | Souvent symptôme de god-class |
| `CyclomaticComplexMethod` | 6 | R-5.x | Idem |
| `MatchingDeclarationName` | 5 | R-2.2 | Renommage fichier ou top-level |
| `LoopWithTooManyJumpStatements` | 5 | R-5.x | Refactor loops |
| `LongMethod` | 4 | R-5.x | Idem `TooManyFunctions` |
| `NestedBlockDepth` | 3 | R-5.x | Refactor flux |
| `ReturnCount` | 2 | À traiter au cas par cas | 1 cas déjà adressé en R-0.2 (`@Suppress("ReturnCount")` justifié dans `DriverSetupScreen.onFooterClick`) |
| `ComplexCondition` | 2 | R-5.x | Extract conditions to named vars |
| `SwallowedException` | 1 | R-1.x | Lié à l'effort error envelope |
| `LargeClass` | 1 | R-5.x | `MapViewModel` — splitting prévu |
| `ImplicitDefaultLocale` | 1 | R-2.2 | `String.format` explicit Locale |
| `FunctionParameterNaming` | 1 | R-2.2 | `_distanceKm` → renommer |
| `FunctionOnlyReturningConstant` | 1 | R-2.2 | Inline ou marquer `const` |
| `ForbiddenComment` | 1 | R-2.2 | `// TODO: notifications` à résoudre ou à tagger ticket |
| `ForEachOnRange` | 1 | R-2.2 | `1..5` → `repeat(5)` |

## Règles d'usage

1. **Toute nouvelle violation NON présente dans la baseline = build cassé**. C'est par
   construction de `maxIssues: 0` + `buildUponDefaultConfig = true`.
2. **On NE rallonge JAMAIS la baseline** sans décision explicite documentée dans ce
   fichier + ACTION_PLAN.md. Ajouter une entrée à la baseline = avouer qu'on parke un
   bug. Privilégier `@Suppress` ciblé avec rationale inline (cf. R-0.2 commit `2619290`).
3. **On RACCOURCIT la baseline** par phase : à chaque clôture de sprint Sx, retirer du
   baseline les findings résorbés (vérifier par `./gradlew detekt --auto-correct` puis
   reconstruction sélective via `./gradlew detektBaseline`).
4. **À la fin du sprint S3** : la baseline devrait être réduite à < 30 findings, tous
   structurels (`LargeClass` MapViewModel, etc.).

## Liens

- Configuration detekt : [`detekt.yml`](../detekt.yml)
- Plan global : [`ACTION_PLAN.md`](../../ACTION_PLAN.md)
- Règles projet : [`rules.md`](../../rules.md) §19 (gate strict)
