# 🚗 DadaDrive

> Application Android de transport à la demande — inspirée de Bolt/Uber.  
> Connecte les passagers à des chauffeurs en temps réel, avec suivi GPS, paiement intégré et historique de courses.

---

## 📋 Table des matières

- [Aperçu du projet](#aperçu-du-projet)
- [Stack technique](#stack-technique)
- [Architecture MVVM](#architecture-mvvm)
- [Structure des dossiers](#structure-des-dossiers)
- [Mise en route](#mise-en-route)
- [Pusher le projet sur Git](#pusher-le-projet-sur-git)

---

## Aperçu du projet

DadaDrive est une application mobile Android qui permet :

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

DadaDrive suit le pattern **MVVM (Model – View – ViewModel)** combiné aux principes de la **Clean Architecture**, organisée en 3 couches indépendantes :

```
┌─────────────────────────────────────────┐
│              UI LAYER (View)            │
│   Fragments · Activities · Composables  │
│   Observe → LiveData / StateFlow        │
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
Contient les `Fragment`, `Activity`, `ViewModel` liés à l'UI. Elle observe les données exposées par le ViewModel via `LiveData` ou `StateFlow` et ne contient **aucune logique métier**.

**ViewModel Layer** — Le pont entre l'UI et la logique.  
Survit aux rotations d'écran. Appelle les `UseCase` du domaine, expose les états UI, et gère les événements utilisateur.

**Domain Layer** — Le cœur de l'application.  
Contient les `UseCase` (ex: `GetNearbyDriversUseCase`, `BookRideUseCase`) et les interfaces `Repository`. Cette couche est **100% Kotlin pur**, sans dépendance Android — facilitant les tests unitaires.

**Data Layer** — La source de vérité.  
Implémente les interfaces du domaine. Orchestre les données entre la base locale (Room), l'API distante (Retrofit), le canal temps réel (Socket.IO) et Firebase (Crashlytics + FCM + Analytics — **pas Firestore, pas Firebase Auth**). Contient aussi les `DTO` (objets de transfert) et les `DAO`.

---

## Structure des dossiers

> **Note importante** : le code Kotlin réside sous `tn.dadadrive.*`. L'`applicationId` et le `namespace` Android (déclarés dans `app/build.gradle.kts`) restent `com.dadadrive` — c'est sous ce dernier package qu'est généré le `R` Android. Ne pas confondre les deux.

```
app/src/main/java/tn/dadadrive/
│
├── app/             # DadaDriveApplication, MainActivity, AppProcessLifecycleBridge
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

> **À propos de `google-services.json`** : le fichier `app/google-services.json` actuellement versionné est un **stub local** mis en place lors de la phase R-0.1 pour que `compileDebugKotlin` passe sans dépendance Firebase Console. Pour activer Crashlytics, FCM et Analytics réels (typiquement en staging/release), remplace-le par le fichier obtenu via Firebase Console pour l'`applicationId` correspondant (`com.dadadrive`, `com.dadadrive.debug`, `com.dadadrive.staging`).

---

## Contribuer

Le dépôt est déjà initialisé sur GitHub (`origin = https://github.com/Dada-Drive/frontend-kotlin.git`). Le workflow standard depuis un terminal macOS/Linux :

```bash
# 1. Cloner (ou pull la dernière main)
git clone https://github.com/Dada-Drive/frontend-kotlin.git
cd frontend-kotlin

# 2. Créer une branche feature
git checkout -b feature/<short-slug>

# 3. Coder · committer en Conventional Commits
git add <fichiers ciblés>
git commit -m "feat(scope): description courte"

# 4. Pusher la branche puis ouvrir une PR vers main
git push -u origin feature/<short-slug>
gh pr create --base main
```

> Les commits suivent [Conventional Commits](https://www.conventionalcommits.org) (`feat`, `fix`, `refactor`, `docs`, `chore`, `style`, `test`, …). Les hooks pre-commit (ktlint + detekt) doivent passer avant push.

---

## Contributeurs

| Nom | Rôle |
|---|---|
| [@ton-username](https://github.com/ton-username) | Lead Developer |

---

## Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.
