# Security Fixes — Critical Audit Items

**Date** : 2026-05-19
**Source** : Audit Senior — TurboDrive Android (Kotlin)
**Sprints** : hors-sprint (bloquants production)
**Commits** : `b9cdbfa`, `32e5170`, `6a1a9e0`

---

## Critiques fixés (3/3)

### ✅ Critique 1 — TokenManager fail-fast (`b9cdbfa`)

**Avant** : Si `EncryptedSharedPreferences` échouait à l'initialisation (device rooté, Keystore
hardware indisponible, bug OS), le code tombait silencieusement dans un fallback
`context.getSharedPreferences("..._plain_fallback", MODE_PRIVATE)`.
→ Les JWT access/refresh tokens étaient stockés **en clair sur le disque**.

**Après** :
- `catch (e: Exception)` lève `SecurityException(...)` immédiatement
- **Aucun fallback** non chiffré possible
- Import `android.util.Log` supprimé (ne loggue plus l'état du fallback)
- Les callers doivent gérer `SecurityException` et afficher une erreur "sécurité du device indisponible"

**Impact** : les utilisateurs sur un device avec Keystore cassé ne peuvent pas se connecter.
Trade-off intentionnel — mieux que stocker des tokens en clair.

**Tests** : `TokenManagerContractTest` (3 tests, JVM-safe) :
- Aucun appel `getSharedPreferences()` présent dans le source
- `throw SecurityException(` présent dans le catch block
- `android.util.Log` non importé

**Fichiers modifiés** :
- `app/src/main/java/tn/turbodrive/data/storage/TokenManager.kt`
- `app/src/test/java/tn/turbodrive/data/storage/TokenManagerContractTest.kt` (nouveau)

---

### ✅ Critique 2 — Room migrations explicites (`32e5170`)

**Avant** : `AppDatabase` utilisait `.fallbackToDestructiveMigration()` + `exportSchema = false`.
→ Toute mise à jour de schéma (ajout colonne, nouvelle table…) détruisait **toutes les données locales**
(ride actif, wallet, lieux sauvegardés, profil utilisateur) au premier lancement post-update.
Aucun schéma versionné dans le repo — impossible d'auditer les changements ou d'écrire des tests.

**Après** :
- `exportSchema = true` — le schéma est exporté à chaque build
- KSP args : `room.schemaLocation = app/schemas/` (fichiers JSON générés et commités)
- `schemas/tn.turbodrive.data.local.AppDatabase/1.json` committé comme baseline v1
- `fallbackToDestructiveMigration()` retiré de `DatabaseModule`
- `AllMigrations.ALL` array créé (vide pour v1 — migrations futures ici)
- Politique de migration documentée dans `AllMigrations.kt`

**Politique de migration** (documentée dans `AllMigrations.kt`) :
1. Modifier l'entity → bumper `@Database(version = N+1)`
2. Builder → `app/schemas/.../N+1.json` généré automatiquement
3. Écrire `Migration(N, N+1)` dans `AllMigrations.kt` + ajouter à `ALL`
4. Écrire un test `MigrationTestHelper` pour valider le chemin de migration
5. Ne jamais utiliser `fallbackToDestructiveMigration` en production

**Tables couvertes par le schéma v1** :
| Table | PK | Colonnes |
|---|---|---|
| `cached_user_profile` | `id` | id, fullName, phone, email, avatarUrl, role, cachedAt |
| `cached_wallet_balance` | `id` (singleton) | id, balance, currency, status, cachedAt |
| `cached_active_ride` | `id` (singleton) | id, rideStateJson (JSON blob), cachedAt |
| `cached_saved_place` | `id` | id, name, address, lat, lng, type, cachedAt |

**Fichiers modifiés** :
- `app/build.gradle.kts` (bloc `ksp {}` avec `room.schemaLocation`)
- `app/src/main/java/tn/turbodrive/data/local/AppDatabase.kt` (`exportSchema = true`)
- `app/src/main/java/tn/turbodrive/di/DatabaseModule.kt` (migrations explicites)
- `app/src/main/java/tn/turbodrive/data/local/migrations/AllMigrations.kt` (nouveau)
- `app/schemas/tn.turbodrive.data.local.AppDatabase/1.json` (nouveau — baseline v1)

---

### ✅ Critique 3 — Logs de clés API retirés (`6a1a9e0`)

**Avant** : `TurboDriveApplication.kt` loggait :
```kotlin
AppLogger.d("HereSDK Key ID length: ${accessKeyId.length}")
AppLogger.d("HereSDK Key Secret length: ${accessKeySecret.length}")
```
→ Même la **longueur** d'une clé est une fuite d'information : aide les reverse engineers à
identifier le format de la clé et à optimiser leurs attaques.

**Après** :
- Les 2 lignes de log de longueur sont **supprimées**
- ProGuard `proguard-rules.pro` : `assumenosideeffects` retire `Log.d/v/i` en release
  (Log.e et Log.w préservés pour Crashlytics)
- TODO SEC-001 documenté dans `build.gradle.kts` pour stratégie long terme

**Fichiers modifiés** :
- `app/src/main/java/tn/turbodrive/app/TurboDriveApplication.kt`
- `app/build.gradle.kts` (TODO SEC-001)
- `app/proguard-rules.pro` (strip Log.d/v/i en release)

---

## Décisions différées

### ⏭️ Rotation des clés API (décision user requise)

Les clés HERE SDK et Google Web Client ID sont dans `local.properties` (non commité).

- **Si le repo a été rendu public** ou si les clés ont été partagées hors contexte sécurisé → **rotation immédiate requise**.
- **Si le repo est resté privé** → les clés peuvent attendre la stratégie V2 (SEC-001).

### ⏭️ Stratégie clés long terme — SEC-001 (post-MVP)

| Option | Clé concernée | Effort | Protection |
|---|---|---|---|
| Firebase Remote Config | `GOOGLE_WEB_CLIENT_ID` | 2h | Élevée (non bundlée) |
| Backend proxy | HERE Maps tile/routing | 4–8h | Élevée (clé jamais client-side) |
| NDK obfuscation | Toutes | 8h | Faible (reverse possible) |

Recommandation : option 1 pour Google (facile), option 2 pour HERE (efficace mais effort non négligeable).

---

## Vérifications post-fix

```
✅ TokenManager: 0 appel getSharedPreferences() (aucun fallback clair)
✅ AppDatabase: exportSchema = true
✅ DatabaseModule: 0 fallbackToDestructiveMigration
✅ schemas/1.json: présent et committé
✅ TurboDriveApplication: 0 log de longueur de clé
✅ ./gradlew testDebugUnitTest: BUILD SUCCESSFUL
```

## Status production readiness

| Avant fix | Après fix |
|---|---|
| 🔴 3 critiques bloquants | 🟢 PRÊT MVP |

Reste à faire post-MVP :
- SEC-001 : migration clés hors BuildConfig
- Cache TTL/éviction (Room)
- Socket emit fire-and-forget → Result feedback
