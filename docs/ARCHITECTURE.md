# DadaDrive Android — Architecture Notes

Living document. Each major refactor adds a section anchored on its phase ID (R-X.Y).

---

## Pattern d'état UI : `ScreenState<T>` (R-2.1)

Tous les ViewModels de l'app exposent un `StateFlow<ScreenState<T>>` plutôt que des paires `(loading, error)`. Cette approche force le code consommateur (Compose) à gérer **tous les états explicitement** et élimine les "écrans blancs" sur erreur.

### Le contrat

```kotlin
sealed interface ScreenState<out T> {
    data object Idle : ScreenState<Nothing>                       // jamais chargé
    data object Loading : ScreenState<Nothing>                     // requête en cours
    data class Loaded<out T>(val value: T) : ScreenState<T>        // données disponibles
    data class Error(val error: PresentableError) : ScreenState<Nothing>  // erreur localisée
}
```

Fichier : [`presentation/common/ScreenState.kt`](../app/src/main/java/tn/dadadrive/presentation/common/ScreenState.kt)

**Variance** : `out T` est obligatoire — sans elle, `MutableStateFlow<ScreenState<User>>(ScreenState.Idle)` ne compilerait pas (`Idle` est `ScreenState<Nothing>`, incompatible avec `ScreenState<User>` en variance invariante).

### Pattern ViewModel

```kotlin
class UserProfileViewModel @Inject constructor(
    private val repo: UserRepository,
    private val errorMapper: PresentableErrorMapper,
) : ViewModel() {

    private val _state = MutableStateFlow<ScreenState<UserProfile>>(ScreenState.Idle)
    val state: StateFlow<ScreenState<UserProfile>> = _state.asStateFlow()

    fun loadProfile(id: String) = viewModelScope.launch {
        _state.value = ScreenState.Loading
        _state.value = repo.getUserProfile(id).toScreenState(errorMapper)
    }
}
```

L'extension `Result<T>.toScreenState(mapper)` traduit un `Result.success` en `Loaded` et un `Result.failure` en `Error` avec un `PresentableError` localisé. Elle **n'émet pas** `Loading` — c'est au ViewModel de l'émettre avant l'appel (voir snippet ci-dessus).

### Pattern Compose

```kotlin
@Composable
fun UserProfileScreen(viewModel: UserProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        ScreenState.Idle -> Box(Modifier.fillMaxSize()) { /* placeholder */ }
        ScreenState.Loading -> CenteredProgress()
        is ScreenState.Loaded -> UserCard(s.value)
        is ScreenState.Error -> ErrorBanner(
            message = s.error.messageResId?.let { stringResource(it) }
                ?: s.error.message,
        )
    }
}
```

Le `when` est **exhaustif sans `else`** — ajouter une nouvelle branche à `ScreenState` dans le futur provoquera une erreur de compilation sur tous les call sites. C'est la valeur principale du sealed.

### Pourquoi `Error(PresentableError)` et pas `Error(String)` ?

- ✅ **Localisation automatique** via `messageResId` (R-1.2) — `stringResource(id)` retourne la bonne traduction selon la locale device (FR/AR/EN)
- ✅ **Code typé `BackendErrorCode`** accessible pour logique métier (ex: redirect login sur `UNAUTHORIZED`, retry sur `TIMEOUT`)
- ✅ **Pas de re-mapping côté UI** — l'erreur est déjà résolue à la sortie du `PresentableErrorMapper`

### Flow continu : `Flow<Result<T>>.asScreenStateFlow(mapper)`

Pour les sources de données continues (Socket.IO en R-3.x, polling périodique), l'extension `asScreenStateFlow` émet `Loading` une fois, puis un état terminal (`Loaded` / `Error`) à chaque émission upstream :

```kotlin
repo.observeActiveRide()
    .asScreenStateFlow(errorMapper)
    .collect { _state.value = it }
```

### Helpers de convenance

Fichier : [`presentation/common/ScreenStateExtensions.kt`](../app/src/main/java/tn/dadadrive/presentation/common/ScreenStateExtensions.kt)

- `state.dataOrNull(): T?` — valeur si `Loaded`, `null` sinon
- `state.errorOrNull(): PresentableError?` — erreur si `Error`, `null` sinon
- `state.isIdle / isLoading / isLoaded / isError` — booléens (Compose-friendly)

### Migration depuis le pattern legacy

Pattern legacy à remplacer dans **10 ViewModels** (R-2.2) :

```kotlin
// ❌ AVANT — 3 StateFlow + états impossibles possibles (loading=true && data!=null)
private val _loading = MutableStateFlow(false)
private val _error = MutableStateFlow<String?>(null)
private val _data = MutableStateFlow<UserProfile?>(null)

// ✅ APRÈS — 1 StateFlow + 4 états explicites + impossibilités garanties par le type
private val _state = MutableStateFlow<ScreenState<UserProfile>>(ScreenState.Idle)
```

### Tests

- [`ScreenStateTest`](../app/src/test/java/tn/dadadrive/presentation/common/ScreenStateTest.kt) — 5 cas : variance Idle/Loading, inférence Loaded, payload Error, exhaustiveness `when`
- [`ScreenStateExtensionsTest`](../app/src/test/java/tn/dadadrive/presentation/common/ScreenStateExtensionsTest.kt) — 6 cas : `dataOrNull` / `errorOrNull` / booléens / `toScreenState` success+failure / `asScreenStateFlow` ordre `[Loading, Loaded, Error]`

---

## Migration des ViewModels vers `ScreenState<T>` (R-2.2)

R-2.2 migre les ViewModels legacy `(loading: Boolean, error: String?, data: T?)` vers `StateFlow<ScreenState<T>>`. Vague A = 4 VM critiques. La table suivante reflète l'état après Vague A.

### Inventaire

| VM | Status | Stratégie |
|---|---|---|
| `AuthViewModel` | ✅ Conforme (jamais legacy) | Sealed maison `AuthState` + `OtpUiState` — voir section suivante |
| `DriverSetupViewModel` | ✅ Migré (Vague A) | Single-flow `ScreenState<Unit>` |
| `DriverViewModel` (driverhome) | ✅ Migré (Vague A) | Multi-flow par domaine |
| `MapViewModel` | ⏸ Déféré (Vague C) | 1029 LOC / 47 StateFlow / 9 domaines — refactor en session dédiée |
| 6 VMs secondaires | ⏸ Vague B | `Wallet`, `Profile`, `NameEntry`, `Language`, `Role`, `Session` |

### `AuthViewModel` — Pattern sealed conforme antérieur à R-2.1

`AuthViewModel` n'a jamais utilisé le trio legacy. Il expose deux sealed maison qui couvrent fonctionnellement le contrat `ScreenState` :

```kotlin
// app/src/main/java/tn/dadadrive/presentation/auth/AuthState.kt
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

// app/src/main/java/tn/dadadrive/presentation/auth/AuthViewModel.kt
sealed class OtpUiState {
    object Idle : OtpUiState()
    object SendingOtp : OtpUiState()
    data class OtpSent(val phone: String) : OtpUiState()
    data class VerifyingOtp(val phone: String) : OtpUiState()
    data class Success(val token: String) : OtpUiState()
    data class Error(val message: String) : OtpUiState()
}
```

**Pourquoi ne pas l'aligner mécaniquement sur `ScreenState<User>` ?**
- `OtpUiState` porte des transitions métier spécifiques au flow OTP (`SendingOtp` / `OtpSent` / `VerifyingOtp`) qui n'existent pas dans `ScreenState<T>` générique. Forcer un sous-cas `Loaded<OtpStep>` perdrait l'exhaustivité du `when` sur chaque étape.
- `AuthState.Error(val message: String)` utilise un `String` brut au lieu d'un `PresentableError`. La migration vers `PresentableError` est intentionnellement déférée jusqu'à ce que les flows Auth aient besoin d'i18n granulaire (S5).

**Statut** : conforme au pattern par construction. Aucun refactor de fond requis. Re-évalué si :
- Les écrans Auth doivent consommer un message localisé par code backend (`BackendErrorCode`) → migrer `AuthState.Error` vers `PresentableError`.
- Le wizard OTP s'enrichit de nouvelles étapes (R-3.x post-Socket integration).

### Stratégies de migration — Vague A

**A.1 — `DriverSetupViewModel` : single-flow `ScreenState<Unit>`**

VM simple (wizard 3 steps → 1 submit). Une seule opération `submitDriverSetup` → un seul `_state: MutableStateFlow<ScreenState<Unit>>`. La valeur métier (`Vehicle`) est passée au callback `onComplete`, pas portée par le state. Voir [`DriverSetupViewModel.kt`](../app/src/main/java/tn/dadadrive/presentation/driversetup/DriverSetupViewModel.kt).

**A.2 — `DriverViewModel` : multi-flow par domaine**

VM complexe (toggle online + liste available rides + active ride + polling). Trois `ScreenState` flows indépendants :

```kotlin
val onlineState: StateFlow<ScreenState<Boolean>>            // toggle data + loading + error
val availableRidesState: StateFlow<ScreenState<List<AvailableRide>>>
val activeRideState: StateFlow<ScreenState<ActiveRide>>     // Idle = pas de course active
```

**Stratégie d'erreur par domaine** : chaque flow porte sa propre `ScreenState.Error`. Pas de flow global `errorMessage`. Le screen consume les 3 et applique une priorité d'affichage (active ride > online > available).

```kotlin
val errorMsg: String? =
    when (val s = activeRideState) {
        is ScreenState.Error -> s.error.message
        ScreenState.Idle, ScreenState.Loading, is ScreenState.Loaded ->
            onlineState.errorOrNull()?.message ?: availableRidesState.errorOrNull()?.message
    }
```

**UI transient state non migré** (`showAvailableRides`, `showActiveRide`, `completeResult`, `showCompleteResult`) : reste en `StateFlow<Boolean>` / `StateFlow<T?>` brut — ce sont des toggles UI, pas des cycles de données.

**Polling-aware Loading** : `fetchAvailableRides` n'émet `Loading` que si la liste est vide. Le polling périodique met à jour silencieusement la `Loaded(list)` pour éviter le flicker.

**Trade-off `ScreenState.Error` perd la donnée** : transition `Loaded(x) → Error` efface `x`. Pour les opérations qui doivent préserver l'état affiché (ex : start/cancel sur une ride active visible), le consommateur peut cacher la dernière valeur via `remember { mutableStateOf(...) }`. Voir [`DriverHomeScreen.kt`](../app/src/main/java/tn/dadadrive/presentation/driverhome/DriverHomeScreen.kt) (variante simplifiée actuelle : la sheet disparaît brièvement durant Error — acceptable, l'erreur reste visible en snackbar).

### Helpers réutilisés

- `state.dataOrNull()` / `state.errorOrNull()` — extraction sûre
- `state.isLoading` — booléen Compose-friendly pour conditionner un spinner
- `mapper.fromThrowable(e)` puis `ScreenState.Error(presentable)` — ou directement `result.toScreenState(mapper)`

### Tests Vague A

- [`DriverSetupViewModelTest`](../app/src/test/java/tn/dadadrive/presentation/driversetup/DriverSetupViewModelTest.kt) — 5 cas (initial Idle, submit success, vehicle failure, profile+vehicle failure avec override message, dismissError)
- [`DriverViewModelTest`](../app/src/test/java/tn/dadadrive/presentation/driverhome/DriverViewModelTest.kt) — 5 cas (initial state, toggle online success/failure, toggle online→offline clears, startRide failure isolé par domaine)
