# 🚗 TurboDrive

[![Android CI](https://github.com/Dada-Drive/frontend-kotlin/actions/workflows/android-ci.yml/badge.svg?branch=main)](https://github.com/Dada-Drive/frontend-kotlin/actions/workflows/android-ci.yml)

> Application Android de transport à la demande — inspirée de Bolt/Uber.  
> Connecte les passagers à des chauffeurs en temps réel, avec suivi GPS, paiement intégré et historique de courses.

---

## 📋 Table des matières

- [Aperçu du projet](#aperçu-du-projet)
- [Stack technique](#stack-technique)
- [Architecture MVVM](#architecture-mvvm)
- [Structure des dossiers](#structure-des-dossiers)
- [Mise en route](#mise-en-route)
- [Contribuer](#contribuer)

---

## Aperçu du projet

TurboDrive est une application mobile Android qui permet :

- 📍 La **géolocalisation en temps réel** du passager et du chauffeur
- 🚕 La **mise en relation** instantanée passager ↔ chauffeur
- 💳 Le **paiement intégré** (mobile money, carte bancaire)
- ⭐ Le **système de notation** des courses
- 📜 L'**historique des trajets** avec détail des prix
- 🔔 Les **notifications push** en temps réel

---

## Stack technique

| Composant | Technologie |
|---|---|
| Langage | Kotlin 2.0.21 |
| Architecture | MVVM + Clean Architecture |
| UI | Jetpack Compose (Material 3, BOM 2024.09.00) |
| Navigation | Jetpack Navigation Compose |
| Injection de dépendances | Hilt 2.56.1 (Dagger) + KSP |
| Réseau HTTP | Retrofit 2.11.0 + OkHttp 4.12.0 |
| Sérialisation | kotlinx.serialization 1.7.3 + Gson converter |
| Base de données locale | Room 2.6.1 |
| Stockage chiffré | androidx.security:security-crypto (token store) |
| Temps réel | Socket.IO 2.1.0 (`io.socket:socket.io-client`) |
| Cartes & GPS | HERE SDK Explore 4.25.5 (AAR local) — Mapbox prévu en remplacement (roadmap `design-system.md`) |
| Authentification | Google Sign-In (`play-services-auth` 21.3.0) → JWT custom + `TokenAuthenticator` (pas Firebase Auth) |
| Crash & monitoring | Firebase Crashlytics + Analytics + FCM (BOM 33.12.0) — **pas Firestore, pas Firebase Auth** |
| Reactive programming | Kotlin Coroutines + Flow |
| Tests unitaires | JUnit 4.13.2 + MockK 1.13.11 + MockWebServer 4.12.0 |
| Tests UI / snapshot | Compose `ui-test-junit4` + Paparazzi 2.0.0-alpha02 |
| Tests instrumentation | androidx.test.espresso 3.7.0 *(legacy, peu utilisé)* |
| Lint & qualité | ktlint 12.1.2 + detekt 1.23.8 |

---

## Architecture MVVM

TurboDrive suit le pattern **MVVM (Model – View – ViewModel)** combiné aux principes de la **Clean Architecture**, organisée en 3 couches indépendantes :

```
┌─────────────────────────────────────────┐
│              UI LAYER (View)            │
│   Composables (Compose-only)            │
│   Observe → StateFlow / collectAsState  │
└──────────────┬──────────────────────────┘
               │ observe / call
┌──────────────▼──────────────────────────┐
│           VIEWMODEL LAYER               │
│   Gère l'état UI · Appelle les UseCases │
│   Ne connaît pas la View directement    │
└──────────────┬──────────────────────────┘
               │ execute
┌──────────────▼──────────────────────────┐
│           DOMAIN LAYER                  │
│   UseCases · Interfaces Repository      │
│   Logique métier pure · Sans Android    │
└──────────────┬──────────────────────────┘
               │ implement
┌──────────────▼──────────────────────────┐
│            DATA LAYER                   │
│   Repositories · Room · Retrofit        │
│   Socket.IO · Crashlytics/FCM           │
└─────────────────────────────────────────┘
```

### Rôle de chaque couche

**UI Layer** — Ce que l'utilisateur voit et avec quoi il interagit.  
Contient une `MainActivity` hôte unique et les écrans Composables (Jetpack Compose). Elle observe les données exposées par le ViewModel via `StateFlow` (`collectAsState`) et ne contient **aucune logique métier**.

**ViewModel Layer** — Le pont entre l'UI et la logique.  
Survit aux rotations d'écran. Appelle les `UseCase` du domaine, expose les états UI, et gère les événements utilisateur.

**Domain Layer** — Le cœur de l'application.  
Contient les `UseCase` (ex: `GetNearbyDriversUseCase`, `BookRideUseCase`) et les interfaces `Repository`. Cette couche est **100% Kotlin pur**, sans dépendance Android — facilitant les tests unitaires.

**Data Layer** — La source de vérité.  
Implémente les interfaces du domaine. Orchestre les données entre la base locale (Room), l'API distante (Retrofit), le canal temps réel (Socket.IO) et Firebase (Crashlytics + FCM + Analytics — **pas Firestore, pas Firebase Auth**). Contient aussi les `DTO` (objets de transfert) et les `DAO`.

---

## Structure des dossiers

> **Note importante** : le code Kotlin réside sous `tn.turbodrive.*`. L'`applicationId` et le `namespace` Android (déclarés dans `app/build.gradle.kts`) restent `com.turbodrive` — c'est sous ce dernier package qu'est généré le `R` Android. Ne pas confondre les deux.

```
app/src/main/java/tn/turbodrive/
│
├── app/             # TurboDriveApplication, MainActivity, AppProcessLifecycleBridge
│
├── core/            # Briques transverses (sans dépendance présentation)
│   ├── constants/        # Clés / URLs / valeurs partagées
│   ├── debug/            # Helpers DEBUG (configs alternatives)
│   ├── designsystem/     # Tokens, composants UI design system
│   ├── diagnostics/      # Boot diagnostics, crash reporting
│   ├── extensions/       # Extensions Kotlin/Android transverses
│   ├── language/         # Sélecteur de langue (FR / AR / EN)
│   ├── logging/          # Wrappers Timber
│   ├── phone/            # Validation/format numéros tunisiens
│   ├── pricing/          # Calculs tarifaires (parité Swift)
│   ├── theme/            # Couleurs, typo (LocalAppColors)
│   ├── utils/            # Helpers génériques (date, IO, …)
│   └── validation/       # DateParseResult & co (R-0.2)
│
├── data/            # Implémentations concrètes (sources de données)
│   ├── local/            # Room DAO + AppDatabase
│   ├── network/          # Retrofit (api, dto, interceptors, authenticator)
│   ├── repositories/     # Implémentations des interfaces domaine
│   ├── socket/           # Wrapper Socket.IO (temps réel)
│   └── storage/          # Stockage chiffré (security-crypto)
│
├── di/              # Modules Hilt (binds + provides)
│
├── domain/          # Logique métier pure (100% Kotlin, sans Android)
│   ├── model/            # Entités principales
│   ├── models/           # Modèles additionnels (sous-domaines)
│   ├── protocols/        # Interfaces transverses
│   └── usecases/         # Cas d'utilisation (BookRide, CreateDriverProfile, …)
│
├── map/             # HERE SDK : caméra, overlays, helpers carte
│
├── presentation/    # Écrans Compose (un sous-dossier par feature)
│   ├── auth/             # Login / Sign-Up Google Sign-In
│   ├── branding/         # Splash branding
│   ├── common/           # Composants partagés inter-écrans
│   ├── components/       # Composants UI atomiques réutilisables
│   ├── driverhome/       # Écran principal chauffeur
│   ├── driversetup/      # Onboarding chauffeur (Personal/License/Vehicle)
│   ├── files/            # Historique documents/courses
│   ├── home/             # Écran d'accueil utilisateur
│   ├── language/         # Sélecteur de langue
│   ├── map/              # Écran carte + bottom sheets
│   ├── navigation/       # AppNavHost (Navigation Compose)
│   ├── notifications/    # FCM in-app
│   ├── onboarding/       # Onboarding utilisateur (pages d'intro)
│   ├── profile/          # Profil utilisateur & paramètres
│   ├── role/             # Sélecteur Driver / Rider
│   ├── session/          # Gestion session/token
│   ├── settings/         # Paramètres avancés (ColorWheel, …)
│   ├── splash/           # Splash screen
│   └── wallet/           # Portefeuille / paiements
│
└── utils/           # Helpers transverses non liés au domaine
```

---

## Mise en route

### Prérequis

- Android Studio Hedgehog (ou plus récent)
- JDK 17+ (le projet compile en `JavaVersion.VERSION_11`)
- Backend `dada-api` accessible (par défaut `http://10.0.2.2:3000/api/v1` en debug — voir `backend-integration.md`)
- Identifiants HERE Maps (`HERE_ACCESS_KEY_ID` + `HERE_ACCESS_KEY_SECRET`) — obtenus via la console HERE Developer
- Compte Firebase **uniquement** pour Crashlytics + FCM + Analytics (le fichier `app/google-services.json` est un **stub local** versionné depuis la phase R-0.1 ; remplacer par le vrai fichier pour la prod)

### Installation

```bash
# 1. Cloner le dépôt
git clone https://github.com/Dada-Drive/frontend-kotlin.git
cd frontend-kotlin

# 2. Copier le template de secrets et remplir les valeurs
cp local.properties.template local.properties
# Édite local.properties pour renseigner sdk.dir, HERE_*, BACKEND_BASE_URL_*, etc.

# 3. (Optionnel) Remplacer le stub google-services.json par le vrai fichier
#    depuis Firebase Console si tu veux Crashlytics/FCM réels en debug.

# 4. Synchroniser et lancer
./gradlew :app:assembleDebug
# Puis : Android Studio > Run > app  (variante debug par défaut)
```

### Build variants

Trois `buildTypes` sont déclarés dans [`app/build.gradle.kts`](app/build.gradle.kts) :

| Variant   | `applicationIdSuffix` | URL backend par défaut                       | Cert pinning | Signing                                                            |
|-----------|-----------------------|----------------------------------------------|--------------|--------------------------------------------------------------------|
| `debug`   | `.debug`              | `http://10.0.2.2:3000/api/v1`                | OFF          | debug                                                              |
| `staging` | `.staging`            | `https://staging-api.turbodrive.tn/api/v1`   | ON           | debug                                                              |
| `release` | *(aucun)*             | `https://api.turbodrive.tn/api/v1`           | ON           | release si `ENABLE_RELEASE_SIGNING=true` (+ keystore configuré), sinon debug |

Les URLs sont surchargeables via `BACKEND_BASE_URL_DEBUG` / `_STAGING` / `_RELEASE` dans `local.properties`.

```bash
./gradlew :app:assembleDebug      # APK debug
./gradlew :app:assembleStaging    # APK staging (cert pinning ON, signing debug)
./gradlew :app:assembleRelease    # APK release (minify + R8 + cert pinning ON)
```

### Secrets locaux

Tous les secrets vivent dans `local.properties` à la racine du projet (non versionné, ignoré par `.gitignore`). Le fichier [`local.properties.template`](local.properties.template) sert de référence ; chaque clé est documentée inline.

| Clé                          | Rôle                                                                 | Obligatoire        |
|------------------------------|----------------------------------------------------------------------|--------------------|
| `sdk.dir`                    | Chemin local du SDK Android                                          | ✅                 |
| `GOOGLE_WEB_CLIENT_ID`       | OAuth 2.0 « Web application » pour Google Sign-In (Credential Manager) | ✅ (auth)          |
| `BACKEND_BASE_URL_DEBUG`     | URL backend variant `debug` (défaut : `http://10.0.2.2:3000/api/v1`) | ⚠️ recommandé      |
| `BACKEND_BASE_URL_STAGING`   | URL backend variant `staging`                                        | ⚠️ recommandé      |
| `BACKEND_BASE_URL_RELEASE`   | URL backend variant `release`                                        | ⚠️ recommandé      |
| `HERE_ACCESS_KEY_ID`         | Clé d'accès HERE SDK                                                 | ✅ (cartes)        |
| `HERE_ACCESS_KEY_SECRET`     | Secret HERE SDK                                                      | ✅ (cartes)        |
| `MAPBOX_ACCESS_TOKEN`        | Placeholder pour le futur SDK Mapbox (roadmap)                       | ❌ (pas encore utilisé) |
| `CERTIFICATE_PINS`           | Pins SHA-256 pour le certificate pinning OkHttp                      | ⚠️ recommandé (staging/release) |
| `ENABLE_RELEASE_SIGNING`     | `true` pour signer l'APK release avec un keystore réel               | ❌ (défaut `false`) |
| `KEYSTORE_PATH`              | Chemin du keystore release                                           | si `ENABLE_RELEASE_SIGNING=true` |
| `KEYSTORE_STORE_PASSWORD`    | Mot de passe du store                                                | si `ENABLE_RELEASE_SIGNING=true` |
| `KEYSTORE_KEY_ALIAS`         | Alias de la clé                                                      | si `ENABLE_RELEASE_SIGNING=true` |
| `KEYSTORE_KEY_PASSWORD`      | Mot de passe de la clé                                               | si `ENABLE_RELEASE_SIGNING=true` |

> **À propos de `google-services.json`** : le fichier `app/google-services.json` actuellement versionné est un **stub local** mis en place lors de la phase R-0.1 pour que `compileDebugKotlin` passe sans dépendance Firebase Console. Pour activer Crashlytics, FCM et Analytics réels (typiquement en staging/release), remplace-le par le fichier obtenu via Firebase Console pour l'`applicationId` correspondant (`com.turbodrive`, `com.turbodrive.debug`, `com.turbodrive.staging`).

---

## Contribuer

Le dépôt est déjà initialisé sur GitHub (`origin = https://github.com/Dada-Drive/frontend-kotlin.git`). Le workflow standard depuis un terminal macOS/Linux :

```bash
# 1. Cloner (ou pull la dernière main)
git clone https://github.com/Dada-Drive/frontend-kotlin.git
cd frontend-kotlin

# 2. Installer les hooks Git (étape obligatoire — voir ci-dessous)
bash scripts/install-hooks.sh

# 3. Créer une branche feature
git checkout -b feature/<short-slug>

# 4. Coder · committer en Conventional Commits
git add <fichiers ciblés>
git commit -m "feat(scope): description courte"

# 5. Pusher la branche puis ouvrir une PR vers main
git push -u origin feature/<short-slug>
gh pr create --base main
```

> Les commits suivent [Conventional Commits](https://www.conventionalcommits.org) (`feat`, `fix`, `refactor`, `docs`, `chore`, `style`, `test`, …).

### Convention commits — footer phase

Tout commit lié à une phase de remédiation référencée dans [`ACTION_PLAN.md`](../ACTION_PLAN.md) **doit** porter un footer explicite :

- `Closes R-X.Y` — la phase est fermée par ce commit (dernier commit de la phase).
- `Refs R-X.Y` — commit contribuant à la phase sans la clôturer (split en plusieurs commits, ou correctif post-phase).

Exemple :

```text
refactor(driversetup): replace !! with explicit guards in submit flow

Removes 10 non-null assertions from DriverSetupScreen.onFooterClick…

Closes R-0.2
```

Permet `git log --grep="R-0.2"` pour retrouver l'historique d'une phase sans dépendre d'un changelog manuel.

### Hooks Git (obligatoire pour tout nouveau dev)

Le projet installe un hook `pre-commit` qui exécute `ktlintCheck` + `detekt` sur les fichiers Kotlin **stagés**. Un commit avec violation est refusé avec un message d'aide.

```bash
# macOS / Linux
bash scripts/install-hooks.sh

# Windows (PowerShell 7+)
pwsh scripts/install-hooks.ps1
```

Comportement :

- **Aucun `.kt` / `.kts` stagé** → le hook saute immédiatement (≈100 ms).
- **Au moins un `.kt` / `.kts` stagé** → `./gradlew ktlintCheck detekt` est lancé (typique 3–8 s grâce au daemon Gradle ; cas pessimiste ~11 s en first-run).
- **Échec** → message explicite avec rappel d'`./gradlew ktlintFormat` pour auto-fix.

Bypass exceptionnel : `git commit --no-verify` (à utiliser avec parcimonie — la CI continuera à gater).

---

## Certificate pinning

L'app épingle les certificats des backends `staging` et `release` via OkHttp `CertificatePinner` (`rules.md §4.6`, phase R-0.6). Le pinning est :
- **désactivé** en `debug` (`BuildConfig.ENABLE_CERT_PINNING = false`) ;
- **activé** en `staging` et `release` (`true`) si `BuildConfig.CERTIFICATE_PINS` est non vide.

Tout `SSLPeerUnverifiedException` (cert qui ne matche pas un pin) est loggué dans Crashlytics avec un breadcrumb `[cert-pinning] host=…` puis **re-thrown** ; la requête échoue côté UI (jamais avalée silencieusement).

### Configurer les pins en local

Ajouter dans `local.properties` (gitignored — voir `local.properties.template:CERTIFICATE_PINS`) :

```properties
CERTIFICATE_PINS=api-staging.turbodrive.tn|sha256/AAAA…=|sha256/BBBB…=,api.turbodrive.tn|sha256/CCCC…=|sha256/DDDD…=
```

Convention : `host|sha256/PIN_BASE64=` séparés par virgule ; **2 pins minimum par host** (cert actuel + cert backup) pour survivre à une rotation.

### Récupérer un pin SHA-256 depuis un host

```bash
echo | openssl s_client -servername HOST -connect HOST:443 2>/dev/null \
  | openssl x509 -pubkey -noout \
  | openssl pkey -pubin -outform DER \
  | openssl dgst -sha256 -binary \
  | openssl enc -base64
```

Exemple : `HOST=api.turbodrive.tn`. Le résultat est la partie après `sha256/`.

### CI / GitHub Actions

Les vrais pins **ne sont jamais commités** dans le repo. Ils vivent dans :
- `local.properties` côté dev (gitignored)
- GitHub Actions Secret `CERTIFICATE_PINS` côté CI (cf. R-0.8)

Le workflow CI génère `local.properties` à la volée à partir des secrets GitHub avant chaque tâche Gradle :

```yaml
- run: |
    {
      echo "sdk.dir=$ANDROID_HOME"
      echo "CERTIFICATE_PINS=${{ secrets.CERTIFICATE_PINS }}"
      # ...autres secrets
    } > local.properties
```

---

## 🌐 Network architecture (R-1.1)

Tous les retours backend passent par un **wrapper typé homogène** `ApiResponse<T>` défini dans [`data/network/envelope/`](app/src/main/java/tn/turbodrive/data/network/envelope/) :

```kotlin
data class ApiResponse<T>(
    val success: Boolean = true,
    val data: T? = null,
    val error: ApiError? = null,
)

data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any?>? = null,
)
```

### Pattern de consommation

```kotlin
// 1. ApiService renvoie Response<ApiResponse<T>>
@POST("auth/login")
suspend fun login(@Body req: LoginRequest): Response<ApiResponse<AuthResponse>>

// 2. Repository appelle .unwrap() pour obtenir Result<T>
val result: Result<AuthResponse> = api.login(req).unwrap()

// 3. ViewModel consomme via Result.fold (en attendant R-2.1 ScreenState)
result.fold(
    onSuccess = { /* ... */ },
    onFailure = { e ->
        val msg = (e as? BackendException)?.apiError?.message ?: "Erreur"
    },
)
```

### Périmètre couvert

- **43 endpoints** migrés sur 6 ApiServices : Auth ×9, Driver ×16, Rides ×13, Wallet ×2, Notification ×2, DriverRegistration ×1
- **5 repositories** consommateurs adaptés (`.unwrap().fold()` quand messages localisés, `.unwrap().getOrThrow()` quand propagation simple)
- **TokenAuthenticator + RefreshTokenExecutor** intacts (OkHttp direct — refresh token a son propre format hors enveloppe)
- **`ApiClient.kt` legacy** coexiste pendant la transition (Cloudinary uploader, paginated requests)

### Convention `ApiResponse<Unit>` pour les endpoints sans payload

Les endpoints qui ne renvoient pas de données utiles (logout, delete, action sans retour) sont typés `ApiResponse<Unit>` plutôt qu'avec un DTO `EmptyResponse` ad-hoc :

```kotlin
@POST("auth/logout")
suspend fun logout(
    @Body body: LogoutRequest,
): Response<ApiResponse<Unit>>
```

Côté `unwrap()`, le succès sans `data` est résolu vers `Result.success(Unit)` :

```kotlin
// Dans ApiCall.unwrap()
when {
    body.success && body.data != null -> Result.success(body.data)
    body.success && body.data == null -> {
        // Endpoint sans payload : succès implicite
        @Suppress("UNCHECKED_CAST")
        Result.success(Unit as T)
    }
    // ... cas error
}
```

Cette convention :
- Évite la prolifération de `EmptyResponse` / `VoidResponse` / etc.
- Reste type-safe : le compilateur Kotlin garantit qu'on ne consomme pas `Unit` comme une donnée
- Aligne `logout()` avec les futurs endpoints `DELETE /resources/{id}` (R-6.5+)

⚠️ **Garde-fou** : ne déclarer `ApiResponse<Unit>` que pour les endpoints qui retournent **réellement** rien. Pour `ApiResponse<DtoX>` avec `data: null`, le cast `Unit as T` provoquerait une `ClassCastException` à l'usage. Voir le test `ApiCallTest.unwrap returns success Unit when backend envelope success=true with null data` pour la validation.

### Feature flag `STRICT_ENVELOPE`

`BuildConfig.STRICT_ENVELOPE = false` (default) — réservé à une phase future. Quand `true`, `unwrap()` refusera tout endpoint qui renvoie l'ancien format brut. Pour l'instant, `unwrap()` est déjà strict côté code ; le flag permet d'ajouter un fallback compat plus tard sans toucher `build.gradle.kts`.

### Tests

[`ApiCallTest.kt`](app/src/test/java/tn/turbodrive/data/network/envelope/ApiCallTest.kt) — 5 cas :
- `success` nominal (`{success:true, data:{...}}`)
- `success` Unit (logout / delete sans data → `Result.success(Unit)`)
- `failure` backend (`{success:false, error:{code,message}}` → `BackendException(apiError.code)`)
- `failure` HTTP 401 (`Response.error(401, ...)` → `BackendException("HTTP_401")`)
- `failure` malformed (body null → `BackendException("EMPTY_BODY")`)

### Codes erreur localisés

Les `apiError.message` bruts sont propagés tels quels en R-1.1. Le mapping codes backend → FR/AR localisé arrive en **R-1.2**.

---

## ⚙️ Continuous Integration (R-0.8)

**Workflow** : [`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml) — lancé sur chaque `push` et `pull_request` ciblant `main`.

**Lien Actions** : <https://github.com/Dada-Drive/frontend-kotlin/actions/workflows/android-ci.yml>

### Jobs (4)

| Job | Tâche Gradle | Timeout | Dépendance |
|---|---|---|---|
| `lint` | `ktlintCheck detekt` (parité hook pre-commit R-0.4) | 20 min | — |
| `unit-test` | `:app:testDebugUnitTest` | 30 min | — |
| `snapshot` | `:app:verifyPaparazziDebug` | 30 min | — |
| `build-debug` | `:app:assembleDebug` | 30 min | `lint` |

JDK 17 Temurin, Gradle 8.13 (détecté via wrapper), cache Gradle automatique (`gradle/actions/setup-gradle@v4`). `concurrency` annule les builds obsolètes sur push successifs. Artifacts (test reports + Paparazzi diffs) uploadés uniquement en cas d'échec.

**Parité local ↔ CI** : le job `lint` exécute exactement les mêmes commandes que [`scripts/pre-commit.sh`](scripts/pre-commit.sh) (`./gradlew ktlintCheck detekt`). Si le hook passe localement, le job `lint` passera en CI.

### Secrets à configurer

**URL** : <https://github.com/Dada-Drive/frontend-kotlin/settings/secrets/actions>

| Secret | Criticité | Source | Comportement si absent |
|---|---|---|---|
| `HERE_ACCESS_KEY_ID` | ✅ **critique** | Ops / HERE portal | Build échoue (HERE SDK auth) |
| `HERE_ACCESS_KEY_SECRET` | ✅ **critique** | Ops / HERE portal | Build échoue (HERE SDK auth) |
| `GOOGLE_WEB_CLIENT_ID` | ⚠️ soft | Firebase Console → OAuth Web Client | Google Sign-In KO en runtime, build OK |
| `CERTIFICATE_PINS` | ⚠️ soft | `openssl s_client` (cf. section Pinning ci-dessus) | Pinning désactivé en debug, build OK |

> **Note** : la première run CI échouera tant que les 2 secrets critiques `HERE_*` ne sont pas configurés. C'est attendu — configurer puis relancer manuellement via l'onglet Actions.

### Activer la branch protection sur `main`

**URL** : <https://github.com/Dada-Drive/frontend-kotlin/settings/branches>

1. Cliquer **Add branch protection rule**
2. **Branch name pattern** : `main`
3. Cocher **Require status checks to pass before merging**
4. Cocher **Require branches to be up to date before merging**
5. Dans la liste des status checks, sélectionner les 4 jobs :
   - `Lint (ktlint + detekt)`
   - `Unit tests`
   - `Paparazzi snapshots`
   - `Build debug APK`
6. (Optionnel) Cocher **Require a pull request before merging** pour interdire les push directs sur `main`
7. **Create**

### Tester un pin invalide (validation manuelle)

1. Mettre dans `local.properties` un pin volontairement faux (ex: `sha256/AAA…AAA=`).
2. `./gradlew :app:assembleStagingDebug` puis installer l'APK.
3. Déclencher une requête réseau → `SSLPeerUnverifiedException` côté Logcat + report Crashlytics.

---

## Documentation

| Fichier | Contenu |
|---|---|
| [`docs/ACTION_PLAN.md`](docs/ACTION_PLAN.md) | Plan d'action de remédiation (sprints S0 → S5+). Source de vérité pour les phases `R-X.Y` référencées dans les commits. |
| [`app/detekt-baseline-meta.md`](app/detekt-baseline-meta.md) | Inventaire de la dette detekt parquée en baseline et planning de résorption. |

---

## Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.
