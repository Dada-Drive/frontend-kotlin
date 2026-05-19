# Backend contract — Frontend Kotlin (S4 freeze)

Spec de référence extraite du code frontend `tn.turbodrive.*` (2026-05-19). Ce que le backend **doit** implémenter pour que l'app Kotlin actuelle fonctionne sans modification.

Source de vérité côté code :
- Enveloppe : [`data/network/envelope/`](../app/src/main/java/tn/turbodrive/data/network/envelope/)
- Codes erreur : [`domain/models/BackendErrorCode.kt`](../app/src/main/java/tn/turbodrive/domain/models/BackendErrorCode.kt)
- Services Retrofit : [`data/network/api/`](../app/src/main/java/tn/turbodrive/data/network/api/)
- DTOs : [`data/network/model/`](../app/src/main/java/tn/turbodrive/data/network/model/)

---

## 1. Base URL & versioning

Les paths Retrofit sont relatifs à une base URL qui doit se terminer par `/api/v1/`.

| Env | URL |
|---|---|
| Debug (émulateur) | `http://10.0.2.2:3000/api/v1/` |
| Staging | `https://staging-api.turbodrive.tn/api/v1/` |
| Release | `https://api.turbodrive.tn/api/v1/` |

Modifiable via `local.properties` (`BACKEND_BASE_URL_DEBUG/STAGING/RELEASE`).

## 2. Enveloppe de réponse (toutes les réponses)

**Toutes** les réponses (succès et erreur) doivent suivre cette shape :

```json
// Succès avec payload
{ "success": true, "data": { ... }, "error": null }

// Succès sans payload (logout, delete, 204 équivalent)
{ "success": true, "data": null, "error": null }

// Erreur
{ "success": false, "data": null, "error": { "code": "OTP_INVALID", "message": "Code invalide", "details": { ... } } }
```

Règles :
- `success` est un **flag business** (≠ HTTP status). Une 200 avec `success: false` est valide.
- `code` (dans `error`) est l'identifiant **stable** mappé côté front. Voir §6.
- `message` est le texte FR brut (non utilisé côté front s'il y a un mapping pour `code`).
- `details` est optionnel — Map JSON arbitraire (params invalides, retry-after, etc.).
- Sur HTTP 4xx/5xx, le front fabrique `code = "HTTP_${status}"` si le body est manquant — donc préférer **toujours renvoyer un body avec `error.code`** même sur les erreurs.

Implémentation côté front : [`ApiCall.kt`](../app/src/main/java/tn/turbodrive/data/network/envelope/ApiCall.kt).

## 3. Headers obligatoires

### Envoyés par le client (à chaque requête)

| Header | Source | Note |
|---|---|---|
| `Authorization: Bearer <accessToken>` | si user logué | Ajouté par `AuthInterceptor` |
| `Accept-Language` | `ar` / `fr` / `en` | Selon préférence user (fallback `en`) |
| `X-Device-Id` | UUID persisté local | Pour analytics / audit / rate limit |
| `X-App-Version` | `BuildConfig.VERSION_NAME` | Pour breaking changes |
| `X-Platform` | `android` | Constant |
| `Content-Type: application/json` | sur `POST`/`PATCH`/`PUT` | Standard |
| `idempotency-key: <uuid-v4>` | sur endpoints `@Idempotent` | Voir §5 |

### Optionnels client

| Header | Note |
|---|---|
| `X-TurboDrive-Auth-Retry: 1` | Re-essai automatique après refresh token (interne au `TokenAuthenticator`) — le backend l'ignore mais doit **accepter** la requête comme une normale |

### Retournés par le backend

| Header | Quand | Note |
|---|---|---|
| `Content-Type: application/json` | Toutes | Obligatoire |
| `Retry-After` | sur 429 | Secondes — propagé dans `details.retry_after` du body si possible |

## 4. Authentication flow

```
┌─────────────────────────────────────────────────────────┐
│ NOT AUTHENTICATED                                       │
│   1. POST /auth/send-otp     { phone }                  │
│   2. POST /auth/verify-otp   { phone, code }            │
│      → { accessToken, refreshToken, user, isNewUser }   │
│      (front persiste accessToken + refreshToken)        │
│                                                         │
│ ALTERNATIVE                                             │
│   1. POST /auth/login        { phone, password }        │
│      OU /auth/register       { full_name, ..., pwd }    │
│      OU /auth/google         { idToken }                │
│      → { accessToken, refreshToken, user }              │
└─────────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────┐
│ AUTHENTICATED                                           │
│   Toutes les requêtes : Authorization: Bearer <access>  │
│                                                         │
│   GET /auth/me                                          │
│      → { user }  (refresh user au démarrage app)        │
│                                                         │
│   Sur 401 (accessToken expiré ou invalide) :            │
│     → TokenAuthenticator déclenche automatiquement      │
│       POST /auth/refresh-token { refreshToken }         │
│       → { accessToken, refreshToken } (rotation)        │
│     → relance la requête originale avec nouveau token   │
│     Header marker : X-TurboDrive-Auth-Retry: 1          │
│                                                         │
│   Si refresh échoue 3× → cooldown 5min + force logout   │
│   Si 401 sur /auth/refresh-token lui-même → logout      │
│                                                         │
│   Logout :                                              │
│     POST /auth/logout { refreshToken }                  │
│     → ApiResponse<Unit> (data null OK)                  │
└─────────────────────────────────────────────────────────┘
```

### JWT

Pas de contrainte sur le contenu du JWT côté front — le client le stocke et le réémet. Recommandé : expiration courte (~15-30 min) pour accessToken, longue (~30 jours) pour refreshToken.

### Codes erreur auth-spécifiques

- `401` sur n'importe quel endpoint (sauf `/auth/refresh-token`) → déclenche refresh
- `401` sur `/auth/refresh-token` → logout user
- Codes business : `INVALID_CREDENTIALS`, `ACCOUNT_SUSPENDED`, `ACCOUNT_NOT_FOUND`, `FORBIDDEN`, `UNAUTHORIZED`, `OTP_EXPIRED`, `OTP_INVALID`, `OTP_MAX_ATTEMPTS`, `OTP_RATE_LIMITED` (voir §6)

Note : `TOKEN_EXPIRED` / `TOKEN_INVALID` ne **doivent PAS** être exposés au front en tant que codes business — le front les attend comme HTTP 401 pour déclencher le refresh, pas comme un envelope `success: false`.

## 5. Idempotency

4 endpoints marqués `@Idempotent` côté front (header `idempotency-key: <uuid-v4>` injecté automatiquement) :

| Endpoint | Raison |
|---|---|
| `POST /rides` | Création de course — éviter doublon |
| `PATCH /rides/{id}/offers/{offerId}/pick` | Sélection chauffeur — éviter double assignation |
| `POST /ratings/rides/{rideId}` | Note de course — éviter doublon |
| `POST /rides/{id}/accept` | (Driver) accepter une course — éviter double accept |

Contrat backend :
- Si une requête avec le même `idempotency-key` arrive en duplicate (timeframe ≥ 5min), le backend **doit** :
  - soit renvoyer **la même réponse** que la première requête (ride ID identique, status 200)
  - soit renvoyer 200 avec un flag dans `details` indiquant "déjà traité"
- Le header est en **minuscules** (`idempotency-key`, pas `Idempotency-Key`) — HTTP est insensible à la casse, mais pour la consistance des logs.

Implémentation : [`IdempotencyKeyInterceptor.kt`](../app/src/main/java/tn/turbodrive/data/network/interceptor/IdempotencyKeyInterceptor.kt).

## 6. Codes erreur (34 codes)

Le front mappe ces codes vers des messages localisés (FR/EN/AR) via `PresentableErrorMapper`. Tout code non reconnu tombe sur `UNKNOWN` (message générique).

### Auth (5)
| Code | Sens |
|---|---|
| `UNAUTHORIZED` | Pas de session valide |
| `INVALID_CREDENTIALS` | Phone/password incorrects |
| `ACCOUNT_SUSPENDED` | Compte suspendu (admin) |
| `ACCOUNT_NOT_FOUND` | Numéro non enregistré |
| `FORBIDDEN` | Action interdite à ce role |

### OTP (4)
| Code | Sens |
|---|---|
| `OTP_EXPIRED` | Code dépassé (TTL ~5min) |
| `OTP_MAX_ATTEMPTS` | Trop de tentatives erronées |
| `OTP_INVALID` | Code ne match pas |
| `OTP_RATE_LIMITED` | Trop de demandes d'OTP (renvoyer après X sec) |

### Rides (7)
| Code | Sens |
|---|---|
| `RIDE_NOT_FOUND` | rideId inconnu |
| `RIDE_INVALID_STATUS` | Action incompatible avec status actuel (ex: cancel sur ride terminée) |
| `RIDE_ALREADY_ACCEPTED` | Course déjà acceptée par un autre driver |
| `RIDE_EXPIRED` | Demande expirée (no driver found) |
| `RIDE_OUTSIDE_BOUNDS` | Hors zone de service |
| `OFFER_NOT_FOUND` | offerId inconnu |
| `RIDE_STOP_NOT_FOUND` | stopId inconnu |

### Wallet (4)
| Code | Sens |
|---|---|
| `INSUFFICIENT_BALANCE` | Solde trop faible |
| `WALLET_SUSPENDED` | Wallet bloqué |
| `DUPLICATE_TRANSACTION` | Tx déjà enregistrée |
| `INVALID_AMOUNT` | Montant négatif / zéro / hors limites |

### Driver / Vehicle (4)
| Code | Sens |
|---|---|
| `DRIVER_NOT_FOUND` | driverId inconnu |
| `DRIVER_NOT_APPROVED` | Driver pas encore validé par admin |
| `DRIVER_OFFLINE` | Driver actuellement offline |
| `VEHICLE_NOT_FOUND` | Pas de véhicule rattaché |

### Rating (2)
| Code | Sens |
|---|---|
| `RATING_NOT_FOUND` | ratingId inconnu |
| `RATING_ALREADY_EXISTS` | Note déjà soumise pour cette course |

### Upload (3)
| Code | Sens |
|---|---|
| `UPLOAD_INVALID_TYPE` | MIME non supporté |
| `UPLOAD_TOO_LARGE` | Fichier > limite (10MB suggéré) |
| `UPLOAD_RATE_LIMITED` | Trop d'uploads / temps |

### Notification (1)
| Code | Sens |
|---|---|
| `NOTIFICATION_NOT_FOUND` | notificationId inconnu |

### Generic (4)
| Code | Sens |
|---|---|
| `VALIDATION_ERROR` | Params invalides (`details` doit contenir le détail) |
| `NOT_FOUND` | Ressource générique non trouvée |
| `RATE_LIMITED` | Throttle global (`details.retry_after` recommandé) |
| `INTERNAL_ERROR` | Erreur serveur non-typée |

### Fallback
| Code | Sens |
|---|---|
| `UNKNOWN` | Code non reconnu côté front — le front affiche un message générique |

## 7. Endpoints (43)

Tous renvoient `ApiResponse<T>` (voir §2). Conventions :
- snake_case dans les JSON bodies (mappé par `@SerializedName` côté front)
- Phones au format E.164 (`+216XXXXXXXX`)
- IDs en `String` (UUID ou identifiant texte, ouvert)

### 7.1 Auth (9 endpoints)

[`AuthApiService.kt`](../app/src/main/java/tn/turbodrive/data/network/api/AuthApiService.kt)

| Méthode | Path | Auth | Body | Réponse `data` |
|---|---|---|---|---|
| POST | `/auth/login` | ❌ | `LoginRequest` | `AuthResponse` |
| POST | `/auth/register` | ❌ | `RegisterRequest` | `AuthResponse` |
| POST | `/auth/google` | ❌ | `GoogleAuthRequest` | `AuthResponse` |
| POST | `/auth/send-otp` | ❌ | `SendOtpRequest` | `SendOtpResponse` |
| POST | `/auth/verify-otp` | ❌ | `VerifyOtpRequest` | `VerifyOtpResponse` |
| POST | `/auth/refresh-token` | ❌ (refresh token in body) | `{refreshToken}` | `{accessToken, refreshToken}` |
| GET | `/auth/me` | ✅ | — | `GetMeResponseDto` |
| PATCH | `/users/me/role` | ✅ | `UpdateRoleRequest` | `UpdateRoleResponse` |
| PATCH | `/users/me` | ✅ | `UpdateProfileRequest` | `UpdateProfileResponse` |
| POST | `/auth/logout` | ✅ | `LogoutRequest` | `Unit` (data null) |

#### Schémas

```ts
// LoginRequest
{ "phone": "string", "password": "string" }

// RegisterRequest
{ "full_name": "string", "email": "string?", "phone": "string?", "password": "string" }

// GoogleAuthRequest
{ "idToken": "string" }

// SendOtpRequest
{ "phone": "string" }

// SendOtpResponse
{ "message": "string?", "expiresIn": "int?" }     // expiresIn en secondes

// VerifyOtpRequest
{ "phone": "string", "code": "string" }

// VerifyOtpResponse
{
  "message": "string?",
  "accessToken": "string?",   // null si user déjà connecté (Google) — sinon obligatoire
  "refreshToken": "string?",
  "user": UserDto?,           // OtpUserDto, voir ci-dessous
  "isNewUser": "bool"
}

// AuthResponse (login/register/google)
{ "accessToken": "string", "refreshToken": "string", "user": UserDto }

// UserDto (canonique)
{
  "id": "string",
  "full_name": "string?",
  "email": "string?",
  "phone": "string?",
  "avatar_url": "string?",
  "role": "string"            // ex: "RIDER", "DRIVER", "ADMIN"
}

// LogoutRequest
{ "refreshToken": "string" }

// UpdateRoleRequest
{ "role": "string" }          // ex: "DRIVER"

// UpdateProfileRequest (tous champs optionnels)
{ "full_name": "string?", "email": "string?", "avatar_url": "string?" }

// RefreshTokenRequest
{ "refreshToken": "string" }

// RefreshTokenResponse  (PAS dans une enveloppe : raw JSON !)
// ⚠️ POST /auth/refresh-token retourne du JSON BRUT non encapsulé
//    car parsé par RefreshTokenExecutor avec Gson direct, hors Retrofit.
//    Soit le backend renvoie l'enveloppe et le front s'adapte, soit
//    cet endpoint est l'exception. À aligner.
{ "success": "bool?", "accessToken": "string", "refreshToken": "string" }
```

> ⚠️ **Anomalie tracée** : `RefreshTokenExecutor.kt` désérialise directement `RefreshTokenResponse` sans passer par `ApiResponse<T>`. C'est une incohérence à corriger côté front OU à documenter comme exception côté backend (renvoyer payload nu sur cet endpoint).

### 7.2 Rides (13 endpoints)

[`RidesApiService.kt`](../app/src/main/java/tn/turbodrive/data/network/api/RidesApiService.kt)

| Méthode | Path | Auth | Idempotent | Body / Query | Réponse `data` |
|---|---|---|---|---|---|
| GET | `/rides/fare` | ✅ | — | `?distance_km=&estimated_minutes=` | `FareApiResponse` |
| POST | `/rides` | ✅ | ✅ | `RequestRideRequestDto` | `RideResponseDto` |
| GET | `/rides/{id}/offers` | ✅ | — | path: rideId | `RideOffersResponseDto` |
| GET | `/rides/my` | ✅ | — | — | `RidesResponseDto` |
| POST | `/rides/{id}/stops` | ✅ | — | `AddRideStopsRequestDto` | `RideStopsResponseDto` |
| GET | `/rides/{id}/stops` | ✅ | — | — | `RideStopsResponseDto` |
| GET | `/rides/scheduled` | ✅ | — | — | `RidesResponseDto` |
| PATCH | `/rides/{id}/offers/{offerId}/pick` | ✅ | ✅ | — | `RideResponseDto` |
| PATCH | `/rides/{id}/cancel` | ✅ | — | `CancelRideBodyDto` | `RideResponseDto` |
| POST | `/ratings/rides/{rideId}` | ✅ | ✅ | `SubmitRideRatingRequestDto` | `RideRatingResponseDto` |
| GET | `/ratings/rides/{rideId}` | ✅ | — | — | `RideRatingResponseDto` |
| GET | `/ratings/drivers/{driverId}` | ✅ | — | `?page=1&limit=20` | `DriverRatingsResponseDto` |
| GET | `/driver/nearby` | ✅ | — | `?lat=&lng=&radius_km=5.0` | `NearbyDriversResponseDto` |

DTOs : voir [`data/network/model/`](../app/src/main/java/tn/turbodrive/data/network/model/).

### 7.3 Driver (16 endpoints)

[`DriverApiService.kt`](../app/src/main/java/tn/turbodrive/data/network/api/DriverApiService.kt)

| Méthode | Path | Auth | Idempotent | Body | Réponse `data` |
|---|---|---|---|---|---|
| GET | `/driver/profile` | ✅ | — | — | `DriverProfileResponseDto` |
| POST | `/driver/profile` | ✅ | — | `CreateDriverProfileRequestDto` | `DriverProfileResponseDto` |
| PATCH | `/driver/profile` | ✅ | — | `CreateDriverProfileRequestDto` | `DriverProfileResponseDto` |
| GET | `/driver/vehicle` | ✅ | — | — | `VehicleResponseDto` |
| POST | `/driver/vehicle` | ✅ | — | `CreateVehicleRequestDto` | `VehicleResponseDto` |
| PATCH | `/driver/vehicle` | ✅ | — | `CreateVehicleRequestDto` | `VehicleResponseDto` |
| PATCH | `/driver/status` | ✅ | — | `SetOnlineStatusRequestDto` | `DriverProfileResponseDto` |
| PATCH | `/driver/location` | ✅ | — | `UpdateDriverLocationRequestDto` | `DriverProfileResponseDto` |
| GET | `/rides/available` | ✅ | — | — | `AvailableRidesResponseDto` |
| POST | `/rides/{id}/accept` | ✅ | ✅ | — | `RideOfferResponseDto` |
| POST | `/rides/{id}/refuse` | ✅ | — | — | `MessageResponseDto` |
| GET | `/rides/my` | ✅ | — | — | `RidesResponseDto` |
| GET | `/rides/{id}` | ✅ | — | — | `RideResponseDto` |
| PATCH | `/rides/{id}/start` | ✅ | — | — | `RideResponseDto` |
| PATCH | `/rides/{id}/complete` | ✅ | — | — | `CompleteRideResponseDto` |
| PATCH | `/rides/{id}/cancel` | ✅ | — | `CancelRideRequestDto` | `RideResponseDto` |

### 7.4 Driver Registration (1 endpoint)

[`DriverRegistrationApiService.kt`](../app/src/main/java/tn/turbodrive/data/network/api/DriverRegistrationApiService.kt)

| Méthode | Path | Auth | Body | Réponse `data` |
|---|---|---|---|---|
| POST | `/driver/registration` | ✅ | `DriverRegistrationRequestDto` | `DriverRegistrationResponseDto` |

⚠️ Marqué *"Placeholder API contract for upcoming backend endpoints. Wire the real URL path once backend confirms it."* dans le code — path à confirmer.

### 7.5 Wallet (2 endpoints)

[`WalletApiService.kt`](../app/src/main/java/tn/turbodrive/data/network/api/WalletApiService.kt)

| Méthode | Path | Auth | Réponse `data` |
|---|---|---|---|
| GET | `/wallet` | ✅ | `WalletResponseDto` |
| GET | `/wallet/transactions` | ✅ | `WalletTransactionsResponseDto` |

### 7.6 Notifications (2 endpoints)

[`NotificationApiService.kt`](../app/src/main/java/tn/turbodrive/data/network/api/NotificationApiService.kt)

| Méthode | Path | Auth | Body | Réponse `data` |
|---|---|---|---|---|
| POST | `/notifications/token` | ✅ | `NotificationTokenRequest` | `Unit` |
| DELETE | `/notifications/token` | ✅ | — | `Unit` |

## 8. Conventions générales

### Nommage
- **Paths** : snake-case avec slash final non requis (`/auth/send-otp`, pas `/auth/sendOtp`)
- **JSON fields** : snake_case côté backend, le front mappe via `@SerializedName` quand nécessaire (`full_name` → `fullName` côté Kotlin)
- **Exception historique** : les champs `accessToken` / `refreshToken` sont en camelCase (cf. `AuthResponse`, `RefreshTokenResponse`) — à conserver pour compat

### Statuts HTTP
- `200` : succès (avec ou sans data)
- `201` : création réussie (peut être traité comme 200 côté front)
- `400` / `422` : `error.code = VALIDATION_ERROR` (ou code business spécifique)
- `401` : déclenche refresh token automatique côté front — **ne pas** envelopper avec `success: false`
- `403` : `error.code = FORBIDDEN`
- `404` : `error.code = NOT_FOUND` (ou code business spécifique : `RIDE_NOT_FOUND`, etc.)
- `409` : `error.code = DUPLICATE_TRANSACTION` ou `RATING_ALREADY_EXISTS` (idempotency conflict)
- `429` : `error.code = RATE_LIMITED` avec `details.retry_after` en secondes
- `500` : `error.code = INTERNAL_ERROR`

### Pagination
Convention vue dans `getDriverRatings` : `?page=1&limit=20`. Réponse doit inclure `{items, total, page, limit}` (à formaliser quand le DTO sera implémenté).

### Dates
À confirmer — préférence ISO 8601 UTC (`2026-05-19T14:30:00Z`).

## 9. Socket.IO (preview S3)

Non couvert par cette spec — sera détaillé en R-3.1. À titre indicatif, le front attend :
- Endpoint : `wss://api.turbodrive.tn/socket.io/`
- Auth : JWT en `?token=<accessToken>` ou header
- Namespaces : `/riders` et `/drivers`
- Events : à définir (ride status updates, driver location, offer notifications)

## 10. Roadmap d'implémentation backend recommandée

Par ordre de priorité pour débloquer le frontend :

| Phase | Endpoints | Pourquoi |
|---|---|---|
| **MVP-1** | `POST /auth/send-otp`, `POST /auth/verify-otp`, `GET /auth/me`, `POST /auth/refresh-token`, `POST /auth/logout`, `PATCH /users/me/role` | Débloquer onboarding & S5 (Auth Redesign) |
| **MVP-2** | `GET /rides/fare`, `POST /rides`, `GET /rides/{id}/offers`, `PATCH /rides/{id}/offers/{offerId}/pick`, `PATCH /rides/{id}/cancel`, `GET /driver/nearby` | Flow rider booking |
| **MVP-3** | `GET /driver/profile`, `POST/PATCH /driver/profile`, `GET /driver/vehicle`, `PATCH /driver/status`, `GET /rides/available`, `POST /rides/{id}/accept`, `PATCH /rides/{id}/start`, `PATCH /rides/{id}/complete` | Flow driver |
| **MVP-4** | `GET /wallet`, `GET /wallet/transactions`, `POST /ratings/rides/{rideId}`, `GET /rides/my`, `GET /rides/scheduled` | Compléments |
| **MVP-5** | `/auth/login`, `/auth/register`, `/auth/google` | Auth alternatives (OTP suffit pour MVP) |
| **MVP-6** | `/notifications/*`, `/driver/registration`, `/rides/{id}/stops`, ratings drivers | Polish |

## 11. Anomalies tracées (à résoudre côté front quand backend connu)

| # | Anomalie | Fichier | Action |
|---|---|---|---|
| A1 | `RefreshTokenResponse` parsé hors enveloppe `ApiResponse<T>` | `RefreshTokenExecutor.kt` | Soit aligner backend pour ne PAS envelopper sur cet endpoint, soit refactorer front pour parser via envelope |
| A2 | `DriverRegistrationApiService` marqué "placeholder" — path `/driver/registration` non confirmé | `DriverRegistrationApiService.kt` | Confirmer path avec backend |
| A3 | `getMyRides` existe dans `RidesApiService` ET `DriverApiService` avec même path `/rides/my` | les deux | Confirmer : un seul endpoint qui filtre par role automatiquement ? |
| A4 | Conventions de casing mixtes : `accessToken` (camelCase) vs `full_name` (snake_case) dans les DTOs | tous DTOs | Tolérer l'incohérence ou faire une passe d'uniformisation |
| A5 | `GET /rides/fare` et `GET /driver/nearby` utilisent query params snake_case (`distance_km`, `radius_km`) — confirmer | `RidesApiService.kt` | Aligner |
| A6 ✅ | ~~HTTP 4xx/5xx écrase tout `error.code` du body : `unwrap()` retourne systématiquement `HTTP_${status}` au lieu de propager le code envelope.~~ **Résolu 2026-05-19 (R-1.4)** — `ApiCall.kt` parse maintenant `errorBody()` sur les réponses non-2xx via `parseEnvelopeFromErrorBody()`. Fallback `HTTP_${code}` préservé pour body non-JSON (nginx HTML, gateway timeouts). 3 tests contract verrouillent : OTP_RATE_LIMITED sur 429, INTERNAL_ERROR sur 500, fallback HTTP_502 sur HTML. | `ApiCall.kt` | ✅ Fixé |

## 12. Tests de contrat (verrouillage front-side)

Tests d'intégration MockWebServer qui figent le contrat décrit dans ce doc côté front (cf. `app/src/test/java/tn/turbodrive/data/network/contract/`) :

| Test | Couvre |
|---|---|
| [`AuthApiContractTest`](../app/src/test/java/tn/turbodrive/data/network/contract/AuthApiContractTest.kt) | 8 cases — send-otp / verify-otp (existing + new user) / me / logout / OTP_INVALID / OTP_EXPIRED / RATE_LIMITED |
| [`IdempotencyContractTest`](../app/src/test/java/tn/turbodrive/data/network/contract/IdempotencyContractTest.kt) | 4 cases — header injection sur `@Idempotent`, absence sur non-annoté, préservation lors de replay (retry), nom lowercase |
| [`ApiCallTest`](../app/src/test/java/tn/turbodrive/data/network/envelope/ApiCallTest.kt) (pré-existant) | Mécanique d'enveloppe pure |
| [`RefreshTokenExecutorTest`](../app/src/test/java/tn/turbodrive/data/network/authenticator/RefreshTokenExecutorTest.kt) (pré-existant) | Refresh raw OkHttp |
| [`TokenAuthenticatorRefreshIntegrationTest`](../app/src/test/java/tn/turbodrive/data/network/authenticator/TokenAuthenticatorRefreshIntegrationTest.kt) (pré-existant) | Flow 401 → refresh → retry / force logout |

À chaque nouveau DTO ou endpoint MVP-N, ajouter le test contract correspondant dans le même répertoire. Pattern : `ContractTestSupport.retrofitService(server)` pour avoir une instance Retrofit branchée sur MockWebServer avec la Gson production.

---

**Quand le backend est dev** : valider chaque endpoint contre cette spec et mettre à jour le présent doc si drift. Voir aussi [VALIDATION_BACKEND.md](VALIDATION_BACKEND.md) pour le contexte de pourquoi ce doc existe.
