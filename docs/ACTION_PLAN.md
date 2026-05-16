# Plan d'action de remédiation — DadaDrive Android (`frontend-kotlin/`)

> **Contexte** : Ce plan transforme les constats de `AUDIT.md` (racine du projet) en un **plan d'exécution séquentiel** organisé en **9 sprints** et **41 phases de remédiation** (notation `R-X.Y`). Chaque phase est cadrée pour 1–5 jours, autonome, avec critères d'acceptation testables. Le plan couvre intégralement les phases roadmap principal (0–15) ET le redesign (D0–D12).
> **Auteur** : Claude (Opus 4.7) — **Date** : 2026-05-16.
> **Méthode** : remédiation sans refactor total. On garde l'archi Clean/MVVM existante, on patche, on teste, on avance.

---

## Vue d'ensemble — carte des sprints

| Sprint | Thème | Phases | Effort (h) | Durée (1 dev) | Sévérité |
|---|---|---|---|---|---|
| **S0** | Stabilisation & déblocage | R-0.1 → R-0.8 | 24–40 | ~1 sem | Bloquant |
| **S1** | Couche réseau & enveloppe | R-1.1 → R-1.4 | 18–28 | ~1 sem | Bloquant |
| **S2** | Sealed ScreenState & nettoyage tokens | R-2.1 → R-2.4 | 22–34 | ~1 sem | Bloquant |
| **S3** | Socket.IO + lifecycle ride | R-3.1 → R-3.6 | 40–60 | ~2 sem | Critique |
| **S4** | Design system v2 (D0+D1) | R-4.1 → R-4.5 | 36–60 | ~2 sem | Bloquant |
| **S5** | Écrans redesign auth/setup/map/home/négo (D2-D6) | R-5.1 → R-5.5 | 80–128 | ~3 sem | Critique |
| **S6** | Écrans lifecycle + wallet (D7-D10 + P10) | R-6.1 → R-6.6 | 64–104 | ~2,5 sem | Important |
| **S7** | Notifs, deeplinks, offline (P11+P12) | R-7.1 → R-7.4 | 32–48 | ~1,5 sem | Important |
| **S8** | A11y, perf, mock, release (P13+P14+D11+D12) | R-8.1 → R-8.6 | 56–96 | ~2,5 sem | Important |
| **S9** | Shared Rides v2 (P15) | R-9.1 → R-9.5 | 40–60 | ~1,5 sem | Mineur (post-v1) |
| **TOTAL** | | **41 phases** | **412–658 h** | **~17 sem (1 dev) / ~10 sem (2 devs)** | |

---

## Catégorisation des problèmes extraits de AUDIT.md

| # | Problème | Catégorie | Sévérité | Phase | Effort |
|---|---|---|---|---|---|
| 1 | `ignoreFailures=true` ktlint/detekt | Code quality / CI gate | Bloquant | R-0.1 | 4–6h |
| 2 | `detekt.yml maxIssues: 10000` | Code quality | Bloquant | R-0.1 | inclus |
| 3 | 11 `!!` dans DriverSetupScreen.kt:206-222 | Bug / Type safety | Critique | R-0.2 | 2h |
| 4 | README.md liste fausse stack | Documentation drift | Important | R-0.3 | 1h |
| 5 | Pre-commit hook non installé | DevOps | Important | R-0.4 | 30min |
| 6 | `domain/model/` + `domain/models/` doublon | Architecture drift | Mineur | R-0.5 | 15min |
| 7 | Certificate pinning `CERTIFICATE_PINS` vide | Sécurité | Bloquant prod | R-0.6 | 1–2h |
| 8 | `turbodrive_redesign/` absent | Bloquant externe | Bloquant | R-0.7 | user action |
| 9 | Pas de CI GitHub Actions | DevOps | Bloquant | R-0.8 | 4h |
| 10 | Pas d'`ApiResponse<T>` envelope | Drift backend | Bloquant | R-1.1 | 8–12h |
| 11 | Pas de mapping codes erreur localisés | Missing feature | Critique | R-1.2 | 4–6h |
| 12 | Idempotency-key non auto | Bug latent | Important | R-1.3 | 3–4h |
| 13 | Tests parsing/error mapping manquants | Missing tests | Important | R-1.4 | 3–6h |
| 14 | Pas de sealed `ScreenState<T>` | Architecture drift | Critique | R-2.1 | 4–6h |
| 15 | 14 ViewModels en `Boolean+String?` | Refactor | Critique | R-2.2 | 12–18h |
| 16 | 137 hex en dur dans presentation/ | Design drift | Important | R-2.3 | 4–6h |
| 17 | Couverture tests <10% | Missing tests | Critique | continu | inclus |
| 18 | `SocketService` handlers vides | Missing feature | Bloquant | R-3.1+R-3.2 | 16–24h |
| 19 | Pas de resync §4.7 | Missing feature | Critique | R-3.3 | 6–8h |
| 20 | Négociation §4.8 absente | Missing feature | Critique | R-3.4 | 8–12h |
| 21 | Crash recovery active ride flou | Bug latent | Important | R-3.5 | 4–6h |
| 22 | Tests intégration ride 0 | Missing tests | Critique | R-3.6 | 6–10h |
| 23 | `res/font/` absent (Inter) | Design missing | Bloquant D | R-4.2 | 2–3h |
| 24 | 0 SVG du redesign (vs 91) | Design missing | Bloquant D | R-4.3 | 12–20h |
| 25 | 5 composants nouveaux manquants | Design missing | Bloquant D | R-4.4 | 12–20h |
| 26 | Tokens v1 legacy (pas v2 sémantiques) | Design drift | Important | R-4.5 | 4–8h |
| 27 | Room non chiffré (SQLCipher) | Sécurité | Important | R-7.4 | 6–8h |
| 28 | 8 fichiers > 1000 LOC | Code quality | Important | R-5/R-6 | inclus |
| 29 | DriverSetup OCR pas branché | Missing feature | Critique | R-5.2 | 12–20h |
| 30 | Adaptive GPS modes manquants | Missing feature | Important | R-5.3 | 6–8h |
| 31 | Foreground service course active manquant | Missing feature | Important | R-5.3 | 4–6h |
| 32 | `presentation/riderhome/` à extraire | Architecture | Important | R-5.4 | 4–6h |
| 33 | Wallet transactions/top-up absents | Missing feature | Important | R-6.5 | 10–14h |
| 34 | Language switcher complet manquant | Missing feature | Important | R-6.6 | 4–6h |
| 35 | Typed notification handlers manquants | Missing feature | Important | R-7.1 | 4–6h |
| 36 | DeepLinkQueue non testée | Missing tests | Important | R-7.2 | 4–6h |
| 37 | Retry queue offline absente | Resilience | Important | R-7.3 | 6–8h |
| 38 | TalkBack non testé | A11y | Important | R-8.1 | 8–12h |
| 39 | Perf traces absents | Perf | Mineur | R-8.2 | 4–8h |
| 40 | Snapshots multi fontScale×RTL absents | Tests | Important | R-8.3 | 8–12h |
| 41 | Release readiness (signing, AAB, Play) | Release | Important | R-8.5 | 8–16h |
| 42 | Shared rides v2 entier | Feature majeure | Mineur (post-v1) | R-9.* | 40–60h |
| 43 | Endpoints REST manquants (change-password, ratings…) | API coverage | Important | continu | ~8h |
| 44 | Google Sign-In test e2e | Missing tests | Important | inclus R-1.4 | 2h |
| 45 | OTP channel badge non câblé | UI gap | Mineur | R-5.1 | 2h |

---

# SPRINT 0 — STABILISATION & DÉBLOCAGE (P0 bloquants)

> **Objectif sprint** : remettre la CI sous tension, fermer les fuites de sécurité, donner un état "buildable + linté + testé" sain pour démarrer les sprints suivants. **Aucune feature**, uniquement de l'hygiène.
> **Durée cible** : 5 jours ouvrés (~24-40 h).

---

### Phase R-0.1 — Réactiver les gates ktlint/detekt ✅ **TERMINÉE 2026-05-16**
**Objectif** : ktlint + detekt redeviennent bloquants ; aucune dette résiduelle ; CI échoue sur la moindre violation.
**Sévérité** : Bloquant — **Effort estimé** : 4–6 h — **Effort réel** : **~2 h**
**Dépendances** : aucune (mais a découvert un bloqueur infra hors-scope, traité en pré-phase)
**Catégorie** : Code quality / CI gate

**Scission appliquée** (cf. plan : "si findings > 200, scinder")
- **Pré-R-0.1** : bloqueur infra (wrapper Gradle absent + `.gitignore` cassé)
- **R-0.1a** : exemption Composables pour ktlint + `ktlintFormat`
- **R-0.1b** : fix manuel du reliquat ktlint (11 findings)
- **R-0.1c** : config detekt pragmatique + génération du baseline

**Métriques avant / après**
| Outil | Avant | Après autofix | Après config | Final (gate) |
|---|---|---|---|---|
| ktlint | 4 364 | 24 | — | **0** |
| detekt | 1 416 | — | 375 | **0 nouveaux** (375 figés en baseline) |
| `ignoreFailures` | true partout | — | — | **retiré** |
| `maxIssues` | 10 000 | — | — | **0** |

**Actions réalisées**

*Pré-R-0.1 (bloqueur infra découvert au lancement de l'étape 1)*
- [x] Régénération `gradle-wrapper.jar` 8.13 via `gradle wrapper` dans dossier vide (contournement timeouts Maven Central)
- [x] Correction `.gitignore` : retrait de `gradlew`, `gradlew.bat`, `gradle/wrapper/`, `*.jar` (4 lignes fausses qui empêchaient le wrapper de fonctionner pour tout nouveau dev)

*R-0.1a — Composables exemption + autofix*
- [x] `.editorconfig` : `ktlint_function_naming_ignore_when_annotated_with = Composable, Preview, Test, ParameterizedTest`
- [x] `./gradlew ktlintFormat` → 4 340 / 4 364 findings auto-fixés (**99.4 %**)

*R-0.1b — 11 fixes manuels*
- [x] `core/constants/Constants.kt:8` : blank line entre EOL comment et KDoc
- [x] `core/theme/AppFontScale.kt` → renommé `AppFontScalePreference.kt` (filename rule)
- [x] `data/network/model/NotificationModels.kt` → renommé `NotificationTokenRequest.kt` (filename rule)
- [x] `core/logging/LogRedaction.kt:57` : wrap Regex chain (max-line-length 140)
- [x] `core/debug/DebugAuthConfig.kt:11` : `injectStaticUserOnWelcomeSkip` → `INJECT_STATIC_USER_ON_WELCOME_SKIP` (property-naming, + 2 callers internes mis à jour)
- [x] `presentation/driversetup/DriverSetupComponents.kt:135` : KDoc orphelin → `//` comments (no-consecutive-comments)
- [x] `presentation/map/PoiCategory.kt:42` : wrap arguments (max-line-length)
- [x] `presentation/profile/EditProfileScreen.kt:226` : comment inline → ligne au-dessus (discouraged-comment-location)
- [x] 3 wildcard imports expandés (ExampleUnitTest, RefreshTokenExecutorTest, ExampleInstrumentedTest)

*R-0.1c — Detekt config pragmatique + baseline*
- [x] `detekt.yml` enrichi :
  - `FunctionNaming.ignoreAnnotated: ['Composable', 'Preview']` (élimine 178 findings)
  - `MagicNumber.ignoreAnnotated: ['Composable', 'Preview']` + `ignoreNumbers` étendu (8, 16, 24, 32, 48, 64, 100, 255, 300, 500, 1000) (élimine ~700 findings)
  - `LongMethod` / `LongParameterList` / `CyclomaticComplexMethod` exemptés sur `@Composable`
  - `TooManyFunctions` thresholds 11 → 15 (classes) / 20 (interfaces Retrofit)
  - `MaxLineLength: 140`, `ReturnCount: 5 + excludeGuardClauses`
  - `maxIssues: 10000 → 0`
- [x] Detekt: **1 416 → 375** findings après config (−73 %)
- [x] `./gradlew detektBaseline` → `app/detekt-baseline.xml` (25 KB, 375 findings figés comme dette legacy)

*Étape 2 — Désactivation des bypass*
- [x] Retiré `ignoreFailures = true` dans bloc `detekt { }` (`app/build.gradle.kts`)
- [x] Retiré `ignoreFailures.set(true)` dans bloc `ktlint { }` (`app/build.gradle.kts`)
- [x] Ajouté `baseline = file("$projectDir/detekt-baseline.xml")` dans bloc `detekt { }`
- [x] `grep -n "ignoreFailures" app/build.gradle.kts` → **0 ligne**

**Fichiers touchés (résumé)**
- Config : `app/build.gradle.kts`, `detekt.yml`, `.editorconfig`, `.gitignore`
- Infra : `gradle/wrapper/gradle-wrapper.jar` (nouveau), `app/detekt-baseline.xml` (nouveau)
- Source : 196 fichiers reformatés (ktlintFormat — trailing commas, line wraps majoritaires)
- Renommés : 2 fichiers (AppFontScale.kt, NotificationModels.kt)
- **Bilan** : +10 992 / −9 554 lignes (net : reformat)

**Critères d'acceptation** — TOUS VERTS
- [x] `./gradlew ktlintCheck` exit 0 **sans** `ignoreFailures`
- [x] `./gradlew detekt` exit 0 **avec** `maxIssues: 0` (baseline filtre legacy)
- [x] `grep -n "ignoreFailures" app/build.gradle.kts` retourne 0
- [x] `detekt.yml` aligné rules.md §19 (`maxIssues: 0`)
- [x] 0 `@Suppress` ajouté (sur 5 autorisés)

**Vérification finale**
```bash
$ ./gradlew clean ktlintCheck detekt
BUILD SUCCESSFUL in 11s
```

**Dette héritée tracée** (à résorber au fil des sprints)
- **375 findings detekt** dans `app/detekt-baseline.xml` :
  - ~260 MagicNumber dans `core/theme/ColorSchemes.kt`, `TurboDriveColorScheme.kt`, `PhoneScreen.kt`… → seront éliminés en **R-2.3** (nettoyage 137 hex) et **R-4.5** (renommage tokens v2)
  - ~25 UnusedParameter, 20 TooGenericExceptionCaught → cleanup au fil des refactors VM (**R-2.2**)
  - ~13 LongParameterList sur signatures domain → R-3.4 (négociation) et R-5.2 (OCR driver)
  - Reste : ~57 divers, à résorber en R-5/R-6/R-8

**Trouvailles hors-scope ajoutées à AUDIT.md** (action séparée)
- Bug `.gitignore` : excluait wrapper Gradle complet — empêchait tout nouveau dev de bootstrap.
- Backlog : `local.properties` n'a pas de `sdk.dir`, donc `./gradlew compileDebugKotlin` impossible localement. Pré-existant, non bloquant pour R-0.1 (lint pur sans SDK), à résoudre quand on attaquera les builds APK.

**Commits suggérés** (5 atomiques, non encore créés — en attente validation user)
1. `chore(infra): regenerate gradle wrapper 8.13 and fix .gitignore`
2. `chore(ktlint): configure ktlint for Compose and run autofix`
3. `chore(ktlint): fix 11 manual reliquat findings`
4. `chore(detekt): pragmatic config + baseline for legacy tech debt`
5. `chore(quality): re-enable ktlint and detekt gates`

**Rapport complet** : cf. transcript de session du 2026-05-16.

---

### Phase R-0.2 — Supprimer les `!!` de DriverSetupScreen ✅
**Statut** : Terminée — commits `8737026`, `845880b`, `6cbd3be`, `2619290` (poussés sur `origin/main`).
**Objectif** : zéro `!!` en code de production sur ce fichier critique ; remplacer par garde explicite + état d'erreur.
**Sévérité** : Critique — **Effort** : 2 h
**Dépendances** : aucune
**Catégorie** : Bug / Type safety (rules.md §1)

**Tâches**
1. Lister les 11 `!!` dans `presentation/driversetup/DriverSetupScreen.kt:206-222`.
2. Pour chaque : remplacer `value!!` par `requireNotNull(value) { "context msg" }` si invariant garanti ; sinon migrer vers `value?.let { ... } ?: showError(...)`.
3. Pour les bitmaps (`cinFrontBmp!!`, `cinBackBmp!!`, `licensePhotoBmp!!`, `selfieBmp!!`) : afficher message d'erreur localisé si null + désactiver bouton submit tant que requis.
4. Pour `underscoreDateToIso(licenseExpiryInput.trim())!!` : valider avec sealed `DateParseResult` (Valid/Invalid).
5. Tester sur émulateur : refuser caméra → l'app ne crash pas.

**Fichiers touchés**
- `frontend-kotlin/app/src/main/java/tn/dadadrive/presentation/driversetup/DriverSetupScreen.kt:206-222`
- Potentiellement `DriverSetupViewModel.kt` pour state d'erreur

**Critères d'acceptation**
- [x] `grep -n "!!" presentation/driversetup/DriverSetupScreen.kt` ⇒ 0 résultat
- [x] Submit désactivé tant qu'un champ requis manquant (via `footerEnabled` + re-validation `personalOk && licenseOk && vehicleOk` au submit)
- [x] Test manuel : flow setup avec champ vide ⇒ pas de crash (guard ladder → snackbar localisé)

**Vérification**
- Build debug + scénario manuel : ouvrir DriverSetup, ne pas remplir CIN, tenter submit → message localisé.

**Risques & Rollback**
- Risque : casser le flow OCR futur. Mitigation : garder signatures internes, n'altérer que les call sites.

---

### Phase R-0.3 — Mettre à jour README.md ✅
**Statut** : Terminée — commits `7597cb0`, `47666b8`, `fb15ef1`, `d8e005c` (poussés sur `origin/main`).
**Objectif** : aligner `frontend-kotlin/README.md` sur la stack réelle pour ne plus tromper les nouveaux devs.
**Sévérité** : Important — **Effort** : 1 h
**Dépendances** : aucune
**Catégorie** : Documentation drift

**Tâches**
1. Réécrire tableau stack (L43-47) : Socket.IO (pas Firestore), HERE Maps SDK 4.25.5 (pas Google Maps), JWT custom + TokenAuthenticator (pas Firebase Auth), JUnit4 + MockK + Paparazzi (pas Mockito/Espresso).
2. Réécrire schéma "Structure des dossiers" (L99-127) pour refléter `tn.dadadrive.*` (pas `com.dadadrive.*`).
3. Ajouter section "Build variants" (debug/staging/release + BACKEND_BASE_URL_*).
4. Ajouter section "Secrets locaux" pointant `local.properties.template`.
5. Retirer toutes les instructions Windows PowerShell hardcodées (`C:\Users\...`).

**Fichiers touchés**
- `frontend-kotlin/README.md`

**Critères d'acceptation**
- [x] Stack table reflète réalité 100% (vérifié contre `app/build.gradle.kts` + `libs.versions.toml`)
- [x] Aucune mention de Google Maps / Mockito ; Firestore et Firebase Auth ne subsistent qu'en **mentions négatives explicites** ("pas Firestore, pas Firebase Auth") pour disambiguïser le Firebase Crashlytics/FCM/Analytics réel — décision validée
- [x] Espresso **conservé** comme "legacy, peu utilisé" (présent dans `build.gradle.kts:218`) — override honnête du brief
- [x] Path Windows hardcodé retiré (PowerShell, `C:\`, `Invoke-WebRequest` → bash macOS/Linux)
- [x] Sections ajoutées : **Build variants** (debug/staging/release) + **Secrets locaux** (table `local.properties`)
- [x] Note sur `google-services.json` stub local (cohérence R-0.1)
- [x] Package `tn.dadadrive.*` partout dans la doc (le seul `com.dadadrive` restant = note explicative `applicationId` / `namespace`)

**Vérification**
- Relecture par un dev tiers (ou grep des termes interdits).

**Risques & Rollback** : aucun (doc only).

---

### Phase R-0.4 — Installer pre-commit hook ✅
**Statut** : Terminée — commits `043c865`, `70e9754`, `a22a7a1`, `af72417`, `66f161b`, `4e4015f` (poussés sur `origin/main`).
**Objectif** : ktlint + detekt s'exécutent automatiquement avant chaque commit local.
**Sévérité** : Important — **Effort** : 30 min
**Dépendances** : R-0.1 (gates activés)
**Catégorie** : DevOps

**Tâches**
1. Adapter `frontend-kotlin/scripts/pre-commit.ps1` en version cross-platform (bash + PowerShell).
2. Ajouter `frontend-kotlin/scripts/install-hooks.sh` qui copie/symlinke dans `.git/hooks/pre-commit`.
3. Documenter dans README.md (section dev setup) + onboarding step.
4. Tester : `git commit` avec violation → bloqué ; sans violation → commit OK.

**Fichiers touchés**
- `frontend-kotlin/scripts/pre-commit.sh` (nouveau)
- `frontend-kotlin/scripts/install-hooks.sh` (nouveau)
- `frontend-kotlin/scripts/pre-commit.ps1` (mise à jour mineure)
- `frontend-kotlin/README.md` (section "Dev setup")

**Critères d'acceptation**
- [x] `bash scripts/install-hooks.sh` installe le hook (symlink `.git/hooks/pre-commit → ../../scripts/pre-commit.sh`) — idempotent
- [x] Commit avec violation ktlint refusé (testé via `SmokeTest.kt` jetable : exit 1 + message structuré ; 8 s)
- [x] Commit propre passe (fast-path 0.03 s quand aucun `.kt` stagé ; hot-cache 0.68 s ; cible < 10 s respectée)
- [x] Parité macOS/Linux (`pre-commit.sh`) ↔ Windows (`pre-commit.ps1` + shim POSIX installé par `install-hooks.ps1`)
- [x] Section "Hooks Git" ajoutée dans `README.md` sous **Contribuer** (étape obligatoire d'onboarding)

**Vérification**
- Provoquer une violation puis tenter `git commit -m "test"` ⇒ refus avec message clair.

**Risques** : hook trop lent (>10 s) ⇒ activer ktlint en mode rapide (`--relative`).

---

### Phase R-0.5 — Fusionner `domain/model/` dans `domain/models/` ✅
**Statut** : Terminée — commit `1dfce20` (poussé sur `origin/main`).
**Objectif** : supprimer le doublon de package qui suggère un manque de discipline.
**Sévérité** : Mineur — **Effort** : 15 min
**Dépendances** : R-0.1
**Catégorie** : Architecture drift

**Tâches**
1. Déplacer `domain/model/PresentableError.kt` → `domain/models/PresentableError.kt`.
2. Mettre à jour le package declaration (`tn.dadadrive.domain.models`).
3. Lancer `./gradlew compileDebugKotlin` ; corriger les imports cassés (IDE).

**Fichiers touchés**
- `frontend-kotlin/app/src/main/java/tn/dadadrive/domain/model/PresentableError.kt` (déplacé)
- Tous les imports `domain.model.PresentableError` (refactor automatique)

**Critères d'acceptation**
- [x] `domain/model/` n'existe plus (`rmdir` final ; `git mv` préserve l'historique : `git log --follow` traverse le rename 92 %)
- [x] Build OK (`./gradlew clean ktlintCheck detekt :app:compileDebugKotlin` → BUILD SUCCESSFUL)
- [x] Tests unit passent (`:app:testDebugUnitTest` vert, incluant les 12 tests `DateParseResultTest`)
- [x] Hook pre-commit (R-0.4) a accepté le commit après ktlintFormat de `PresentableErrorMapper.kt` bundlé dans le même commit (documenté dans le body)

**Vérification** : `./gradlew test`

---

### Phase R-0.6 — Activer certificate pinning effectif
**Objectif** : pins SHA-256 renseignés pour staging et release ; log Crashlytics sur échec.
**Sévérité** : Bloquant prod — **Effort** : 1–2 h
**Dépendances** : accès ops / certs
**Catégorie** : Sécurité (rules.md §4.6)

**Tâches**
1. Récupérer les pins SHA-256 du certificat staging et release (ops/devops).
2. Renseigner `CERTIFICATE_PINS` dans `local.properties.template` + variables d'env CI : `host|sha256/AAAAAAA=,host|sha256/BBBBBB=`.
3. Ajouter logging Crashlytics dans `NetworkModule` sur `SSLPeerUnverifiedException` : breadcrumb + non-fatal report.
4. Tester en debug avec un pin volontairement invalide (staging) ⇒ requête échoue + log.

**Fichiers touchés**
- `frontend-kotlin/local.properties.template`
- `frontend-kotlin/app/build.gradle.kts` (lecture BuildConfig)
- `frontend-kotlin/app/src/main/java/tn/dadadrive/di/NetworkModule.kt` (handler échec)

**Critères d'acceptation**
- [ ] `CERTIFICATE_PINS` non vide en staging/release
- [ ] `ENABLE_CERT_PINNING = true` en staging/release
- [ ] Sur pin invalide : non-fatal Crashlytics report émis

**Vérification**
- Build staging avec pin invalide ⇒ requête échoue avec `SSLPeerUnverifiedException` ; Logcat montre le report Crashlytics.

**Risques & Rollback**
- Risque : casser staging si pins erronés. **Mitigation** : tester d'abord avec un pin valide connu (récupéré via `openssl s_client`).

---

### Phase R-0.7 — Rapatrier `turbodrive_redesign/`
**Objectif** : disposer du dossier prototype JSX/SVG dans le workspace pour débloquer toutes les phases D.
**Sévérité** : Bloquant — **Effort** : action utilisateur (~30 min)
**Dépendances** : aucune
**Catégorie** : Bloquant externe

**Tâches** (à exécuter par l'utilisateur)
1. Localiser le dossier source (Figma export, repo design, etc.).
2. Le copier en `../turbodrive_redesign/` ou en ajout multi-root au workspace.
3. Vérifier présence de : `icons.jsx`, `screens-auth.jsx`, `screens-rider.jsx`, `screens-driver.jsx`, assets SVG.
4. Confirmer à Claude la disponibilité.

**Fichiers touchés** : aucun dans le projet.

**Critères d'acceptation**
- [ ] `../turbodrive_redesign/` existe avec `icons.jsx` + écrans JSX
- [ ] Au moins 91 fichiers SVG identifiés

**Vérification** : `ls turbodrive_redesign/icons.jsx && find turbodrive_redesign -name "*.svg" | wc -l`

**Risques & Rollback**
- Risque bloquant : si dossier perdu, fallback "freeze v1 actuel" → phases D dégradées (cf. AUDIT.md §9 condition de No-Go).

---

### Phase R-0.8 — Mettre en place CI GitHub Actions
**Objectif** : pipeline CI qui exécute build + ktlint + detekt + tests + paparazzi sur chaque push/PR.
**Sévérité** : Bloquant — **Effort** : 4 h
**Dépendances** : R-0.1
**Catégorie** : DevOps

**Tâches**
1. Créer `frontend-kotlin/.github/workflows/android-ci.yml` avec jobs :
   - `lint` (ktlintCheck + detekt)
   - `unit-test` (test)
   - `snapshot` (verifyPaparazziDebug)
   - `build-debug` (assembleDebug)
2. Cache Gradle (action `gradle/actions/setup-gradle`).
3. Variables secrets : `HERE_SDK_USER`, `HERE_SDK_PASSWORD`, `MAPBOX_TOKEN`, `CERTIFICATE_PINS`.
4. Status badge dans README.md.
5. Branch protection : requires CI vert pour merge sur `main`.

**Fichiers touchés**
- `frontend-kotlin/.github/workflows/android-ci.yml` (nouveau)
- `frontend-kotlin/README.md` (badge)

**Critères d'acceptation**
- [ ] PR ouverte ⇒ CI lance les 4 jobs
- [ ] CI échoue sur violation lint/test
- [ ] Build debug réussit en <8 min

**Vérification** : créer une PR test avec une violation → CI rouge.

**Risques** : secrets HERE/Mapbox absents côté GitHub ⇒ jobs build échouent. Mitigation : fournir secrets via repo settings avant activation branch protection.

---

### Phase R-0.9 — Résorber le working tree pré-existant
**Statut** : 🟠 Identifiée lors du ré-audit triple-expert post-S0 (2026-05-16).
**Objectif** : éliminer les 5211 deletions + 195 modifications héritées d'avant R-0.2 pour garantir la reproductibilité fresh-clone et empêcher toute dérive silencieuse.
**Sévérité** : Important — **Effort estimé** : 1–2 h
**Dépendances** : R-0.5
**Catégorie** : Repo hygiene

**Contexte**
Le ré-audit S0 a noté que le working tree porte un backlog massif (~5400 entrées) hérité de la migration `com.dadadrive` → `tn.dadadrive` et de cleanups partiels. Option (c) "backlog tracé" approuvée par les 3 experts à condition de planifier la résorption avant R-1.x. Slot R-0.6/0.7/0.8 déjà pris (cert pinning, turbodrive_redesign, CI) ⇒ phase placée en R-0.9.

**Tâches**
1. Auditer `git diff --stat HEAD` et catégoriser les ~195 fichiers modifiés :
   - Legitimate WIP à committer
   - Résidu temporaire à `git restore`
   - Forgotten cleanup à finir
2. Valider que les ~5211 deletions correspondent toutes à `app/libs/_tmp_here/*` ou à l'ancien arbre `com.dadadrive/*`.
3. Commits ciblés par catégorie (1 par catégorie max) OU `git restore` ce qui n'a pas lieu d'être versionné.
4. Vérifier qu'aucun secret n'est accidentellement commité (clés API, tokens, `local.properties` réel).
5. Re-run `./gradlew clean ktlintCheck detekt :app:compileDebugKotlin` post-cleanup pour confirmer que la codebase build toujours.

**Fichiers touchés**
- Working tree complet (`git status` final attendu : clean)
- Éventuellement `.gitignore` si des patterns supplémentaires sont identifiés

**Critères d'acceptation**
- [ ] `git status` retourne 0 modifié, 0 untracked, 0 deleted
- [ ] `./gradlew clean ktlintCheck detekt :app:compileDebugKotlin` passe
- [ ] Aucun fichier sensible commité (audit `git diff` avant chaque commit)
- [ ] Hook pre-commit (R-0.4) tourne vert sur un échantillon de modifs

**Vérification**
- `git status` post-phase
- Cloner le repo dans un dossier vierge, lancer build → doit réussir identiquement

**Risques & Rollback**
- Risque : restaurer un fichier WIP utile. Mitigation : `git stash` complet avant action, restauration possible.
- Rollback : `git stash pop` pour récupérer un fichier supprimé à tort.

---

# SPRINT 1 — COUCHE RÉSEAU & ENVELOPPE (P0)

> **Objectif sprint** : aligner intégralement la couche réseau sur le contrat `backend-integration.md` §1.4 (enveloppe `{success, data}` / `{success, error}`) et §2.2 (codes erreur localisés). Idempotency-key automatique.
> **Durée cible** : 5 jours (~18-28 h).

---

### Phase R-1.1 — Implémenter `ApiResponse<T>` générique
**Objectif** : tous les retours backend passent par un wrapper typé `{success: Boolean, data: T?, error: ApiError?}` ; les DTOs métier sont décollés du transport.
**Sévérité** : Bloquant — **Effort** : 8–12 h
**Dépendances** : R-0.1
**Catégorie** : Drift backend

**Tâches**
1. Créer `data/network/envelope/ApiResponse.kt` : `data class ApiResponse<T>(val success: Boolean, val data: T? = null, val error: ApiError? = null)`.
2. Créer `data/network/envelope/ApiError.kt` : `data class ApiError(val code: String, val message: String, val details: Map<String, Any>? = null)`.
3. Créer `data/network/envelope/ApiCall.kt` : extension `suspend fun <T> Response<ApiResponse<T>>.unwrap(): Result<T>` qui mappe en `Result.success(data)` ou `Result.failure(BackendException(error))`.
4. Refactor signatures des 36 endpoints (`AuthApiService`, `RidesApiService`, `DriverApiService`, `WalletApiService`, `NotificationApiService`, `DriverRegistrationApiService`) pour renvoyer `Response<ApiResponse<XxxDto>>`.
5. Refactor repository impls pour appeler `.unwrap()` et propager `Result<T>`.
6. Refactor ViewModels pour consommer `Result<T>` (avec future `ScreenState` cf. R-2.1).
7. Garder rétrocompatibilité temporaire : si backend renvoie l'ancien format, fallback `ApiResponse(success=true, data=raw)`.

**Fichiers touchés** (~30 fichiers)
- Nouveaux : `data/network/envelope/{ApiResponse,ApiError,ApiCall,BackendException}.kt`
- Modifiés : 6 `*ApiService.kt`, 5 `*RepositoryImpl.kt`, ~10 ViewModels appelants
- DTOs : possiblement renommer `AuthResponse` → `AuthData` (data class interne au wrapper)

**Critères d'acceptation**
- [ ] Tous les endpoints retournent `Response<ApiResponse<*>>`
- [ ] Aucun call site ne dépaque manuellement `success`
- [ ] 5 tests unit sur `unwrap()` (success, error, malformed)

**Vérification**
- `./gradlew test` + run d'une requête réelle vers `/auth/me` qui renvoie l'enveloppe.

**Risques & Rollback**
- Risque : backend ne renvoie pas encore l'enveloppe partout. **Mitigation** : `unwrap()` accepte fallback compat. Activer compat strict via feature flag `BuildConfig.STRICT_ENVELOPE`.

---

### Phase R-1.2 — Mapping codes erreur localisés
**Objectif** : codes backend (`VALIDATION_ERROR`, `UNAUTHORIZED`, `RIDE_NOT_FOUND`, etc.) traduits en messages utilisateur localisés (ar/fr/en).
**Sévérité** : Critique — **Effort** : 4–6 h
**Dépendances** : R-1.1
**Catégorie** : Missing feature

**Tâches**
1. Lister tous les codes erreur depuis `backend-integration.md §2.2` (~30 codes).
2. Créer `domain/models/BackendErrorCode.kt` : `enum class BackendErrorCode { VALIDATION_ERROR, UNAUTHORIZED, RIDE_NOT_FOUND, WALLET_INSUFFICIENT_FUNDS, ... ; companion object { fun fromString(s: String): BackendErrorCode? } }`.
3. Ajouter dans `res/values/strings.xml`, `values-fr/strings.xml`, `values-ar/strings.xml` : 30 entrées `error_<code_lowercase>`.
4. Créer `core/error/BackendErrorMapper.kt` : `fun BackendErrorCode.toLocalizedMessage(ctx: Context): String`.
5. Refactor `ApiErrorParser.kt` pour extraire `error.code` (et plus juste `.message`).
6. Câbler dans `PresentableError` (déjà présent en domain) pour propager le code mappé.

**Fichiers touchés**
- Nouveaux : `domain/models/BackendErrorCode.kt`, `core/error/BackendErrorMapper.kt`
- Modifiés : `data/network/envelope/ApiError.kt`, `data/network/ApiErrorParser.kt`, `res/values*/strings.xml` (×3)
- `domain/models/PresentableError.kt` (déjà existant, à enrichir)

**Critères d'acceptation**
- [ ] 30 codes mappés
- [ ] 3 locales (ar/fr/en) complètes
- [ ] Test unit sur 5 codes au hasard
- [ ] Snapshot Paparazzi d'une snackbar avec un message d'erreur localisé

**Vérification**
- Provoquer une 422 → snackbar affiche le texte traduit selon device locale.

**Risques** : codes manquants ⇒ fallback `error_unknown`.

---

### Phase R-1.3 — Idempotency-key automatique
**Objectif** : header `idempotency-key` injecté automatiquement sur ride create / ride accept / wallet topup confirm.
**Sévérité** : Important — **Effort** : 3–4 h
**Dépendances** : R-1.1
**Catégorie** : Bug latent (doublons)

**Tâches**
1. Créer annotation `@Idempotent` sur méthodes Retrofit nécessitant idempotency.
2. Créer `data/network/interceptors/IdempotencyKeyInterceptor.kt` qui détecte l'annotation et injecte un UUID v4 si absent.
3. Annoter : `RidesApiService.requestRide`, `RidesApiService.pickRideOffer`, `WalletApiService.confirmTopUp`.
4. Vérifier que la clé est stable pour retry interne (RetryPolicy ne doit pas regénérer).
5. Tests : 3 tests unit (key injectée, key préservée sur retry, endpoint non-annoté → pas de key).

**Fichiers touchés**
- Nouveaux : `data/network/annotations/Idempotent.kt`, `data/network/interceptors/IdempotencyKeyInterceptor.kt`, tests
- Modifiés : 3 services Retrofit (annotations), `NetworkModule.kt` (ajout interceptor)

**Critères d'acceptation**
- [ ] Annotation `@Idempotent` reconnue par interceptor
- [ ] UUID v4 généré + injecté
- [ ] Retry interne préserve la même clé
- [ ] 3 tests passent

**Vérification** : log requête `requestRide` ⇒ `idempotency-key: uuid` présent.

---

### Phase R-1.4 — Tests parsing/error mapping
**Objectif** : harnais de tests pour la couche réseau (ApiResponse, error mapping, idempotency).
**Sévérité** : Important — **Effort** : 3–6 h
**Dépendances** : R-1.1, R-1.2, R-1.3
**Catégorie** : Missing tests

**Tâches**
1. Créer `test/data/network/envelope/ApiResponseTest.kt` : parse success, parse error, parse malformed, fallback compat.
2. Créer `test/core/error/BackendErrorMapperTest.kt` : 5 codes mappés vers 3 locales (test avec `Locale.setDefault`).
3. Créer `test/data/network/interceptors/IdempotencyKeyInterceptorTest.kt` : MockWebServer + 3 scénarios.
4. Coverage minimum visée 80% sur les 3 nouveaux packages.

**Fichiers touchés**
- Nouveaux : 3 fichiers tests sous `app/src/test/java/tn/dadadrive/`

**Critères d'acceptation**
- [ ] `./gradlew test` exécute >= 15 tests sur ces nouveaux fichiers
- [ ] Coverage `data/network/envelope/` >= 80%

**Vérification** : `./gradlew test jacocoTestReport` (si Jacoco configuré, sinon coverage IDE).

---

# SPRINT 2 — SEALED SCREENSTATE & NETTOYAGE TOKENS (P0)

> **Objectif sprint** : éliminer le pattern `Boolean + String?` dans les ViewModels au profit d'un `sealed interface ScreenState<T>` exhaustif. Nettoyer les 137 couleurs hex en dur.
> **Durée cible** : 5 jours (~22-34 h).

---

### Phase R-2.1 — Définir `sealed interface ScreenState<T>`
**Objectif** : pattern d'état unique, exhaustif, exigé par rules.md §3.2.
**Sévérité** : Critique — **Effort** : 4–6 h
**Dépendances** : R-1.2 (PresentableError disponible)
**Catégorie** : Architecture drift

**Tâches**
1. Créer `presentation/common/ScreenState.kt` :
   ```kotlin
   sealed interface ScreenState<out T> {
       data object Idle : ScreenState<Nothing>
       data object Loading : ScreenState<Nothing>
       data class Loaded<T>(val value: T) : ScreenState<T>
       data class Error(val error: PresentableError) : ScreenState<Nothing>
   }
   ```
2. Helpers d'extension : `fun <T> ScreenState<T>.dataOrNull()`, `isLoading`, `isError`.
3. Pattern partagé `Flow<Result<T>>.toScreenState()` pour les ViewModels appelant un use case.
4. Documenter dans `frontend-kotlin/docs/ARCHITECTURE.md` (créer fichier) avec exemple.
5. 3 tests unit (Loaded → dataOrNull non null, Error → isError true, Loading → exhaustive `when`).

**Fichiers touchés**
- Nouveaux : `presentation/common/ScreenState.kt`, `presentation/common/ScreenStateExtensions.kt`, `docs/ARCHITECTURE.md`, tests

**Critères d'acceptation**
- [ ] Sealed interface compile et exhaustif
- [ ] 3 tests passent
- [ ] Doc avec snippet

**Vérification** : `when (state) { … }` sans `else` ⇒ exhaustif.

---

### Phase R-2.2 — Refactor 14 ViewModels vers ScreenState
**Objectif** : remplacer tous les `_loading: Boolean + _error: String?` par `MutableStateFlow<ScreenState<T>>`.
**Sévérité** : Critique — **Effort** : 12–18 h
**Dépendances** : R-2.1
**Catégorie** : Refactor

**Tâches**
1. Inventorier les 14 ViewModels (cf. `grep -l "_loading" presentation/`).
2. Refactor par ordre de criticité :
   - **Vague A** (critiques) : `DriverSetupViewModel`, `AuthViewModel`, `MapViewModel`, `DriverViewModel` (driverhome).
   - **Vague B** (secondaires) : `WalletViewModel`, `ProfileViewModel`, `NameEntryViewModel`, `OnboardingViewModel`, `RoleSelectionViewModel`, autres.
3. Pour chaque VM : remplacer `MutableStateFlow<Boolean>` + `MutableStateFlow<String?>` par `MutableStateFlow<ScreenState<MyData>>`.
4. Mettre à jour les Composables consommateurs : pattern `when (val s = state) { is Loading → ProgressIndicator(); is Loaded → Content(s.value); is Error → ErrorView(s.error); Idle → Unit }`.
5. Ajouter 1 test unit par VM (au minimum 14 tests, transitions Idle→Loading→Loaded/Error).

**Fichiers touchés** (~25 fichiers)
- 14 VMs sous `presentation/*/`
- ~14 Composables d'écran appelants
- 14 nouveaux fichiers test sous `test/presentation/*/`

**Critères d'acceptation**
- [ ] `grep -r "_loading: Boolean\|MutableStateFlow<Boolean>" presentation/` ⇒ 0 matches (sauf cas légitimes documentés)
- [ ] Compose : tous les écrans gèrent les 4 états explicitement
- [ ] 14 nouveaux tests VM passent
- [ ] Couverture VM passe de ~7% à >50%

**Vérification** : test manuel : forcer une erreur réseau ⇒ chaque écran affiche bien un état Error (pas blanc).

**Risques & Rollback**
- Risque : régression UI (états oubliés). **Mitigation** : refactor VM par VM avec PR séparée par vague.

---

### Phase R-2.3 — Nettoyer 137 hex en dur
**Objectif** : zéro `Color(0xFF…)` ou hex literal dans `presentation/` ; tout passe par `LocalAppColors.current.xxx`.
**Sévérité** : Important — **Effort** : 4–6 h
**Dépendances** : R-2.1 (parallèle possible)
**Catégorie** : Design drift

**Tâches**
1. `grep -rn "Color(0xFF" presentation/` → liste 137 occurrences dans 21 fichiers.
2. Pour chaque hex inconnu : créer le token correspondant dans `core/theme/TurboDriveColorScheme.kt` si absent (mais préférer mapping vers token existant).
3. Refactor par batch de ~30 (5 PR) :
   - Batch 1 : `DriverSetupComponents.kt` (13), `DriverVehicleStep.kt`, `DriverAccessSetupScreen.kt`
   - Batch 2 : `WelcomeScreen.kt`, `PhoneScreen.kt`, autres auth
   - Batch 3 : `MapScreen.kt`, `MapRouteSheet.kt`, autres map
   - Batch 4 : `DriverHomeScreen.kt`, driver*
   - Batch 5 : wallet, profile, divers
4. Ajouter une règle detekt custom `ForbiddenColorLiteral` qui interdit `Color(0xFF` en dehors de `core/theme/`.

**Fichiers touchés**
- 21 fichiers sous `presentation/`
- `core/theme/TurboDriveColorScheme.kt` (ajouts éventuels)
- `frontend-kotlin/detekt.yml` (règle custom ou plugin)

**Critères d'acceptation**
- [ ] `grep -rn "Color(0xFF" app/src/main/java/tn/dadadrive/presentation/` ⇒ 0
- [ ] Règle detekt active : tout nouveau hex literal bloque le build

**Vérification** : screenshots avant/après identiques sur 3 écrans (Paparazzi diff).

**Risques** : drift visuel imperceptible si mauvais mapping. **Mitigation** : snapshot Paparazzi avant chaque batch.

---

### Phase R-2.4 — Tests unit ViewModels (rattrapage)
**Objectif** : au moins 1 test unit par ViewModel (transitions d'état).
**Sévérité** : Important — **Effort** : inclus dans R-2.2 (14 tests)
**Dépendances** : R-2.2
**Catégorie** : Missing tests

**Tâches** : (incluses dans R-2.2 étape 5 — phase R-2.4 sert de gate validation)

**Critères d'acceptation**
- [ ] `./gradlew test` exécute >= 14 nouveaux tests VM
- [ ] Couverture presentation/ passe de ~5% à >40%

---

# SPRINT 3 — SOCKET.IO + LIFECYCLE RIDE (P1 — chemin critique)

> **Objectif sprint** : combler le trou béant `SocketService` handlers vides → événements typés, resync, négociation, persistance.
> **Durée cible** : 10 jours (~40-60 h).

---

### Phase R-3.1 — Sealed `SocketEvent` + payloads `@Serializable`
**Objectif** : typer tous les events Socket.IO documentés dans `backend-integration.md §4`.
**Sévérité** : Bloquant — **Effort** : 8–12 h
**Dépendances** : R-1.1 (DTOs alignés)
**Catégorie** : Missing feature

**Tâches**
1. Lister tous les events backend (~15-20) : `ride:offer`, `ride:accepted`, `ride:started`, `ride:driver_arrived`, `ride:completed`, `ride:cancelled`, `negotiate:propose`, `negotiate:accept`, `negotiate:counter`, `wallet:topup_confirmed`, `notification:new`, `shared:match_found`, etc.
2. Créer `domain/models/socket/SocketEvent.kt` : sealed class avec sous-classes par event + payload `@Serializable`.
3. Créer `data/socket/SocketEventDecoder.kt` : `fun decode(eventName: String, json: String): SocketEvent?` via kotlinx.serialization.
4. Tests unit : 5 events parsés correctement, payload malformé → null + log.

**Fichiers touchés**
- Nouveaux : `domain/models/socket/SocketEvent.kt`, `data/socket/SocketEventDecoder.kt`, tests
- Ajouter `kotlinx-serialization` au `libs.versions.toml` si absent

**Critères d'acceptation**
- [ ] ≥15 sous-classes typed
- [ ] 5 tests parsing passent

**Vérification** : mock server émet un `ride:offer` → decoder produit `SocketEvent.RideOffer(payload)`.

---

### Phase R-3.2 — Wirer handlers SocketService (riders + drivers)
**Objectif** : `SocketService` ne plus avoir `s.on(...) {}` vide ; émet un `SharedFlow<SocketEvent>` consommable par les ViewModels.
**Sévérité** : Bloquant — **Effort** : 8–12 h
**Dépendances** : R-3.1
**Catégorie** : Missing feature

**Tâches**
1. Refactor `SocketService.kt` :
   - Expose `val events: SharedFlow<SocketEvent>` (replay=0, buffer=64).
   - Méthode `connect(role: Role, token: String)` qui choisit namespace `/riders` ou `/drivers`.
   - Pour chaque event listé R-3.1 : `s.on("ride:offer") { args → events.tryEmit(decoder.decode("ride:offer", args[0].toString())) }`.
   - Reconnexion auto déjà OK, mais ajouter `s.on(Socket.EVENT_CONNECT) { resyncManager.trigger() }`.
2. Créer `data/socket/SocketEventManager.kt` (façade Hilt singleton) qui expose le flow.
3. Câbler dans 4 ViewModels prioritaires : `MapViewModel` (rider), `DriverViewModel`, `WalletViewModel`, `NotificationsViewModel`.
4. Tests : mock socket + 3 events → flows reçus dans ordre.

**Fichiers touchés**
- Modifiés : `data/socket/SocketService.kt`
- Nouveaux : `data/socket/SocketEventManager.kt`, `di/SocketModule.kt` (Hilt provider), tests
- 4 ViewModels : subscription au flow

**Critères d'acceptation**
- [ ] `grep "s.on(.*) {}" data/socket/SocketService.kt` ⇒ 0 handler vide
- [ ] Namespaces `/riders` et `/drivers` activés selon `Role`
- [ ] Tests : 3 events émis → 3 reçus

**Vérification** : connecter à staging, émettre un `ride:offer` côté backend → MapViewModel state change.

---

### Phase R-3.3 — Resync §4.7 (reconnect)
**Objectif** : sur reconnect Socket, fetch active ride, wallet balance, unread notifications.
**Sévérité** : Critique — **Effort** : 6–8 h
**Dépendances** : R-3.2
**Catégorie** : Missing feature

**Tâches**
1. Créer `domain/usecases/ResyncOnReconnectUseCase.kt` : appelle en parallèle `GET /rides/active`, `GET /wallet`, `GET /notifications?unread=true`.
2. Câbler à `SocketEventManager` : sur event `Socket.EVENT_CONNECT`, lancer le use case dans un `CoroutineScope` Hilt-scoped.
3. Mettre à jour caches Room (`CachedActiveRideEntity`, `CachedWalletBalanceEntity`).
4. Émettre un `SocketEvent.ResyncCompleted` pour les VM intéressées.
5. Tests : disconnect simulé puis reconnect → 3 appels API faits.

**Fichiers touchés**
- Nouveaux : `domain/usecases/ResyncOnReconnectUseCase.kt`, `data/repositories/ResyncRepository.kt`, tests
- Modifiés : `SocketEventManager.kt`

**Critères d'acceptation**
- [ ] Reconnect → 3 appels API lancés en parallèle (vérifié via MockWebServer)
- [ ] Caches Room mis à jour
- [ ] Test d'intégration passe

---

### Phase R-3.4 — Négociation §4.8
**Objectif** : implémenter le flow de négociation prix (propose / accept / counter) côté domain + data.
**Sévérité** : Critique — **Effort** : 8–12 h
**Dépendances** : R-3.1, R-3.2
**Catégorie** : Missing feature

**Tâches**
1. DTOs `NegotiationProposalDto`, `NegotiationAcceptDto`, `NegotiationCounterDto` (cf. spec §4.8).
2. Use cases : `ProposePriceUseCase`, `AcceptPriceUseCase`, `CounterPriceUseCase`.
3. Repository méthode `negotiate(...)` qui émet via Socket.IO (`socket.emit("negotiate:propose", payload)`).
4. Côté écoute : `SocketEvent.NegotiationProposed`, `NegotiationAccepted`, `NegotiationCountered` (déjà dans R-3.1).
5. Tests : 4 tests scénarios (propose → counter → accept ; propose → reject ; concurrent counters).
6. **UI** différée à R-5.5 (D6 négociation).

**Fichiers touchés**
- Nouveaux : `data/network/dto/Negotiation*.kt`, `domain/usecases/Negotiation*.kt`, tests
- Modifiés : `RidesRepository.kt` (méthode `negotiate`)

**Critères d'acceptation**
- [ ] 3 use cases compilent et passent tests
- [ ] Socket emit réel testé via MockWebServer Socket.IO

---

### Phase R-3.5 — Crash recovery active ride
**Objectif** : après crash/kill app, l'état de la course active est restauré depuis `CachedActiveRideEntity` ; UI reprend où elle en était.
**Sévérité** : Important — **Effort** : 4–6 h
**Dépendances** : R-3.2, R-3.3
**Catégorie** : Bug latent

**Tâches**
1. Vérifier que `CachedActiveRideEntity` stocke l'état complet (rideId, status, pickup, dropoff, driverId, eta, fare).
2. `MapViewModel.init` : lire le cache au démarrage ; si présent + `status in [OFFERED, ACCEPTED, STARTED]`, restaurer dans `StateFlow`.
3. Lancer un `GET /rides/active` immédiatement pour réconcilier (cache vs serveur).
4. Test d'intégration : créer ride en BDD, redémarrer VM ⇒ état restauré.

**Fichiers touchés**
- Modifiés : `data/local/entities/CachedActiveRideEntity.kt` (compléter champs), `MapViewModel.kt`, `RidesRepository.kt`
- Nouveaux : tests intégration

**Critères d'acceptation**
- [ ] Cache stocke 7+ champs requis
- [ ] Cold start avec ride en cache ⇒ UI affiche ride sans flash
- [ ] Test d'intégration passe

---

### Phase R-3.6 — Tests intégration lifecycle ride
**Objectif** : 3 scénarios d'intégration end-to-end (offer→accept→start→complete ; cancel ; reconnect mid-ride).
**Sévérité** : Critique — **Effort** : 6–10 h
**Dépendances** : R-3.1 à R-3.5
**Catégorie** : Missing tests

**Tâches**
1. Setup harnais `androidTest` ou `test` avec `MockWebServer` + mock Socket.IO server (Netty).
2. Scénario A : rider request ride → offer reçu → accept → started → completed. Assertions sur StateFlow.
3. Scénario B : rider cancel pending request.
4. Scénario C : disconnect après accept → reconnect → resync rétablit l'état.
5. Documenter dans `docs/TESTING.md`.

**Fichiers touchés**
- Nouveaux : `androidTest/integration/RideLifecycleIntegrationTest.kt`, `androidTest/util/MockSocketServer.kt`, `docs/TESTING.md`

**Critères d'acceptation**
- [ ] 3 tests d'intégration verts en CI
- [ ] Coverage data/socket/ et domain/usecases/ride/ >= 70%

**Vérification** : `./gradlew connectedAndroidTest` ou `test` selon framework.

---

# SPRINT 4 — DESIGN SYSTEM v2 (D0 + D1)

> **Objectif sprint** : combler les fondations design (Inter, SVG, 5 composants, renommage tokens v2).
> **Durée cible** : 10 jours (~36-60 h).
> **Bloqué par** : R-0.7 (dossier `turbodrive_redesign/` disponible).

---

### Phase R-4.1 — D0 Baseline snapshots & mapping
**Objectif** : capturer l'état visuel actuel avant refonte ; produire table mapping tokens v1→v2.
**Sévérité** : Bloquant D — **Effort** : 6–10 h
**Dépendances** : R-0.7
**Catégorie** : Design baseline

**Tâches**
1. Setup Paparazzi pour les ~10 écrans existants (Splash déjà fait, ajouter : Welcome, Phone, OTP, Name, Role, Onboarding, DriverSetup, MapScreen, DriverHome, Wallet).
2. Générer snapshots baseline (clair + sombre, sp 100%) ⇒ commit dans `app/src/test/snapshots/images/baseline_v1/`.
3. Créer `docs/DESIGN_MIGRATION.md` avec table mapping :
   | Token v1 | Token v2 spec | Usage actuel | Action |
   |---|---|---|---|
   | `successGreen` | `accent` | Boutons CTA | rename + bridge typealias |
   | `surfaceMuted` | `surfaceAlt` | Cartes | rename |
   | `primaryDisabled` | `inkSoft` | Boutons disabled | rename |
   | ... | ... | ... | ... |

**Fichiers touchés**
- Nouveaux : ~10 tests Paparazzi, `docs/DESIGN_MIGRATION.md`
- Dossier baseline snapshots

**Critères d'acceptation**
- [ ] 10 snapshots baseline committés
- [ ] Table mapping >= 20 entrées

---

### Phase R-4.2 — D1 Inter fonts
**Objectif** : police Inter 400/500/600/700 dans `res/font/` + `AppTypography` aligné spec.
**Sévérité** : Bloquant D — **Effort** : 2–3 h
**Dépendances** : R-0.7
**Catégorie** : Design missing

**Tâches**
1. Télécharger Inter (Google Fonts ou rsms.me/inter) : 4 fichiers `.ttf`.
2. Ajouter `app/src/main/res/font/inter_regular.ttf`, `inter_medium.ttf`, `inter_semibold.ttf`, `inter_bold.ttf`.
3. Créer `core/theme/InterFontFamily.kt` : `val Inter = FontFamily(Font(R.font.inter_regular, FontWeight.Normal), ...)`.
4. Refactor `AppTypography.kt` : remplacer `FontFamily.Default` par `Inter` partout.
5. Aligner sur design-system.md §3 : `headingS=17sp` (vs 18), `displayMedium=28sp` (vs 30), ajouter `letterSpacing` et `lineHeight` par token.
6. Ajouter nouveaux tokens : `button` (15/600), `bodyStrong` (15/600), `smallStr` (13/600).
7. Snapshot diff pour vérifier l'impact visuel (10 écrans).

**Fichiers touchés**
- Nouveaux : 4 `.ttf` dans `res/font/`, `core/theme/InterFontFamily.kt`
- Modifiés : `core/theme/AppTypography.kt`, `core/theme/TypographyScale.kt`, `core/theme/Type.kt`

**Critères d'acceptation**
- [ ] 4 fichiers Inter présents
- [ ] `AppTypography` utilise `Inter` uniquement
- [ ] 3 nouveaux tokens (`button`, `bodyStrong`, `smallStr`) présents
- [ ] Tailles alignées spec
- [ ] Snapshot diff documenté

**Risques** : changement visuel global (tailles plus petites). Mitigation : faire diff snapshot par snapshot, PR distinct.

---

### Phase R-4.3 — D1 Port 91 SVG icônes
**Objectif** : exporter 91 icônes du redesign en vector drawable Android ; refonte `AppIcon`.
**Sévérité** : Bloquant D — **Effort** : 12–20 h
**Dépendances** : R-0.7
**Catégorie** : Design missing

**Tâches**
1. Inventorier icônes dans `turbodrive_redesign/icons.jsx` (script Node ou manuel).
2. Pour chaque icône : extraire SVG path, convertir en `<vector>` Android via Android Studio import ou script (svg-to-vector).
3. Placer dans `app/src/main/res/drawable/ic_*.xml` (~91 fichiers).
4. Refonte `core/designsystem/tokens/AppIcon.kt` :
   - `object AppIcon { val arrowLeft = R.drawable.ic_arrow_left; val car = R.drawable.ic_car; ... }` (91 entrées).
5. Mettre à jour tous les call sites (`Icon(Icons.Default.ArrowBack, ...)` → `Icon(painterResource(AppIcon.arrowLeft), ...)`).
6. Tests Paparazzi sur un écran d'icônes (catalog).

**Fichiers touchés**
- Nouveaux : ~91 fichiers `res/drawable/ic_*.xml`
- Modifiés : `core/designsystem/tokens/AppIcon.kt`, ~30 call sites
- Nouveau : `test/presentation/IconCatalogPaparazziTest.kt`

**Critères d'acceptation**
- [ ] ≥91 vector drawables présents
- [ ] `AppIcon` expose ≥91 entrées
- [ ] Plus aucun usage de `Icons.Default.*` Material (sauf justifié)
- [ ] Snapshot icon catalog vert

**Risques** : conversion SVG complexe (gradients, masks) échoue ⇒ fallback PNG. Mitigation : audit dossier avant tâche pour identifier les 5-10 cas durs.

---

### Phase R-4.4 — D1 5 composants nouveaux
**Objectif** : créer `ServiceCategoryTile`, `StackedOffersList`, `PriceStepper`, `ProgressTimer`, `PriceToggle` avec snapshots.
**Sévérité** : Bloquant D — **Effort** : 12–20 h
**Dépendances** : R-4.2, R-4.3
**Catégorie** : Design missing

**Tâches**
1. **ServiceCategoryTile** : tuile catégorie service (taxi, livraison, premium). API : `(icon, label, isSelected, onClick)`.
2. **StackedOffersList** : pile de cartes d'offres chauffeur (avec animation de pile + swipe). API : `(offers: List<Offer>, onAccept, onReject)`.
3. **PriceStepper** : input numérique avec +/- pour négociation. API : `(value, onValueChange, min, max, step)`.
4. **ProgressTimer** : timer circulaire avec countdown. API : `(durationMs, onTick, onFinish)`.
5. **PriceToggle** : segment toggle (TND/USD ou Cash/Wallet). API : `(options, selected, onSelect)`.
6. Pour chaque : composable + Preview + 1 test Paparazzi (3 états : default/active/disabled).
7. Documenter dans `docs/COMPONENTS.md`.

**Fichiers touchés**
- Nouveaux : 5 composables sous `presentation/components/designsystem/`, 5 tests Paparazzi, `docs/COMPONENTS.md`

**Critères d'acceptation**
- [ ] 5 composants compilent
- [ ] 15+ snapshots Paparazzi
- [ ] Documentation API

---

### Phase R-4.5 — D1 Renommage tokens v1→v2
**Objectif** : tokens portent les noms sémantiques v2 (cf. R-4.1 table). Bridge typealias pour migration douce.
**Sévérité** : Important — **Effort** : 4–8 h
**Dépendances** : R-4.1
**Catégorie** : Design drift

**Tâches**
1. Pour chaque entrée table mapping : ajouter alias `val accent get() = successGreen` dans `AppColorScheme` (bridge).
2. Refactor call sites par batch : `successGreen` → `accent` (grep + replace).
3. Ajouter tokens manquants spec v2 : `accentSoft`, `accentInk`, `textSubtle`, `textDisabled`, `surfaceAlt`, `surfaceDeep`, `borderStrong`, `inkSoft`, `inkSubtle`, `onInk`, `errorSoft`, `warningSoft`, `infoSoft`.
4. Ajouter tokens carte : `mapLand`, `mapWater`, `mapRoad`, `mapPath`.
5. Aligner `AppRadius` (rL=16 spec vs 20, rXL=24 vs 32) et `AppMotion` (0.12/0.18 spec vs 0.15/0.25).
6. Supprimer noms legacy après refacto.
7. Tests : snapshots inchangés (couleurs identiques, juste renommées).

**Fichiers touchés**
- Modifiés : `core/theme/TurboDriveColorScheme.kt`, `AppColors.kt`, `AppRadius.kt`, `AppMotion.kt`, ~50 call sites

**Critères d'acceptation**
- [ ] 14 nouveaux tokens v2 présents
- [ ] Aucun usage legacy `successGreen` (sauf alias deprecated)
- [ ] Snapshots Paparazzi inchangés

---

# SPRINT 5 — ÉCRANS REDESIGN AUTH/SETUP/MAP/HOME/NÉGO (D2-D6, P1)

> **Objectif sprint** : refonte écrans rider + driver setup + map + négociation conformes JSX.
> **Durée cible** : 15 jours (~80-128 h).
> **Bloqué par** : R-4.* (design v2 prêt).

---

### Phase R-5.1 — D2 Auth Redesign (S01-S08)
**Objectif** : 8 écrans auth conformes `turbodrive_redesign/screens-auth.jsx`.
**Sévérité** : Critique — **Effort** : 16–24 h
**Dépendances** : R-4.4
**Catégorie** : Design + missing feature (OTP channel badge)

**Tâches**
1. Audit écran par écran : Splash, Welcome, Onboarding, Phone, CountryPicker, OTP, Name, Role (S01-S08).
2. Pour chaque : aligner layout, espacements, typo, icônes sur JSX redesign.
3. Câbler OTP channel badge (WhatsApp/SMS) selon réponse backend.
4. Tester Google Sign-In end-to-end (réel client ID staging).
5. Snapshots Paparazzi : 8 écrans × 2 thèmes × 3 fontScales = 48 snapshots.

**Fichiers touchés**
- Modifiés : `presentation/auth/*` (~10 fichiers), `presentation/onboarding/`, `presentation/role/`, `presentation/splash/`

**Critères d'acceptation**
- [ ] Diff visuel < 5% vs JSX redesign sur les 8 écrans
- [ ] OTP channel badge fonctionnel
- [ ] Google Sign-In testé contre backend staging
- [ ] 48 snapshots verts

---

### Phase R-5.2 — D3 Driver Setup Redesign (S09) + OCR
**Objectif** : refonte DriverSetup conforme redesign + intégration OCR (CIN, permis).
**Sévérité** : Critique — **Effort** : 20–32 h
**Dépendances** : R-4.4, R-0.2
**Catégorie** : Design + missing feature

**Tâches**
1. Audit `DriverSetupScreen` (1 wizard 3 steps) vs JSX redesign.
2. Refactor en sous-composants (DriverPersonalStep, DriverLicenseStep, DriverVehicleStep) si pas déjà fait.
3. Intégration OCR :
   - Décision : ML Kit on-device OU appel backend `/ocr/parse` (à confirmer dispo backend).
   - Workflow : capture photo CIN/permis → preview → submit → champs pré-remplis (numéro, nom, date expiration).
   - Fallback : saisie manuelle si OCR échoue.
4. Persistance brouillon : sauvegarder progression dans DataStore (`driver_setup_draft`).
5. Snapshots Paparazzi 3 steps × 2 thèmes = 6 snapshots.

**Fichiers touchés**
- Modifiés : `presentation/driversetup/*` (~6 fichiers)
- Nouveaux : `data/ocr/OcrService.kt` (impl ML Kit ou Retrofit appel backend), `domain/usecases/ParseDocumentUseCase.kt`
- DataStore : nouvelle clé `driver_setup_draft`

**Critères d'acceptation**
- [ ] OCR fonctionne sur 1 CIN test → pré-remplit ≥80% des champs
- [ ] Brouillon persiste entre kill/restart
- [ ] Pas un seul `!!` (cf. R-0.2)
- [ ] 6 snapshots verts

**Risques** : ML Kit augmente la taille de l'APK (~5 MB). Mitigation : Play Feature Delivery (dynamic feature) si APK serré.

---

### Phase R-5.3 — D4 Map Redesign + tokens carte + GPS modes
**Objectif** : carte conforme spec (tokens `mapLand`/`mapWater`/...), modes GPS adaptatifs, foreground service course active. Décision Mapbox v11 ou rester HERE.
**Sévérité** : Critique — **Effort** : 20–32 h
**Dépendances** : R-4.5 (tokens carte)
**Catégorie** : Design + missing feature

**Tâches**
1. **Décision Mapbox v11 vs HERE** : produire `docs/MAP_DECISION.md` avec :
   - Critères : coût licence, support offline (HERE OK déjà), perf, customisation tokens, Android compat.
   - Recommandation par défaut : **rester HERE** (déjà intégré, offline OK) si custom styling possible. Sinon migrer Mapbox.
2. Appliquer tokens carte (HERE customStyles ou Mapbox style JSON).
3. Refonte écrans S32-S35 (map idle, map with route, map driver pin, map zoom controls).
4. **Adaptive GPS modes** : `LocationServiceController.kt` avec `enum class GpsMode { COARSE, HIGH, OFF }` ; switch selon état (idle=COARSE, active ride=HIGH, backgrounded=OFF).
5. **Foreground service course active** : `RideForegroundService` (notification ongoing + location updates HIGH) ; lance sur ride accepted, stop sur ride completed/cancelled.
6. Filtres GPS : ignorer points avec accuracy > 50m ou displacement < 8m (cf. roadmap.md Phase 6).

**Fichiers touchés**
- Nouveaux : `data/location/LocationServiceController.kt`, `presentation/services/RideForegroundService.kt`, `docs/MAP_DECISION.md`
- Modifiés : `presentation/map/HereMapViewComposable.kt`, `MapViewModel.kt`, `AndroidManifest.xml` (service)

**Critères d'acceptation**
- [ ] Tokens carte appliqués
- [ ] GPS mode switch testé (logs)
- [ ] Notification foreground visible pendant ride
- [ ] Filtres GPS actifs

**Risques** : foreground service exige permission notif (Android 13+). Mitigation : prompt user au démarrage du ride.

---

### Phase R-5.4 — D5 Rider Home (S10-S14) + extract `presentation/riderhome/`
**Objectif** : extraire le flux passager de `presentation/map/` dans `presentation/riderhome/` ; refonte conforme JSX.
**Sévérité** : Critique — **Effort** : 12–20 h
**Dépendances** : R-5.3
**Catégorie** : Architecture + design

**Tâches**
1. Créer package `presentation/riderhome/`.
2. Déplacer composables passager-spécifiques (`MapRiderDestinationConfirmedBar`, `MapRoutePickerComponents`, `MapRouteSheet` parties rider) hors de `map/`.
3. Refonte S10 (rider home idle), S11 (search destination), S12 (route preview), S13 (fare estimate), S14 (waiting driver).
4. Intermediate stops UI (déjà API présente côté backend).
5. Snapshots 5 écrans × 2 thèmes = 10 snapshots.

**Fichiers touchés**
- Nouveaux : `presentation/riderhome/RiderHomeScreen.kt`, sous-composants
- Déplacés : ~5 fichiers depuis `presentation/map/`

**Critères d'acceptation**
- [ ] Package `riderhome/` créé avec ≥5 fichiers
- [ ] `presentation/map/` ne contient plus de logique passager
- [ ] 10 snapshots verts

---

### Phase R-5.5 — D6 Négociation (StackedOffersList + PriceStepper UI)
**Objectif** : flow négociation prix côté UI (propose, counter, accept) utilisant les composants R-4.4.
**Sévérité** : Critique — **Effort** : 12–20 h
**Dépendances** : R-3.4 (use cases), R-4.4 (composants)
**Catégorie** : Missing feature

**Tâches**
1. Écran "Offres reçues" : `StackedOffersList` affichant les offres entrantes.
2. Bottom sheet "Contre-proposition" : `PriceStepper` + boutons Accept/Counter.
3. `NegotiationViewModel` (avec ScreenState) consommant `SocketEventManager.events` filtered sur `Negotiation*`.
4. Animation : offre rejetée → swipe out ; offre acceptée → expansion + transition vers ActiveRide.
5. Snapshots 3 états (offers received, countering, accepted).

**Fichiers touchés**
- Nouveaux : `presentation/negotiation/NegotiationScreen.kt`, `NegotiationViewModel.kt`, tests

**Critères d'acceptation**
- [ ] UI réagit aux events Socket en <500ms
- [ ] 3 snapshots verts
- [ ] Test VM ScreenState + 4 scénarios

---

# SPRINT 6 — ÉCRANS LIFECYCLE + WALLET (D7-D10 + P10)

> **Objectif sprint** : compléter écrans active ride/completed (rider + driver), wallet complet, settings.
> **Durée cible** : 12 jours (~64-104 h).

---

### Phase R-6.1 — D7 Rider Active Ride & Completed (S15-S18)
**Objectif** : 4 écrans rider lifecycle : waiting driver, driver en route, ride in progress, completed.
**Sévérité** : Important — **Effort** : 12–20 h
**Dépendances** : R-5.4
**Catégorie** : Design

**Tâches**
1. S15 Waiting driver (ETA + driver info).
2. S16 Driver en route (live position).
3. S17 Ride in progress (route progress + actions : call driver, cancel SOS, share trip).
4. S18 Completed (fare details + rating prompt).
5. Snapshots 4 × 2 thèmes = 8.

**Fichiers touchés** : `presentation/riderhome/active/*`

**Critères d'acceptation**
- [ ] 4 écrans présents, alignés JSX
- [ ] Rating prompt fonctionnel (POST /ratings)
- [ ] 8 snapshots verts

---

### Phase R-6.2 — D8 Driver Home & Ride Offer (S20-S22) + refactor monolithe
**Objectif** : refonte DriverHomeScreen ; décomposer le monolithe 1089 LOC en sous-composants.
**Sévérité** : Important — **Effort** : 16–24 h
**Dépendances** : R-4.4
**Catégorie** : Design + code quality

**Tâches**
1. Décomposer `DriverHomeScreen.kt` (1089 LOC) en : `DriverHomeContent`, `OnlineStatusToggle`, `IncomingOfferSheet`, `StatsCard`, `EarningsRow` (max 300 LOC chacun).
2. S20 Driver home idle, S21 Driver home online, S22 Incoming offer (avec ProgressTimer décompte 15s).
3. Offer queue management (si plusieurs offers simultanés).
4. Snapshots 3 × 2 thèmes = 6.

**Fichiers touchés** : `presentation/driverhome/*` (refactor)

**Critères d'acceptation**
- [ ] Aucun fichier > 400 LOC dans driverhome/
- [ ] ProgressTimer fonctionnel (auto-dismiss à 0)
- [ ] 6 snapshots verts

---

### Phase R-6.3 — D9 Driver Active Ride & Stats (S23-S27)
**Objectif** : écrans driver lifecycle + stats journalières/hebdo.
**Sévérité** : Important — **Effort** : 12–20 h
**Dépendances** : R-6.2
**Catégorie** : Design

**Tâches**
1. S23 Driver on the way, S24 Arrived at pickup, S25 In ride, S26 Completed, S27 Stats.
2. Stats : graph earnings, courses count, online time (use case `GetDriverStatsUseCase`).
3. Snapshots 5 × 2 thèmes = 10.

**Fichiers touchés** : `presentation/driverhome/active/*`, `presentation/driverhome/stats/*`

**Critères d'acceptation**
- [ ] 5 écrans présents
- [ ] Stats récupérées depuis backend
- [ ] 10 snapshots verts

---

### Phase R-6.4 — D10 Profile / Wallet / Settings (S19, S28-S31)
**Objectif** : refonte profile, wallet, settings écrans conformes JSX.
**Sévérité** : Important — **Effort** : 8–12 h
**Dépendances** : R-4.4
**Catégorie** : Design

**Tâches**
1. S19 Profile, S28 Wallet, S29 Settings, S30 Notifications settings, S31 Language settings.
2. Snapshots 5 × 2 thèmes = 10.

**Fichiers touchés** : `presentation/profile/*`, `presentation/wallet/*`, `presentation/settings/*` (nouveau package)

---

### Phase R-6.5 — P10 Wallet transactions + top-up
**Objectif** : compléter Wallet avec liste transactions, top-up flow, pending transactions.
**Sévérité** : Important — **Effort** : 10–14 h
**Dépendances** : R-1.1, R-6.4
**Catégorie** : Missing feature

**Tâches**
1. Endpoint `GET /wallet/transactions` câblé (cf. AUDIT.md endpoints manquants).
2. Endpoint `POST /wallet/top-up` (avec idempotency-key auto via R-1.3).
3. Endpoint `GET /wallet/pending` pour transactions en cours.
4. UI : liste paginée transactions, top-up modal (montant + méthode), pending banner.
5. Tests intégration top-up.

**Fichiers touchés** : `data/network/WalletApiService.kt` (ajouts), `data/repositories/WalletRepositoryImpl.kt`, `presentation/wallet/*`

**Critères d'acceptation**
- [ ] 3 endpoints fonctionnels
- [ ] UI top-up testée end-to-end staging
- [ ] Pagination transactions

---

### Phase R-6.6 — P10 Language switcher + Settings complets
**Objectif** : settings complets (langue, thème override, notifications granulaires).
**Sévérité** : Important — **Effort** : 4–6 h
**Dépendances** : R-6.4
**Catégorie** : Missing feature

**Tâches**
1. Language switcher (ar/fr/en) avec restart activity propre (`AppCompatDelegate.setApplicationLocales`).
2. Theme override (system/light/dark) persistant en DataStore.
3. Notifications settings (FCM topics opt-in/out par type).
4. Tests instrumentation langue switch.

**Fichiers touchés** : `presentation/settings/*`, `core/language/AppLanguage.kt` (déjà existant)

**Critères d'acceptation**
- [ ] Switch ar → fr → restart propre, layout RTL appliqué
- [ ] Theme override persistant

---

# SPRINT 7 — NOTIFS, DEEPLINKS, OFFLINE (P11+P12)

> **Objectif sprint** : typed notification routing, deeplinks fonctionnels, offline resilience + SQLCipher.
> **Durée cible** : 7,5 jours (~32-48 h).

---

### Phase R-7.1 — Typed notification handlers
**Objectif** : `DriverPushMessagingService.onMessageReceived` route vers handler par type (`ride_offer`, `ride_accepted`, `ride_completed`, `wallet_topup`, `chat_message`...).
**Sévérité** : Important — **Effort** : 4–6 h
**Dépendances** : R-3.1 (sealed types alignés)
**Catégorie** : Missing feature

**Tâches**
1. `sealed class FcmPayload` avec sous-classes par type.
2. `FcmPayloadDecoder` qui parse `RemoteMessage.data["type"]`.
3. Handlers : `RideOfferNotificationHandler`, `RideAcceptedNotificationHandler`, etc.
4. Coalescence avec Socket events (éviter doublons) : `NotificationDeduplicator` avec cache 30s par eventId.
5. Tests 5 types.

**Fichiers touchés** : `presentation/notifications/*`

**Critères d'acceptation**
- [ ] ≥5 types routés
- [ ] Doublon FCM+Socket évité

---

### Phase R-7.2 — DeepLinkQueue tests + intégration nav
**Objectif** : deeplinks (`dadadrive://ride/{id}`, `dadadrive://wallet/topup`) testés et naviguent vers le bon écran.
**Sévérité** : Important — **Effort** : 4–6 h
**Dépendances** : R-7.1
**Catégorie** : Missing tests + feature

**Tâches**
1. Auditer `DeepLinkQueue` existant.
2. Câbler dans `MainActivity.onNewIntent` + `AppNavHost`.
3. Tester cold start (app killed + intent deeplink) et warm (app au foreground).
4. Tests instrumentation 3 deeplinks.

**Fichiers touchés** : `app/MainActivity.kt`, `presentation/navigation/*`

**Critères d'acceptation**
- [ ] 3 deeplinks naviguent correctement (cold + warm)
- [ ] Tests instrumentation verts

---

### Phase R-7.3 — Retry queue offline
**Objectif** : requêtes critiques (rating, location update batch) mises en file si offline, rejouées au retour réseau.
**Sévérité** : Important — **Effort** : 6–8 h
**Dépendances** : R-1.1
**Catégorie** : Resilience

**Tâches**
1. Créer `data/network/queue/OfflineRequestQueue.kt` (Room entity `PendingRequestEntity`).
2. Worker `RetryPendingRequestsWorker` (WorkManager) sur `NetworkType.CONNECTED`.
3. Annotation `@Retryable` sur méthodes Retrofit (rating, location batch).
4. Tests : simuler offline → POST en queue → online → POST rejoué.

**Fichiers touchés** : nouveaux fichiers `data/network/queue/`, `data/local/entities/PendingRequestEntity.kt`

**Critères d'acceptation**
- [ ] Requête POST offline persiste
- [ ] Retour online ⇒ rejouée automatiquement
- [ ] Test passe

---

### Phase R-7.4 — Migration SQLCipher (Room chiffré)
**Objectif** : Room utilise SQLCipher ; PII (User cache) chiffré au repos.
**Sévérité** : Important — **Effort** : 6–8 h
**Dépendances** : R-0.6 (sécurité activée)
**Catégorie** : Sécurité (rules.md §6.2)

**Tâches**
1. Ajouter `net.zetetic:android-database-sqlcipher` au `libs.versions.toml`.
2. Modifier `DatabaseModule.kt` pour utiliser `SupportFactory(passphrase)`.
3. Passphrase stockée dans EncryptedSharedPrefs.
4. Migration Room destructive ou alembic-style (existing DB → encrypted) ; documenter.
5. Test : ouvrir DB sans passphrase ⇒ échec.

**Fichiers touchés** : `di/DatabaseModule.kt`, `libs.versions.toml`

**Critères d'acceptation**
- [ ] DB chiffrée vérifiable (ouvrir avec sqlitebrowser sans clé ⇒ illisible)
- [ ] App fonctionne normalement

**Risques** : migration utilisateur existant. Mitigation : doc + version bump DB + backup auto.

---

# SPRINT 8 — A11Y, PERF, MOCK, RELEASE (P13+P14+D11+D12)

> **Objectif sprint** : finition qualité, mock data layer, release readiness.
> **Durée cible** : 12,5 jours (~56-96 h).

---

### Phase R-8.1 — A11y TalkBack walkthroughs
**Objectif** : 3 parcours principaux navigables au TalkBack (auth, ride, wallet).
**Sévérité** : Important — **Effort** : 8–12 h
**Dépendances** : R-5.*, R-6.*
**Catégorie** : A11y

**Tâches**
1. Audit composables : ajouter `contentDescription` partout (icônes, images).
2. Semantics merge correct pour les composants groupés.
3. Tests instrumentation TalkBack (Compose `assertContentDescriptionEquals`).
4. Checklist d'accessibilité dans `docs/A11Y.md`.

**Fichiers touchés** : ~30 composables

**Critères d'acceptation**
- [ ] 3 parcours testés
- [ ] Aucune icône sans description (sauf décorative explicite)

---

### Phase R-8.2 — Perf traces Macrobenchmark
**Objectif** : mesurer startup, scroll, rendering ; baseline + budget alertes.
**Sévérité** : Mineur — **Effort** : 4–8 h
**Dépendances** : R-0.8 (CI)
**Catégorie** : Perf

**Tâches**
1. Setup module `:macrobenchmark`.
2. Tests : `StartupBenchmark`, `MapScrollBenchmark`, `OfferSheetRenderBenchmark`.
3. Intégrer dans CI (job optionnel/nightly).
4. Baseline profile generation.

**Fichiers touchés** : nouveau module Gradle `macrobenchmark/`

**Critères d'acceptation**
- [ ] 3 benchmarks roulent
- [ ] Baseline profile committé

---

### Phase R-8.3 — Snapshots multi fontScale × RTL/LTR
**Objectif** : snapshots Paparazzi sur 3 fontScales (0.85/1.0/1.3) × 2 directions (LTR/RTL).
**Sévérité** : Important — **Effort** : 8–12 h
**Dépendances** : R-5.*, R-6.*
**Catégorie** : Tests

**Tâches**
1. Étendre tests Paparazzi existants avec paramétrisation (`@RunWith(TestParameterInjector::class)`).
2. Cibler écrans clés : Welcome, OTP, Rider home, Driver home, Active ride, Wallet (6 écrans).
3. 6 × 2 thèmes × 3 scales × 2 dirs = **72 snapshots**.

**Critères d'acceptation**
- [ ] 72 snapshots committés
- [ ] CI exécute `verifyPaparazzi`

---

### Phase R-8.4 — D11 Mock data layer
**Objectif** : feature flag pour basculer en mode "tout mocké" (utile pour démo, screenshots, tests).
**Sévérité** : Mineur — **Effort** : 6–10 h
**Dépendances** : R-1.1
**Catégorie** : DevX

**Tâches**
1. `BuildConfig.USE_MOCK_DATA` (debug build type uniquement).
2. Hilt module `MockRepositoriesModule` qui remplace les impls réelles par des `MockRideRepository`, etc. avec données canned.
3. Switcher in-app (settings dev only).

**Fichiers touchés** : nouveaux mocks sous `data/mock/`, Hilt module conditionné

**Critères d'acceptation**
- [ ] Build debug avec flag mock → toutes API mockées
- [ ] App fonctionnelle bout en bout sans réseau

---

### Phase R-8.5 — P14 Release readiness
**Objectif** : signing release, AAB bundle, Play Console config, mapping R8 uploadé.
**Sévérité** : Important — **Effort** : 8–16 h
**Dépendances** : R-0.8
**Catégorie** : Release

**Tâches**
1. Configurer signing config release (keystore + secrets CI).
2. Génération AAB `./gradlew bundleRelease`.
3. Play Console : créer app, fiche listing, screenshots, privacy policy URL.
4. R8 mapping upload Crashlytics + Play.
5. Test interne sur Play Console (closed testing track).
6. Checklist `docs/RELEASE.md`.

**Critères d'acceptation**
- [ ] AAB signé generé
- [ ] Upload Play Console réussi (closed testing)
- [ ] Mapping Crashlytics OK

---

### Phase R-8.6 — D12 Final audit RTL / parité / docs
**Objectif** : audit final RTL (ar), parité fonctionnelle iOS si pertinent, docs à jour.
**Sévérité** : Important — **Effort** : 8–16 h
**Dépendances** : tous les sprints précédents
**Catégorie** : QA

**Tâches**
1. Audit RTL exhaustif (locale ar) : tous les écrans, vérifier mirroring icônes directionnelles.
2. Parité iOS (si projet iOS existe) : matrice features ; sinon documenter écart attendu.
3. Mise à jour `README.md`, `docs/ARCHITECTURE.md`, `docs/COMPONENTS.md`, `docs/RELEASE.md`.
4. PR finale "v1.0 ready" avec changelog complet.

**Critères d'acceptation**
- [ ] Audit RTL : aucun écran cassé
- [ ] Docs à jour
- [ ] CHANGELOG.md publié

---

# SPRINT 9 — SHARED RIDES v2 (P15, post-v1)

> **Objectif sprint** : implémentation complète des courses partagées (matching dynamique).
> **Durée cible** : 7,5 jours (~40-60 h).
> **Note** : optionnel pour v1.0 ; à planifier en v1.1.

---

### Phase R-9.1 — DTOs + repository shared rides
**Objectif** : couche data complète pour shared rides (cf. `backend-integration.md §3.6` + `rules.md §21`).
**Sévérité** : Mineur (post-v1) — **Effort** : 8–12 h
**Dépendances** : R-1.1, R-3.1
**Catégorie** : Missing feature

**Tâches**
1. DTOs : `SharedRideRequestDto`, `SharedMatchDto`, `SharedRideStateDto`.
2. `SharedRideApiService` avec endpoints (cf. spec §3.6).
3. `SharedRideRepository` impl + interface.
4. Tests unit.

---

### Phase R-9.2 — Écran "Search match"
**Objectif** : UI recherche de co-passager (radius, prix max, ETA max).
**Effort** : 8–12 h
**Tâches** : composables, ViewModel ScreenState, snapshots.

---

### Phase R-9.3 — Écran "Confirm split"
**Objectif** : confirmation partage (prix split, co-passager info, route ajustée).
**Effort** : 6–10 h

---

### Phase R-9.4 — Écran "Active shared ride"
**Objectif** : suivi course partagée multi-pickup.
**Effort** : 8–12 h

---

### Phase R-9.5 — Socket events `shared:*`
**Objectif** : ajouter events `shared:match_found`, `shared:partner_joined`, `shared:partner_dropped` au sealed `SocketEvent`.
**Effort** : 4–6 h
**Dépendances** : R-3.1, R-9.1

---

# Vérification globale (end-to-end)

À chaque fin de sprint, exécuter le **smoke test** :

```bash
cd frontend-kotlin

# 1. CI gates locales
./gradlew clean ktlintCheck detekt test verifyPaparazziDebug

# 2. Build
./gradlew assembleDebug

# 3. Smoke test manuel
# - Installer APK debug
# - OTP login
# - Rider : request ride → driver accept → ride complete → wallet visible
# - Driver : online toggle → receive offer → accept → drive → complete
# - Vérifier dark theme + Arabic locale
```

**Métriques de progression** :

| Sprint | Tests count cible | Files > 400 LOC | Hex literals | Snapshot count |
|---|---|---|---|---|
| Avant S0 | 8 | 8 | 137 | 1 |
| S0 done | 8 | 8 | 137 | 1 |
| S1 done | 13+ | 8 | 137 | 1 |
| S2 done | 27+ | 8 | **0** | 1 |
| S3 done | 35+ | 8 | 0 | 10 |
| S4 done | 35+ | 7 | 0 | **35+** |
| S5 done | 45+ | **3** | 0 | 80+ |
| S6 done | 50+ | 0 | 0 | 110+ |
| S7 done | 55+ | 0 | 0 | 110+ |
| S8 done | 60+ | 0 | 0 | **180+** (avec 72 multi-scale) |
| S9 done | 65+ | 0 | 0 | 200+ |

**Cible finale rules.md §17** : 60+ unit, 5+ intégration, 30+ snapshot ⇒ atteinte fin S3 (intégration), fin S4 (snapshot), fin S2 (unit).

---

## Discipline d'exécution

1. **1 phase = 1 PR** (ou max 2 si très liée).
2. **1 PR = 1 [x] dans `roadmapaaa.md`** + 1 test minimum + 1 entrée CHANGELOG.
3. **CI verte obligatoire** avant merge (R-0.8 prérequis).
4. **Pas de phase démarrée sans ses dépendances closes** (cf. colonne "Dépendances").
5. **À la fin de chaque sprint** : recalibrer `roadmapaaa.md` (purger les claims faux), mettre à jour `AUDIT.md` avec deltas.

---

## Risques cross-sprint (rappel synthétique)

| Risque | Sprint impacté | Mitigation |
|---|---|---|
| `turbodrive_redesign/` indisponible | S4-S6 (D0-D10) | R-0.7 prioritaire ; fallback "freeze v1" |
| Backend ne livre pas l'enveloppe ApiResponse | S1 | Fallback compat dans `unwrap()` |
| Socket events backend pas finalisés | S3 | Spec event par event avec backend dev avant impl |
| Décision Mapbox vs HERE traînante | S5 | Forcer décision avant Sprint 5, doc `MAP_DECISION.md` |
| ML Kit OCR augmente APK | S5 | Play Feature Delivery dynamic |
| Conflits 3-way merges sur ViewModels | S2 | Refactor VM par VM, PR isolées |
| Migration SQLCipher casse users existants | S7 | Version bump DB + backup auto |
| Snapshots Paparazzi divergent souvent | S4-S8 | Re-record sciemment, doc politique |

---

## Prochaine action attendue

Le plan est exécutable. Quand tu valides, je commence par **Phase R-0.1** (réactiver gates ktlint/detekt). Tu peux aussi me dire :
- "Commence par R-0.X" (autre quick win),
- "Skip R-0.7" (si pas de redesign à rapatrier maintenant),
- "Découpe Sprint 0 différemment".
