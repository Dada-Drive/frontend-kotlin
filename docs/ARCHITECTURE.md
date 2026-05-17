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
