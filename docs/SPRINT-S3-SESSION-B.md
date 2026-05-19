# Sprint S3 — Session B : Resync + Négociation + Crash Recovery + Tests (R-3.3 → R-3.6)

**Date** : 2026-05-19
**Branche** : `main` (commits locaux, **0 push**)
**Statut R-3.3 → R-3.6** : ✅ Closed
**Effort réel** : ~3h (vs ~24–36h prévu)

## Périmètre Session B

| Vague | Scope | Commit |
|---|---|---|
| V1 | Audit pré-V2 — BUILD SUCCESSFUL, aucun correctif nécessaire | — |
| V2 | `ResyncResult` + `ResyncOnReconnectUseCase` (parallel rides + wallet) | `4f3afe0` |
| V3 | `@ApplicationScope` + `CoroutineModule` + câblage dans `SocketEventManager` | `10ab073` |
| V4 | `NegotiationRepository` interface + `NegotiationRepositoryImpl` + 4 use cases + DTOs | `a4fcfd4` |
| V5 | 6 tests `NegotiationRepositoryImplTest` (MockK slot capture) | `b33b928` |
| V6 | Crash recovery : `MapViewModel.init` lit le cache + 3 tests `ActiveRideDraftCacheTest` | `24b841f` |
| V7 | E2E Netty : `SocketLifecycleIntegrationTest` (3 tests) + dep `netty-socketio:2.0.0` | `ead4604` |
| V8 | Doc SESSION B + ACTION_PLAN.md fermeture R-3.3–R-3.6 | ce commit |

## Architecture ajoutée

```
SocketService (events SharedFlow)
    │
    ▼ SocketEvent.Connected
SocketEventManager (Hilt Singleton)
    │  appScope.launch { events.collect { if Connected → resync } }
    │
    ▼ ResyncOnReconnectUseCase (domain)
        ├── async { ridesRepository.getMyRides() }  → filter Accepted|InProgress
        └── async { walletRepository.getWallet() }
        → ResyncResult(activeRide, walletInfo)
        → socketService.emitInternalSync(SocketEvent.ResyncCompleted)

NegotiationRepository (interface)
    └── NegotiationRepositoryImpl (data)
            socketEventManager.emit("negotiate:propose", json)
            socketEventManager.emit("negotiate:accept",  json)
            socketEventManager.emit("negotiate:counter", json)
            socketEventManager.emit("negotiate:reject",  json)

MapViewModel.init
    ├── launch { socket events }
    ├── launch { connectivity }
    ├── launch { location }
    └── launch { activeRideDraftCache.load()?.let { _lastRequestedRide.value = it } }  ← R-3.5
```

## Décisions clés

### R-3.3 — Resync
- **`GET /rides/active` absent** : endpoint inexistant côté backend → filtre sur `getMyRides()` + status `Accepted|InProgress`.
- **Notifications non incluses** : pas d'endpoint liste `/notifications?unread=true`. Rides + wallet uniquement.
- **`@ApplicationScope`** : nouvelle annotation Hilt + `CoroutineModule` → `CoroutineScope(SupervisorJob() + Dispatchers.Default)`. Scoped à `SingletonComponent`, durée de vie = process.
- **`SocketService.emitInternalSync`** : méthode publique ajoutée pour injecter des events synthétiques dans `_events` SharedFlow sans passer par le wire.

### R-3.4 — Négociation
- **Cycle de dépendance évité** : `RidesRepositoryImpl → SocketEventManager → ResyncOnReconnectUseCase → RidesRepository → RidesRepositoryImpl` formerait un cycle Hilt. Solution : `NegotiationRepository` est une interface SÉPARÉE, son implémentation injecte seulement `SocketEventManager`.
- **`encodeDefaults = false`** : les champs `null` (`message`, `reason`) sont absents du JSON émis (économie de bande passante, cohérence avec l'API backend).
- **UI différée** : `R-5.5` (D6 négociation). Use cases prêts, pas de `ViewModel` ni `Screen` pour l'instant.

### R-3.5 — Crash recovery
- **`CachedActiveRideEntity`** déjà complet (JSON blob Gson, tous champs `ActiveRide` couverts, pas de migration Room nécessaire).
- **`MockK` sur classe abstraite Android** : `mockk<AppDatabase>(relaxed = true)` utilise objenesis (pas d'appel constructeur) → fonctionne en JVM unit test sans Robolectric.

### R-3.6 — Tests E2E Netty
- **Transport `polling` only** : la combinaison `netty-socketio:2.0.0` + `socket.io-client:2.1.0` provoque `EngineIOException: websocket error` lors de l'upgrade WebSocket (mismatch headers HTTP Netty). Le protocole polling suffit pour les 3 scénarios.
- **Namespace `/` (default)** : `netty-socketio:2.0.0` a un bug EIO4 en polling mode — après acceptation d'un namespace custom (ex. `/riders`), la session devient stale et le client obtient `xhr poll error`. Le comportement du lifecycle (connect/event/disconnect) est identique sur le namespace par défaut.
- **Isolation `@BeforeClass/@AfterClass`** : un seul serveur Netty pour les 3 tests évite les conflits de port et les délais de rebind.
- **`SocketService` bypassé** : son URL est `BuildConfig.BASE_URL` (compile-time, non overridable). Le client `IO` pointe directement vers le serveur Netty in-process.

## Fichiers nouveaux

| Fichier | Type | Taille |
|---|---|---|
| `domain/models/ResyncResult.kt` | data class | 5 lignes |
| `domain/usecases/ResyncOnReconnectUseCase.kt` | use case | 22 lignes |
| `domain/usecases/NegotiationUseCases.kt` | 4 use cases | 30 lignes |
| `domain/protocols/NegotiationRepository.kt` | interface | 10 lignes |
| `data/repositories/NegotiationRepositoryImpl.kt` | impl | 45 lignes |
| `data/network/model/NegotiationDtos.kt` | DTOs | 25 lignes |
| `di/ApplicationScope.kt` | qualifier | 4 lignes |
| `di/CoroutineModule.kt` | Hilt module | 12 lignes |
| `test/…/ResyncOnReconnectUseCaseTest.kt` | 4 tests | 60 lignes |
| `test/…/NegotiationRepositoryImplTest.kt` | 6 tests | 97 lignes |
| `test/…/ActiveRideDraftCacheTest.kt` | 3 tests | 84 lignes |
| `test/…/SocketLifecycleIntegrationTest.kt` | 3 tests E2E | 130 lignes |

## Fichiers modifiés

| Fichier | Modification |
|---|---|
| `data/socket/SocketService.kt` | `+emitInternalSync(SocketEvent)` |
| `data/socket/SocketEventManager.kt` | Ajout `ResyncOnReconnectUseCase` + `@ApplicationScope` init |
| `test/…/SocketEventPipelineTest.kt` | `SocketEventManager(…, mockk(relaxed=true), backgroundScope)` |
| `presentation/map/MapViewModel.kt` | 4e `launch` pour cache restore |
| `di/AppModule.kt` | `@Binds NegotiationRepository → NegotiationRepositoryImpl` |
| `app/build.gradle.kts` | `testImplementation("com.corundumstudio.socketio:netty-socketio:2.0.0")` |
| `docs/ACTION_PLAN.md` | R-3.3 → R-3.6 fermées, sprint S3 100% |

## Suite

**Sprint S3 complet.** Prochain sprint : **S4 (R-4.x)** — Design System v2 (fonts Inter, SVG, 5 composants, tokens v2). Bloqué par disponibilité dossier `turbodrive_redesign/` (R-0.7 ✅ déjà livré).
