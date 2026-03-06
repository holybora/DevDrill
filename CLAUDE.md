# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.
Architecture and patterns are adapted from the [KotlinConf App](https://github.com/JetBrains/kotlinconf-app) — JetBrains' flagship KMP reference.

---

## Project Overview

DevDrill is a **Kotlin Multiplatform** (KMP) application targeting Android, iOS, and Server (Ktor).
Package: `com.sls.devdrill`. Early-stage project (v1.0) using shared Compose Multiplatform UI.

## Figma MCP Server Rules

- If the Figma MCP server returns a localhost source for an image or SVG, use that source directly
- DO NOT import/add new icon packages — all assets should come from the Figma payload
- Do NOT use or create placeholders if a localhost source is provided

---

## Build & Test Commands

```bash
# Android
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:assembleRelease
./gradlew :composeApp:installDebug            # Install on connected device/emulator

# Server (Ktor)
./gradlew :server:run                          # Starts on http://localhost:8080

# Tests — all modules
./gradlew test

# Module-specific tests
./gradlew :composeApp:testDebugUnitTest
./gradlew :shared:test
./gradlew :server:test
./gradlew :core:model:test

# Single test class
./gradlew :shared:testDebugUnitTest --tests "com.sls.devdrill.SomeTest"

# Clean build
./gradlew clean :composeApp:assembleDebug

# Static analysis (detekt)
./gradlew detekt                               # All modules
./gradlew :shared:detekt                       # Single module

# Code coverage (kover)
./gradlew test :composeApp:koverHtmlReport     # HTML → composeApp/build/reports/kover/html/
./gradlew test :composeApp:koverXmlReport      # XML  → composeApp/build/reports/kover/report.xml
```

---

## SDK & Tooling

| Tool | Version |
|------|---------|
| Kotlin | 2.3.0 |
| Compose Multiplatform | 1.10.0 |
| AGP | 8.11.2 |
| Compile/Target SDK | 36 |
| Min SDK | 24 |
| Java compatibility | 11 (JBR 21 for build) |
| Gradle | 8.14.3 |
| Ktor | 3.3.3 |
| Material3 (multiplatform) | 1.10.0-alpha05 |
| AndroidX Lifecycle | 2.9.6 |
| kotlinx-serialization | 1.8.1 |
| Navigation Compose | 2.9.1 (JetBrains multiplatform) |
| Koin | 4.1.x (planned) |
| kotlinx-datetime | 0.7.x (planned) |
| Multiplatform Settings | 1.3.0 (planned) |
| Coil | 3.x (planned) |
| Detekt | 1.23.x (planned) |
| Kover | 0.9.x (planned) |
| Logback (server) | 1.5.24 |

**Dependency versions:** `gradle/libs.versions.toml`

---

## Module Structure

### Current State

```
DevDrill/
├── composeApp/              # KMP Compose application (Android + iOS entry points)
│   └── src/
│       ├── commonMain/      # App.kt (NavHost), navigation/Routes.kt, screens/
│       ├── androidMain/     # MainActivity, Android resources
│       ├── iosMain/         # MainViewController (ComposeUIViewController bridge)
│       └── commonTest/
├── shared/                  # KMP library (Android, iOS, JVM)
│   └── src/
│       ├── commonMain/      # Platform interface, Greeting, Constants
│       ├── androidMain/     # Platform.android.kt
│       ├── iosMain/         # Platform.ios.kt
│       ├── jvmMain/         # Platform.jvm.kt
│       └── commonTest/
├── server/                  # Ktor JVM server (port 8080)
│   └── src/
│       ├── main/kotlin/     # Application.kt, routing
│       └── test/kotlin/     # Server tests
└── iosApp/                  # Xcode project (SwiftUI thin shell wrapping ComposeView)
```

### Target State (evolve toward)

```
DevDrill/
├── composeApp/              # KMP application shell (Android + iOS entry points, Koin init)
│   └── src/
│       ├── commonMain/      # App() root composable, startKoin initialization
│       ├── androidMain/     # MainActivity, platformModule (Android)
│       ├── iosMain/         # MainViewController, platformModule (iOS)
│       └── commonTest/
├── core/
│   ├── model/               # Pure KMP library: @Serializable data models
│   │   └── src/
│   │       └── commonMain/  # Data classes shared by client + server
│   └── designsystem/        # KMP Compose library: theme, colors, typography, components
│       └── src/
│           ├── commonMain/  # DevDrillTheme, Colors, Typography, Shapes, reusable components
│           └── androidMain/ # Dynamic color overrides (optional)
├── shared/                  # KMP library: business logic, screens, ViewModels, navigation
│   └── src/
│       ├── commonMain/      # Services, ViewModels, screens, navigation, networking, storage
│       ├── androidMain/     # Platform implementations
│       ├── iosMain/         # Platform implementations
│       ├── jvmMain/         # Platform implementations
│       └── commonTest/      # ViewModel + Service unit tests
├── server/                  # Ktor JVM server
│   └── src/
│       ├── main/kotlin/     # Application, routing, server Koin modules
│       └── test/kotlin/     # Ktor test-host tests
└── iosApp/                  # Xcode project (SwiftUI thin shell wrapping ComposeView)
```

---

## Dependency Graph

### Current

```
:composeApp → :shared (Android + iOS UI)
:server     → :shared (JVM server)
:shared     → (no external dependencies, pure KMP library)
```

### Target

```
:composeApp → :shared → :core:model
                      → :core:designsystem

:server → :core:model (data models only, no Compose/UI)

:core:designsystem → (Compose BOM only)
:core:model → (kotlinx-serialization, kotlinx-datetime only)
:shared → (no dependency on :composeApp or :server)
```

**Rules:**
- `:server` MUST NOT depend on `:shared` or `:core:designsystem` (avoids pulling in Compose)
- `:core:model` is a pure KMP library with zero UI dependencies
- `:shared` will contain all client business logic — screens, ViewModels, navigation, networking, storage
- Screens and navigation currently live in `:composeApp` and should migrate to `:shared` as the project grows

---

## Architecture Patterns

### Current State

- **Shared UI:** `App()` composable in `composeApp/commonMain` used by Android and iOS
- **Navigation:** Type-safe Navigation Compose with `@Serializable` routes in `composeApp`
- **Screens:** Stateless composables with navigation callback lambdas (no ViewModels yet)
- **Platform abstraction:** `expect/actual` for `Platform` interface in `:shared`
- **Server:** Ktor `embeddedServer(Netty)` with routing, shares `Greeting` class from `:shared`

### Target Layers

```
┌─────────────────────────────────────────────────────┐
│  composeApp (Platform Entry Points)                 │
│  MainActivity / MainViewController / Koin init      │
├─────────────────────────────────────────────────────┤
│  shared/screens/ (Presentation Layer)               │
│  @Composable screens + ViewModels                   │
├─────────────────────────────────────────────────────┤
│  shared/ (Domain/Service Layer)                     │
│  Service classes combining API + Storage            │
├─────────────────────────────────────────────────────┤
│  shared/network/ + shared/storage/ (Data Layer)     │
│  APIClient (Ktor) + ApplicationStorage (Settings)   │
├─────────────────────────────────────────────────────┤
│  core/model/ (Model Layer)                          │
│  @Serializable data classes                         │
└─────────────────────────────────────────────────────┘
```

### Data Flow (Unidirectional)

```
Screen (Composable)
  → ViewModel (user actions)
    → Service (business logic)
      → APIClient / Storage (data operations)
      ← Flow / StateFlow (reactive data)
    ← StateFlow<UiState> (transformed state)
  ← collectAsStateWithLifecycle() (UI rendering)
```

### MVVM with StateFlow

ViewModels expose a single `StateFlow<UiState>` using sealed class hierarchies:

```kotlin
sealed interface DrillUiState {
    data object Loading : DrillUiState
    data class Success(val drills: List<Drill>) : DrillUiState
    data class Error(val message: String) : DrillUiState
}

class DrillViewModel(
    private val service: DrillService,
) : ViewModel() {

    val uiState: StateFlow<DrillUiState> = service.drills
        .map { drills -> DrillUiState.Success(drills) }
        .catch { e -> emit(DrillUiState.Error(e.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DrillUiState.Loading,
        )
}
```

### Service Pattern

Adapted from KotlinConf's `ConferenceService`. A single service class combines networking and storage, exposing reactive `StateFlow` properties:

```kotlin
class DrillService(
    private val client: APIClient,
    private val storage: ApplicationStorage,
) {
    val drills: StateFlow<List<Drill>> = combine(
        client.fetchDrills(),
        storage.savedDrills(),
    ) { remote, local ->
        mergeDrills(remote, local)
    }.stateIn(
        scope = serviceScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )
}
```

### Platform Abstraction

Prefer **interfaces + Koin modules** over `expect/actual` for platform-specific code. Use `expect/actual` only for simple declarations (e.g., `getPlatformName()`). For services with platform-specific implementations, define an interface in `commonMain` and register platform implementations in each platform's Koin module:

```kotlin
// commonMain — interface
interface LocalNotificationService {
    fun schedule(title: String, body: String, delayMs: Long)
}

// androidMain — register in platformModule
single<LocalNotificationService> { AndroidNotificationService(get()) }

// iosMain — register in platformModule
single<LocalNotificationService> { IOSNotificationService() }
```

---

## Package Structure

### Current

```
composeApp/src/commonMain/kotlin/com/sls/devdrill/
├── navigation/
│   └── Routes.kt                # @Serializable route objects (HomeRoute, GreetingRoute)
├── screens/
│   ├── HomeScreen.kt            # Home screen composable
│   └── GreetingScreen.kt        # Greeting screen composable
└── App.kt                       # Root composable (MaterialTheme + NavHost)

composeApp/src/androidMain/kotlin/com/sls/devdrill/
└── MainActivity.kt              # Single Activity, enableEdgeToEdge, setContent { App() }

composeApp/src/iosMain/kotlin/com/sls/devdrill/
└── MainViewController.kt        # ComposeUIViewController { App() }

shared/src/commonMain/kotlin/com/sls/devdrill/
├── Platform.kt                  # Platform interface + expect fun getPlatform()
├── Greeting.kt                  # Greeting class using Platform
└── Constants.kt                 # SERVER_PORT = 8080

server/src/main/kotlin/com/sls/devdrill/
└── Application.kt               # embeddedServer(Netty), routing
```

### Target (evolve toward)

```
composeApp/src/commonMain/kotlin/com/sls/devdrill/
└── App.kt                       # Root composable (theme + Koin init + NavHost)

composeApp/src/androidMain/kotlin/com/sls/devdrill/
├── MainActivity.kt              # Single Activity, enableEdgeToEdge, setContent { App() }
└── di/
    └── PlatformModule.android.kt # Android Koin platformModule

composeApp/src/iosMain/kotlin/com/sls/devdrill/
├── MainViewController.kt        # ComposeUIViewController { App() }
└── di/
    └── PlatformModule.ios.kt     # iOS Koin platformModule

shared/src/commonMain/kotlin/com/sls/devdrill/
├── di/                          # Koin module definitions
│   ├── AppModule.kt             # appModule: services, clients, storage
│   └── ViewModelModule.kt       # viewModelModule: all ViewModels
├── navigation/                  # Navigation
│   ├── Routes.kt                # @Serializable route objects
│   └── DevDrillNavHost.kt       # NavHost composable
├── network/                     # Networking
│   └── APIClient.kt             # Ktor HttpClient configuration + API methods
├── storage/                     # Local persistence
│   ├── ApplicationStorage.kt    # Interface: Flow<T> getters, suspend setters
│   └── SettingsStorage.kt       # Multiplatform Settings implementation
├── screens/                     # Feature screens (co-located Screen + ViewModel)
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── drill/
│   │   ├── DrillScreen.kt
│   │   └── DrillViewModel.kt
│   └── settings/
│       ├── SettingsScreen.kt
│       └── SettingsViewModel.kt
├── DrillService.kt              # Central business logic service
└── Platform.kt                  # Platform interface (if still needed with Koin)

core/model/src/commonMain/kotlin/com/sls/devdrill/model/
├── Drill.kt                     # @Serializable data class
├── User.kt                      # @Serializable data class
├── DrillId.kt                   # @JvmInline value class DrillId(val value: String)
└── ...

core/designsystem/src/commonMain/kotlin/com/sls/devdrill/designsystem/
├── theme/
│   ├── DevDrillTheme.kt         # Theme composable + CompositionLocal accessors
│   ├── Colors.kt                # Custom color tokens (light + dark)
│   ├── Typography.kt            # Custom text styles
│   └── Shapes.kt                # Shape definitions
└── components/                  # Reusable UI components
    ├── DevDrillButton.kt
    ├── DevDrillCard.kt
    └── ...

server/src/main/kotlin/com/sls/devdrill/
├── Application.kt               # embeddedServer(Netty), Koin setup
├── routes/                      # Route handlers
│   └── DrillRoutes.kt
└── di/
    └── ServerModule.kt           # Server-specific Koin module
```

---

## Dependency Injection (Koin)

### Module Structure

Three Koin module types, following the KotlinConf App pattern:

```kotlin
// shared/di/AppModule.kt — singletons: services, clients, storage
val appModule = module {
    single { APIClient(baseUrl = "https://api.devdrill.app") }
    single<ApplicationStorage> { SettingsStorage(settings = get()) }
    single { DrillService(client = get(), storage = get()) }
}

// shared/di/ViewModelModule.kt — ViewModels
val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::DrillViewModel)
    viewModelOf(::SettingsViewModel)
}

// androidMain/di/PlatformModule.android.kt — platform-specific
actual fun platformModule(): Module = module {
    single<ObservableSettings> {
        SharedPreferencesSettings(
            PreferenceManager.getDefaultSharedPreferences(androidContext())
        )
    }
}

// iosMain/di/PlatformModule.ios.kt
actual fun platformModule(): Module = module {
    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
}
```

### Initialization

```kotlin
// composeApp/commonMain — App.kt or AppInit.kt
fun initKoin() {
    startKoin {
        modules(platformModule(), appModule, viewModelModule)
    }
}
```

- **Android:** Call `initKoin()` in `MainActivity.onCreate()` before `setContent`
- **iOS:** Call `initKoin()` in `MainViewController()` before `ComposeUIViewController`
- **Server:** Use `install(Koin)` Ktor plugin with `serverModule`

### Injection in Composables

```kotlin
@Composable
fun DrillScreen(viewModel: DrillViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

---

## Networking Patterns

### Ktor Client Setup

```kotlin
class APIClient(private val baseUrl: String) {
    val client = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(HttpTimeout) { requestTimeoutMillis = 5_000 }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }
        install(Logging) { level = LogLevel.HEADERS }
        defaultRequest { url(baseUrl) }
    }
}
```

### Safe API Call Wrapper

Catch all exceptions except `CancellationException`, log errors, return `null` on failure:

```kotlin
suspend inline fun <reified T> APIClient.safeApiCall(
    block: HttpClient.() -> HttpResponse,
): T? = try {
    client.block().body<T>()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Log.e("APIClient", "API call failed", e)
    null
}
```

### Platform Engines

- **Android / JVM:** `OkHttp`
- **iOS:** `Darwin`

Register via Koin `platformModule` or Ktor's automatic engine resolution.

---

## Storage / Persistence Patterns

### Interface

```kotlin
interface ApplicationStorage {
    fun observeDrillProgress(): Flow<Map<DrillId, DrillProgress>>
    suspend fun saveDrillProgress(id: DrillId, progress: DrillProgress)
    fun observeTheme(): Flow<ThemeMode>
    suspend fun setTheme(mode: ThemeMode)
}
```

### Implementation

Use `multiplatform-settings` with `coroutines` and `serialization` extensions:

```kotlin
class SettingsStorage(
    private val settings: ObservableSettings,
    private val json: Json = Json,
) : ApplicationStorage {

    override fun observeDrillProgress(): Flow<Map<DrillId, DrillProgress>> =
        settings.getStringOrNullFlow(KEY_PROGRESS)
            .map { raw -> raw?.let { json.decodeFromString(it) } ?: emptyMap() }

    override suspend fun saveDrillProgress(id: DrillId, progress: DrillProgress) {
        val current = /* read current map */ ...
        val updated = current + (id to progress)
        settings.putString(KEY_PROGRESS, json.encodeToString(updated))
    }
}
```

**When to use SQLDelight instead:** If the app needs relational queries, full-text search, or stores >100 entities. For key-value preferences and serialized state, `multiplatform-settings` is sufficient.

---

## Navigation Patterns

Navigation is implemented using **JetBrains Navigation Compose** (`2.9.1`) with `kotlinx-serialization` for type-safe routes.

### Current Implementation

Routes are defined as top-level `@Serializable object` declarations in `composeApp/.../navigation/Routes.kt`:

```kotlin
// navigation/Routes.kt
@Serializable
object HomeRoute

@Serializable
object GreetingRoute
```

The `NavHost` is configured in `App.kt` with `composable<Route>` type-safe builders:

```kotlin
// App.kt
@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        Surface(...) {
            NavHost(navController = navController, startDestination = HomeRoute) {
                composable<HomeRoute> {
                    HomeScreen(
                        onNavigateToGreeting = { navController.navigate(GreetingRoute) },
                    )
                }
                composable<GreetingRoute> {
                    GreetingScreen(
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
```

### Route Conventions

- **Parameterless routes:** `@Serializable object {Name}Route`
- **Parameterized routes:** `@Serializable data class {Name}Route(val param: String)`
- **All routes** in a single `Routes.kt` file
- **Screen composables** receive navigation callbacks as lambda parameters (not `NavController`)

### Adding a New Route

```kotlin
// 1. Define route in Routes.kt
@Serializable
data class DrillDetailRoute(val drillId: String)

// 2. Add composable in NavHost (App.kt)
composable<DrillDetailRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<DrillDetailRoute>()
    DrillDetailScreen(drillId = route.drillId, onNavigateBack = { navController.popBackStack() })
}

// 3. Navigate from another screen
navController.navigate(DrillDetailRoute(drillId = "123"))
```

---

## UI / Theming Patterns

### Custom Theme Wrapping Material3

```kotlin
@Composable
fun DevDrillTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors

    CompositionLocalProvider(LocalDevDrillColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(),
            typography = DevDrillTypography,
            shapes = DevDrillShapes,
            content = content,
        )
    }
}

object DevDrillTheme {
    val colors: DevDrillColors @Composable get() = LocalDevDrillColors.current
}
```

### Design Tokens

- Define semantic color properties (`background`, `surface`, `primaryText`, `accent`, etc.)
- Provide both light and dark variants
- Use `CompositionLocal` for theme access throughout the tree
- Coil `AsyncImage` for network images with shared image loader

### Edge-to-Edge

- Android: `enableEdgeToEdge()` in `MainActivity.onCreate()`
- iOS: `.ignoresSafeArea()` in SwiftUI `ContentView`
- Compose: `Modifier.safeContentPadding()` or `Modifier.windowInsetsPadding()` for content

---

## Error Handling Patterns

### API Layer

`safeApiCall` catches all exceptions (except `CancellationException`), logs them, returns `null`. The service layer treats `null` as "no data available" and falls back to cached/local data.

### ViewModel Layer

Map data presence to sealed `UiState`:

```kotlin
val uiState: StateFlow<UiState> = service.data
    .map { data ->
        if (data.isNotEmpty()) UiState.Success(data)
        else UiState.Empty
    }
    .catch { emit(UiState.Error(it.message ?: "Something went wrong")) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)
```

### UI Layer

Each screen handles all UiState variants:

```kotlin
when (val state = uiState) {
    is UiState.Loading -> LoadingIndicator()
    is UiState.Success -> ContentView(state.data)
    is UiState.Error -> ErrorView(state.message, onRetry = { viewModel.retry() })
    is UiState.Empty -> EmptyStateView()
}
```

---

## Testing Strategy

### What to Test

| Layer | What | Tools |
|-------|------|-------|
| ViewModels | State transitions, user actions | `kotlinx-coroutines-test`, `Turbine` |
| Services | Data merging, flow emissions | `kotlinx-coroutines-test`, `multiplatform-settings-test` |
| Server routes | HTTP status codes, response bodies | `ktor-server-test-host` |
| Models | Serialization round-trips | `kotlin-test`, `kotlinx-serialization` |

### Test Naming

```
functionName_condition_expectedResult
```

Examples:
- `fetchDrills_networkError_emitsErrorState`
- `saveDrillProgress_validInput_updatesStorage`
- `uiState_initialLoad_isLoading`

### ViewModel Test Example

```kotlin
@Test
fun uiState_afterSuccessfulFetch_emitsSuccess() = runTest {
    val service = FakeDrillService(drills = listOf(testDrill))
    val viewModel = DrillViewModel(service)

    viewModel.uiState.test {
        assertEquals(DrillUiState.Loading, awaitItem())
        assertEquals(DrillUiState.Success(listOf(testDrill)), awaitItem())
    }
}
```

### Coverage Target

- 70% on business logic (services, ViewModels)
- 50% overall

---

## Coding Conventions

### Kotlin Style

- **Code style:** Kotlin official (`kotlin.code.style=official` in `gradle.properties`)
- **Max line length:** 120 characters
- **Imports:** No wildcard imports (except `kotlinx.coroutines.*`)
- **Trailing commas:** Always use on multi-line declarations

### Naming Conventions

| Type | Convention | Example |
|------|-----------|---------|
| Screen composable | `{Feature}Screen.kt` | `HomeScreen.kt` |
| ViewModel | `{Feature}ViewModel.kt` | `HomeViewModel.kt` |
| Service | `{Domain}Service.kt` | `DrillService.kt` |
| Data model | `{Entity}.kt` (singular) | `Drill.kt` |
| Value class ID | `{Entity}Id.kt` | `DrillId.kt` |
| Route objects | `Routes.kt` | Single file, `{Name}Route` naming |
| DI module | `{Scope}Module.kt` | `AppModule.kt` |
| Platform implementation | `PlatformModule.{platform}.kt` | `PlatformModule.android.kt` |
| Storage interface | `{Name}Storage.kt` | `ApplicationStorage.kt` |
| API client | `APIClient.kt` | Single file |
| Theme | `DevDrillTheme.kt` | Single file |
| Composable functions | PascalCase | `DrillCard()` |

### ViewModel State Pattern

```kotlin
class FooViewModel : ViewModel() {
    // Expose immutable StateFlow, never MutableStateFlow
    val uiState: StateFlow<FooUiState> = ...

    // For one-shot events, use Channel + receiveAsFlow
    private val _events = Channel<FooEvent>(Channel.BUFFERED)
    val events: Flow<FooEvent> = _events.receiveAsFlow()
}
```

### Composable Conventions

- Screens receive navigation callbacks as lambda parameters (`onNavigateBack: () -> Unit`)
- When ViewModels are added: screens receive a `ViewModel` parameter with default `koinViewModel()`
- Stateless components receive data + callbacks, no ViewModel
- Use `Modifier` as first optional parameter for reusable components

---

## Git Workflow

- **Branch prefixes:** `feature/`, `bugfix/`, `chore/`, `refactor/`
- **Commit messages:** Imperative mood, present tense ("Add drill list screen", not "Added")
- **PR-based workflow:** All changes via PR to `main`
- **No force pushes** to `main`

---

## CI/CD (GitHub Actions)

Target workflow with parallel jobs:

| Job | Runs |
|-----|------|
| `build-android` | `./gradlew :composeApp:assembleDebug` |
| `build-server` | `./gradlew :server:build` |
| `test-jvm` | `./gradlew test` |
| `lint` | `./gradlew detekt` |

- **Triggers:** PR and push to `main`
- **JDK:** 21 (Zulu)
- **Caching:** Gradle dependencies + build outputs
- **Concurrency:** Cancel in-progress runs for same PR

---

## Code Quality

### Detekt

- Detekt with Compose rules (`io.nlopez.compose.rules:detekt`) for Composable best practices
- Run: `./gradlew detekt`
- Config: `config/detekt/detekt.yml`

### Kover

- Code coverage aggregated in `:composeApp` module
- Exclude generated code, Koin modules, Compose `@Composable` functions from coverage
- Run: `./gradlew test :composeApp:koverHtmlReport`

---

## Notes

- No build-logic convention plugins — standard KMP Gradle setup (matches KotlinConf App)
- **Navigation is implemented** using JetBrains Navigation Compose 2.9.1 with `@Serializable` type-safe routes and `kotlinx-serialization` 1.8.1 (not Navigation3 alpha); migrate when Navigation3 stabilizes
- Screens and navigation currently live in `:composeApp`; migrate to `:shared` when adding ViewModels and services
- No DI framework yet — add Koin when introducing ViewModels and services
- No state management beyond Compose `remember` — add ViewModel + StateFlow per the MVVM pattern above
- Storage uses Multiplatform Settings (key-value) when added; use SQLDelight only if relational queries are needed
- No CI/CD, linting, or code coverage tooling configured yet
- iOS build requires Xcode; CI iOS builds are optional until the project matures
