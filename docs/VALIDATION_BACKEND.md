# Validation backend — Audit pré-S5 (2026-05-19)

## Contexte

Avant d'attaquer S5 (Auth Redesign — 80-128h), audit E2E de la couche réseau construite en R-1.1 (envelope) / R-1.2 (codes erreur) / R-1.3 (idempotency) pour valider que les hypothèses correspondent au backend réel.

## Méthode

Tests DNS + curl sur les URLs de fallback compilées dans `BuildConfig` :
- debug → `http://10.0.2.2:3000/api/v1`
- staging → `https://staging-api.turbodrive.tn/api/v1`
- release → `https://api.turbodrive.tn/api/v1`

## Finding critique : aucun backend n'existe

| Cible | État DNS | État service |
|---|---|---|
| `staging-api.turbodrive.tn` | ❌ NXDOMAIN | n/a |
| `api.turbodrive.tn` | ❌ NXDOMAIN | n/a |
| `turbodrive.tn` (apex) | ❌ NXDOMAIN | n/a |
| `dadadrive.tn` (ancien nom) | ❌ NXDOMAIN | n/a |
| `localhost:3000` | n/a | ❌ aucun service à l'écoute |

Le TLD `turbodrive.tn` n'est même pas enregistré chez un registrar. Aucun backend tournant en local non plus.

Confirmation utilisateur : **backend pas encore développé**. Le frontend Kotlin a été construit pendant 4 sprints contre des hypothèses, sans contrat formel ni endpoint réel.

## Impact sur la suite

Risques quand le backend sera développé :

1. **Drift d'enveloppe** — si le backend retourne `{status, payload}` au lieu de `{success, data}`, `ApiResponse<T>.unwrap()` casse partout (43 endpoints) et il faudra refondre [ApiCall.kt](../app/src/main/java/tn/turbodrive/data/network/envelope/ApiCall.kt).
2. **Drift de codes erreur** — si le backend utilise `UserNotFound` au lieu de `USER_NOT_FOUND`, les 34 codes de [BackendErrorCode.kt](../app/src/main/java/tn/turbodrive/domain/models/BackendErrorCode.kt) tombent tous sur `UNKNOWN` et les messages localisés `PresentableErrorMapper` ne s'affichent jamais.
3. **Drift de noms de champs** — si user revient avec `fullName` (camelCase) au lieu de `full_name` (snake_case attendu par `@SerializedName`), tous les DTOs cassent.
4. **Refresh token flow** — `RefreshTokenExecutor` attend `{accessToken, refreshToken}` à `POST /auth/refresh-token` ; aucune validation que le backend exposera cet endpoint avec cette shape.
5. **Idempotency-key** — l'`IdempotencyKeyInterceptor` envoie le header sur 4 endpoints (`POST /rides`, `PATCH /rides/{id}/offers/{offerId}/pick`, `POST /ratings/rides/{rideId}`, `POST /rides/{id}/accept`). Si le backend l'ignore, on a une fausse impression de protection contre les doublons.

## Décision

🔴 **STOP audit E2E** (impossible) — pivot vers production d'un **contrat de référence** que le backend devra implémenter.

Voir [BACKEND_CONTRACT.md](BACKEND_CONTRACT.md) — extrait du code frontend : 43 endpoints, 34 codes erreur, envelope, auth flow, headers, idempotency convention.

## Recommandations avant S5

| Priorité | Action | Effort |
|---|---|---|
| 🔴 P0 | Décider qui développe le backend (toi ? autre dev ? freelance ?) et selon quel timeline | n/a |
| 🔴 P0 | Donner `docs/BACKEND_CONTRACT.md` au dev backend comme spec | n/a |
| 🟠 P1 | Soit attendre backend MVP avant S5, soit développer S5 contre `MockWebServer` puis valider en S6 | n/a |
| 🟠 P1 | Enregistrer le domaine `turbodrive.tn` (ou décider du vrai TLD) | 1h |
| ✅ DONE | Setup `MockWebServer` en tests d'intégration pour figer le contrat côté front — 12 tests verts (`data/network/contract/`) | 2026-05-19 |

## Verdict pour S5

🔴 **NOT GO sans clarification** sur l'origine du backend.

Trois scénarios possibles :

| Scénario | Action S5 |
|---|---|
| Backend démarre en parallèle (autre dev) | S5 sur MockWebServer + validation E2E en fin de S5 |
| Pas de backend prévu | S5 reporté, écriture d'un backend minimal (Node/Bun) avant |
| Toi seul dev (front + back) | Re-séquencer : backend MVP avant S5 (priorité contrat plutôt qu'UI) |
