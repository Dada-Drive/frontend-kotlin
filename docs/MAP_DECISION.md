# MAP_DECISION.md — Décision SDK carte + stratégie tokens

**Date** : 2026-05-19
**Sprint** : R-5.3
**Statut** : DÉCIDÉ — rester HERE Maps SDK

---

## Décision : Rester HERE Maps SDK

### Alternatives évaluées

| Option | Effort | Avantages | Inconvénients |
|---|---|---|---|
| **HERE Maps (actuel)** | 0 | Offline Tunisia déjà intégré, routing testé | Pas de styling basemap custom sans HERE Studio |
| Mapbox | 3–4 semaines | Style JSON complet (GL Style), custom basemap | Migration totale, perte offline maps Tunisia |
| Google Maps | 2 semaines | Facile à intégrer | Pas d'offline, coût variable, pas de routing intégré |

### Justification

1. **Offline Tunisia maps** : `HereOfflineMapsManager.kt` (261 LOC) intègre le téléchargement et la lecture des cartes hors-ligne — fonctionnalité métier critique pour la Tunisie (couverture réseau variable).
2. **Routing HERE actif** : `MapPassengerRoutingController.kt` (368 LOC) — routing multipoints testé et fonctionnel.
3. **Coût migration Mapbox** : 3–4 semaines + régression offline maps = non justifié pour R-5.3.
4. **Styling partiel suffisant** : Les tokens design (mapPath, mapRoad) s'appliquent aux polylines et markers — niveau suffisant pour R-5.3.

---

## Contrainte critique : tokens mapLand/mapWater/mapRoad ne s'appliquent PAS au fond de carte

### Problème

Le système de design R-4.5 définit 4 tokens map dans `AppColorScheme` :
- `mapLand = Color(0xFFEEEBE0)` / `Color(0xFF1A1F2E)` (light/dark)
- `mapWater = Color(0xFFD8DCE4)` / `Color(0xFF0E1320)`
- `mapRoad = Color(0xFFFFFFFF)` / `Color(0xFF2A3142)`
- `mapPath = Color(0xFFDDD7C5)` / `Color(0xFF3A4358)`

### Limitation HERE SDK

HERE Maps SDK **ne supporte pas le recoloriage des tuiles de fond de carte** (land, water, roads built into tile data) sans un fichier de style créé dans [HERE Style Studio](https://platform.here.com/). Le `MapScheme` ne contrôle que le preset global (NORMAL_DAY / NORMAL_NIGHT).

### Ce qu'on peut colorier

| Élément | Contrôlable | Token appliqué |
|---|---|---|
| Tuiles fond (land, water) | ❌ (HERE Studio requis) | — |
| Routes des tuiles | ❌ (HERE Studio requis) | — |
| Polylines dessinées par l'app | ✅ | `mapPath`, `mapRoad` |
| Markers custom | ✅ | `mapPath`, `mapRoad` |
| MapScheme (preset global) | ✅ (NORMAL_DAY/NIGHT) | auto via thème app |

### Stratégie R-5.3

- `mapPath` → couleur de la route active (polyline principale)
- `mapRoad` → couleur de la route secondaire (second leg polyline)
- `mapLand` / `mapWater` → réservés pour une future intégration HERE Style Studio (post-MVP)
- `MapScheme.NORMAL_DAY` / `NORMAL_NIGHT` sélectionné automatiquement selon le thème app

### Stratégie long terme (post-MVP)

Pour appliquer `mapLand`/`mapWater`/`mapRoad` au fond de carte :
1. Créer un style JSON dans HERE Style Studio en référençant les valeurs des tokens
2. Exporter le fichier `.HERE` et le bundler dans `assets/`
3. Charger via `hereMapView.mapScene.loadScene(mapStyleUri, ...)`

---

## Dissolution MapColorTokens.kt

`MapColorTokens.kt` (créé en R-2.3 comme structure transitionnelle) est dissous en R-5.3.

### Mapping des tokens legacy → AppColorScheme

| Token legacy | Valeur | Token AppColorScheme |
|---|---|---|
| `routeActiveBlue` | `#2D79FF` | `c.primary` |
| `routeSecondLeg` | `#43A047` | `c.mapRoad` |
| `scheduleAccent` | `#4FC3C8` | `c.info` |
| `darkPanelSurface` | `#2C2C2C` | `c.surfaceDeep` |
| `pinIntermediate` | `#1A1A1A` | `c.textPrimary` |
| `connectorGrey` | `#8E8E93` | `c.textSubtle` |

### Fichiers modifiés (V2)

- `HereMapViewComposable.kt` — `routeSecondLeg` → `mapRoad`
- `MapScreen.kt` — `routeActiveBlue` + `routeSecondLeg`
- `MapRouteInputComponents.kt` — `connectorGrey`, `darkPanelSurface`
- `MapRoutePickerComponents.kt` — `connectorGrey`
- `MapRouteSheet.kt` — `darkPanelSurface` (×3)
- `PickupPinOverlay.kt` — `pinIntermediate` (×2)
- `ScheduleForLaterSheet.kt` — `scheduleAccent`
- `MapColorTokens.kt` — **supprimé**
