# Sprint S3 — Session A : Socket.IO Foundations (R-3.1 + R-3.2)

**Date** : 2026-05-19
**Branche** : `main` (commits locaux, **0 push**)
**Statut R-3.1 + R-3.2** : ✅ Closed
**Effort réel** : ~4h (vs ~7h prévu)

## Périmètre Session A

| Vague | Scope | Commit |
|---|---|---|
| V1 | Audit SocketService + events backend | — (0 commit) |
| V2 | Sealed `SocketEvent` + 12 nouveaux payloads + `enum Role` | `d00fe5e` |
| V3 | `SocketEventDecoder` + 9 tests parsing | `23cdc11` |
| V4 | Refactor `SocketService` — `SharedFlow` + namespaces via `Role` | `ddaeec3` |
| V5 | `SocketEventManager` Hilt façade | `91ed4e4` |
| V6 | Wire 3 ViewModels (Map, Driver, Wallet) | `4ec9eb9` |
| V7 | Tests intégration pipeline + doc | _en cours_ |

## Architecture

```
Backend Socket.IO (wss://api.turbodrive.tn/socket.io/)
       │
       ▼ s.on("ride:accepted") { args -> ... }
SocketService (data/socket/)
  - SharedFlow<SocketEvent> (replay=0, buffer=64, DROP_OLDEST)
  - Connect via Role (RIDER → /riders, DRIVER → /drivers)
  - Decoder dispatched, malformed → null + warn
       │
       ▼ forwarded as-is
SocketEventManager (Hilt @Singleton façade)
       │
       ▼ events.collect { handleSocketEvent(it) }
3 ViewModels — each filters its scope:
  - MapViewModel    : ride:* + negotiate:* + shared:*  (17 events)
  - DriverViewModel : ride:* (driver-facing) + shared:* (10 events)
  - WalletViewModel : wallet:topup_confirmed + wallet:transaction_new
                      → triggers refresh() REST as source of truth
```

## Events câblés (21 + 3 synthétiques)

### Ride lifecycle (9)
`ride:new_request`, `ride:new_offer`, `ride:accepted`, `ride:offer_rejected`,
`ride:driver_arrived`, `ride:status_changed`, `ride:completed`, `ride:cancelled`,
`ride:driver_location`

### Negotiation (4)
`negotiate:propose`, `negotiate:accept`, `negotiate:counter`, `negotiate:reject`
> UI déférée à R-5.5 ; use cases d'émission `socketEventManager.emit(...)` arrivent en R-3.4.

### Wallet (2)
`wallet:topup_confirmed`, `wallet:transaction_new`

### Notification (1)
`notification:new`
> Pas de `NotificationsViewModel` (FCM push gère les notifs background). Stash dans un VM dédié quand R-5.x landera.

### Shared rides (5)
`shared:passenger_joined`, `shared:passenger_left`, `shared:passenger_picked_up`,
`shared:passenger_dropped_off`, `shared:ride_completed`
> Backend V1 déjà implémenté dans `dada-api/`.

### Synthétiques (3, émis par `SocketService`)
- `Connected` — sur `Socket.EVENT_CONNECT` (déclenchera resync en R-3.3)
- `Disconnected` — sur `Socket.EVENT_DISCONNECT`
- `ResyncCompleted` — réservé pour `ResyncOnReconnectUseCase` (R-3.3)

## Décisions tech

- **camelCase** sur tous les `@SerialName` (cohérent avec les 9 payloads pré-existants).
  ⚠️ À valider contre le backend Node en R-3.6 ou Session B.
- **kotlinx.serialization** avec `ignoreUnknownKeys=true`, `isLenient=true`, `coerceInputValues=true` — tolérant aux ajouts futurs.
- **Logger** : `android.util.Log.w()` wrappé dans `runCatching` (natif, casse en JVM unit tests).
- **Pas de `@Named("socket_base_url")`** — `SocketUrl.build()` (pré-existant) dérive déjà de `Constants.BASE_URL`.
- **Pas de SocketModule** — `@Inject constructor` + `@Singleton` suffisent.
- **VMs conservateurs** : exposent `lastSocketEvent: StateFlow<SocketEvent?>` plutôt que muter les états existants alimentés par polling. Évite les races / double-emit. Substantive integration arrive en R-3.5 + R-5.5.
- **WalletVM seul** déclenche un side-effect actif (`refresh()`) car son polling est user-triggered (non périodique).

## Tests Session A (13 tests, tous passants)

### `SocketEventDecoderTest` — 9 tests (V3)
1. décode `ride:new_offer` (happy path)
2. décode `shared:passenger_joined` avec `fareUpdates` imbriqué
3. retourne `null` pour event inconnu
4. retourne `null` sur JSON malformé (pas de crash)
5. tolère champs backend non modélisés (forward-compat)
6. décode `wallet:topup_confirmed` avec `newBalance`
7. décode `ride:driver_location`
8. décode `negotiate:propose` avec `message` optionnel
9. retourne `null` quand un champ requis manque

### `SocketEventPipelineTest` — 4 tests (V7)
1. 3 events émis upstream → reçus dans l'ordre via le façade
2. filtre wallet-scoped accepte wallet et ignore ride (mirror VM pattern)
3. forwarding préserve l'identité de l'event (pas de copie)
4. `replay=0` → collector tardif ne reçoit pas l'historique

## Fichiers Session A

### Nouveaux (5)
- `domain/models/Role.kt` (enum RIDER/DRIVER)
- `data/socket/SocketEventDecoder.kt` (dispatch 21 events)
- `data/socket/SocketEventManager.kt` (Hilt façade)
- `test/.../socket/SocketEventDecoderTest.kt` (9 tests)
- `test/.../socket/SocketEventPipelineTest.kt` (4 tests)

### Modifiés (5)
- `data/socket/SocketEvent.kt` (sealed hierarchy + 12 nouveaux payloads — 103 LOC → 310 LOC)
- `data/socket/SocketService.kt` (SharedFlow + Role + 21 handlers — 79 LOC → 178 LOC)
- `presentation/map/MapViewModel.kt` (subscription + handler scope rider)
- `presentation/driverhome/DriverViewModel.kt` (subscription + handler scope driver)
- `presentation/wallet/WalletViewModel.kt` (subscription + refresh() trigger)
- `test/.../driverhome/DriverViewModelTest.kt` (stub `SocketEventManager.events`)

### Inchangés (utilisés en l'état)
- `data/socket/SocketLifecycleController.kt` — son contrat `onAppBackgrounded/Foregrounded` est préservé.
- `data/socket/SocketUrl.kt` — réutilisé tel quel pour dériver l'URL namespace.

## Différé en Session B (~5h)

- **R-3.3** — Resync §4.7 (`ResyncOnReconnectUseCase`, déclenché par `SocketEvent.Connected`)
- **R-3.4** — Negotiation use cases (`emit("negotiate:propose", ...)`)
- **R-3.5** — Crash recovery active ride (replay cache + restore)
- **R-3.6** — Tests intégration E2E (Netty Socket.IO server mock — 3 scénarios)

## Politique git

❌ **0 git push effectué.** Commits locaux uniquement.
6 commits locaux : `d00fe5e`, `23cdc11`, `ddaeec3`, `91ed4e4`, `4ec9eb9`, _V7_.

## TODOs marqués dans le code

- `data/socket/SocketEvent.kt` — `TODO backend: validate event names + schemas` sur les 12 nouveaux payloads (à valider lorsque le backend documentera §9 du `BACKEND_CONTRACT.md`).
- camelCase des `@SerialName` à confirmer avec le backend Node.
