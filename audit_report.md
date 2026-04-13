# iStory Codebase Audit Report

## SOURCE MANIFEST
Files read: 139 files
Tech stack: Android, Kotlin 1.9+, Jetpack Compose, Room, Hilt, Navigation Component, Gemini/Unified AI
Architecture pattern: Multi-module layered architecture (Features + Core + Domain + Data)
Entry flow: MainActivity → initializes SecureApiKeyStorage → StoryBuilderNavHost → AgeGateScreen / ApiProviderSetup / StoryLibraryScreen
State management: ViewModel with unidirectional data flow (Compose State)
Settings system: EncryptedSharedPreferences (SecureApiKeyStorage), Preferences DataStore for User settings

---

## [Phase 2] Category Audit

### Logic & Architecture

### [LOGIC-1] Global API Key Desynchronization
**File(s):** `src/app/src/main/java/com/storybuilder/app/MainActivity.kt`
**Current behavior:** API key is initialized into the static `ApiKeyProvider` once sequentially inside `onCreate()`.
**Problem:** If the user updates or changes the API key within `ApiProviderSetupScreen`, the global singleton `ApiKeyProvider` is not updated synchronously, causing generation to fail until the app is forcefully restarted.
**Proposed fix:** Listen to `SecureApiKeyStorage.activeKeyFlow` or update `ApiKeyProvider` dynamically during the save action in `ApiProviderSetupViewModel`.
**Impact:** High
**Effort:** Low
**Priority score:** 7

### [LOGIC-2] Unscoped Background Coroutines
**File(s):** `src/data/ai/src/main/java/com/storybuilder/data/ai/repository/StoryAIRepositoryImpl.kt` (line 42)
**Current behavior:** Background network requests are launched globally using `CoroutineScope(Dispatchers.IO).launch { ... }`.
**Problem:** The coroutine is completely detached from the application lifecycle, meaning navigating away or canceling the task will not cancel the underlying network request or DB write, leaking memory and resources.
**Proposed fix:** Inject a `@Singleton` `ApplicationScope` bound to the app's lifecycle, or use `suspend` functions directly and rely on `viewModelScope`.
**Impact:** High
**Effort:** Low
**Priority score:** 7

### [LOGIC-3] Legacy API Key Leakage
**File(s):** `src/app/src/main/java/com/storybuilder/app/data/storage/SecureApiKeyStorage.kt`
**Current behavior:** `clearApiKey(provider)` removes the provider's explicit key but leaves `KEY_GEMINI_API_KEY` (the legacy key) intact.
**Problem:** A user who believes they have deleted their credentials might still have them actively stored and loaded fallback via `getApiKey()`, compromising privacy.
**Proposed fix:** Execute `remove(KEY_GEMINI_API_KEY)` alongside the provider-specific clear actions in `clearApiKey()`.
**Impact:** High
**Effort:** Low
**Priority score:** 7

### [LOGIC-4] Missing DB Migrations Strategy
**File(s):** `src/data/local/src/main/java/com/storybuilder/data/local/database/AppDatabase.kt`
**Current behavior:** Database is built via `Room.databaseBuilder()` without a fallback mechanism or explicit migrations mapping.
**Problem:** When releasing Phase 2 which alters schema tables, existing installations will immediately crash with `IllegalStateException` on startup.
**Proposed fix:** Add `.fallbackToDestructiveMigration()` for pre-production, and set up empty `Migration(1,2)` structures for production.
**Impact:** High
**Effort:** Low
**Priority score:** 7

### [LOGIC-5] String-based Navigation Arguments
**File(s):** `src/app/src/main/java/com/storybuilder/app/navigation/StoryBuilderNavHost.kt`
**Current behavior:** Complex enumerations like `storyId` or `pacingName` are passed via untyped string paths and `savedStateHandle`.
**Problem:** Hardcoded string references `"pacing"`, `"darknessLevel"` are brittle and will silently miscast or return null if property names change.
**Proposed fix:** Migrate to TypeSafe Compose Navigation by defining Kotlin `serializable` classes/objects for route arguments.
**Impact:** Medium
**Effort:** Medium
**Priority score:** 3

### [LOGIC-6] Mismanaged Backstack Reset
**File(s):** `src/app/src/main/java/com/storybuilder/app/navigation/StoryBuilderNavHost.kt`
**Current behavior:** `popUpTo(Screen.AgeGate.route) { inclusive = true }` is used to advance state.
**Problem:** Because `startDestination` is computed dynamically, the root backstack might not actually be `AgeGate`. This can result in an empty stack meaning the next back press exits the app improperly.
**Proposed fix:** Pop up to the root graph ID dynamically via `popUpTo(navController.graph.id)`.
**Impact:** Medium
**Effort:** Low
**Priority score:** 5

### [LOGIC-7] Synchronous DB Setup Blockage
**File(s):** `src/data/local/src/main/java/com/storybuilder/data/local/di/LocalDataModule.kt` (line 49)
**Current behavior:** Pre-population queries are triggered directly alongside the instance creation using `launch`.
**Problem:** Asynchronous calls injected at module creation create race conditions where UI might query the `GenreDao` before the prepopulation routine finishes.
**Proposed fix:** Implement Room's `RoomDatabase.Callback().onCreate` to guarantee database schema executes the initial insertions cleanly.
**Impact:** Medium
**Effort:** Medium
**Priority score:** 3

### [LOGIC-8] Incomplete Auto-Save Job Cancellation
**File(s):** `src/data/local/src/main/java/com/storybuilder/data/local/autosave/AutoSaveManager.kt` (line 28)
**Current behavior:** Auto-save instantiates its own `CoroutineScope(Dispatchers.IO)`.
**Problem:** Hardcoded IO dispatchers prevent injecting TestDispatchers for unit testing, causing erratic test failures and potential production memory leaks.
**Proposed fix:** Pass an isolated `@IoDispatcher` into the AutoSaveManager constructor via Hilt.
**Impact:** Medium
**Effort:** Low
**Priority score:** 5

### [LOGIC-9] Fragile Route Initialization
**File(s):** `src/feature/character-create/src/main/java/com/storybuilder/feature/charactercreate/CharacterCreateScreen.kt`
**Current behavior:** On completion, navigates to `ChatPlayer.createRoute("new")`.
**Problem:** Bypassing repository creation by passing the magic string `"new"` pushes database responsibility down to the Chat Player, breaking separation of concerns.
**Proposed fix:** The view model for Character Creation should execute `createStory()` and pass the genuine UUID route forward.
**Impact:** High
**Effort:** Medium
**Priority score:** 5

### [LOGIC-10] Silent Failure of TTS Initialization
**File(s):** `src/data/tts/src/main/java/com/storybuilder/data/tts/TtsManager.kt`
**Current behavior:** Initialization relies on native text-to-speech engine availability without explicit callbacks properly propagating to UI.
**Problem:** If standard Google TTS is disabled or missing on an Android device, the user will experience dead silent narration without feedback.
**Proposed fix:** Emit a `TtsState.Error` flow when `OnInitListener` returns `ERROR`, allowing the UI to present a warning snippet.
**Impact:** Medium
**Effort:** Low
**Priority score:** 5

---

### UI / Visual Design

### [UI-1] Renders Huge Build Artifacts
**File(s):** `src/app/build.gradle.kts`
**Current behavior:** `isMinifyEnabled` is hardcoded to `false` in the release block.
**Problem:** The app is shipped without ProGuard shrinking or resource minification, resulting in massive install sizes and trivial decompilation.
**Proposed fix:** Switch to `isMinifyEnabled = true`, configure explicit `proguard-rules.pro`, and enable `isShrinkResources = true`.
**Impact:** High
**Effort:** Low
**Priority score:** 7

### [UI-2] Inflexible AgeGate Buttons
**File(s):** `src/app/src/main/java/com/storybuilder/app/screens/agegate/AgeGateScreen.kt`
**Current behavior:** The 'Continue' button explicitly declares `height(56.dp)`.
**Problem:** At high system font scalings, standard sized text will overlap or clip off the edge because height cannot expand.
**Proposed fix:** Change `height(56.dp)` to `minHeight(56.dp)` and allow flexible intrinsic expansion.
**Impact:** Medium
**Effort:** Low
**Priority score:** 5

### [UI-3] Unconstrained Gradient Painting
**File(s):** `src/app/src/main/java/com/storybuilder/app/screens/agegate/AgeGateScreen.kt`
**Current behavior:** `Box` sets `brush = Brush.verticalGradient()` but uses unrestricted height calculations beneath `Scaffold`.
**Problem:** On devices with different aspect ratios, the scrollable content might push the gradient's visual termination boundary strangely, causing banding at the bottom.
**Proposed fix:** Add `fillMaxSize()` logic properly to the constrained background box rather than dynamically calculating it with nested scroll modifiers.
**Impact:** Medium
**Effort:** Low
**Priority score:** 5

### [UI-4] Hardcoded Theming Colors
**File(s):** `src/app/src/main/java/com/storybuilder/app/screens/agegate/AgeGateScreen.kt`
**Current behavior:** Hardcodes the color `Color(0xFF1A0A0A)` into a gradient chain.
**Problem:** Using random hex colors ignores Material 3 semantic tokens and breaks if the app supports a strict light theme.
**Proposed fix:** Abstract this to `MaterialTheme.colorScheme.scrim` or defined theme extension tokens.
**Impact:** Low
**Effort:** Low
**Priority score:** 3

### [UI-5] Broken Edge-To-Edge Presentation
**File(s):** `src/feature/story-library/src/main/java/com/storybuilder/feature/storylibrary/StoryLibraryScreen.kt`
**Current behavior:** Screens do not consume WindowInsets manually.
**Problem:** Edge-to-edge drawing is enabled globally, leading to lists dipping into or vanishing beneath system navigation gesture bars.
**Proposed fix:** Apply `Modifier.windowInsetsPadding(WindowInsets.systemBars)` or `contentWindowInsets` explicitly across the inner container.
**Impact:** High
**Effort:** Low
**Priority score:** 7

### [UI-6] Unlabeled Slider Scale
**File(s):** `src/feature/genre-select/src/main/java/com/storybuilder/feature/genres/GenreSelectScreen.kt`
**Current behavior:** The Darkness factor provides a continuous slider.
**Problem:** Users have absolute zero reference visually on what point of the 1..10 scale they reside, diminishing precise creative control.
**Proposed fix:** Display `Text("${sliderValue.toInt()}/10")` overlapping or adjacent to the slider component.
**Impact:** Low
**Effort:** Low
**Priority score:** 3

### [UI-7] Generically Placed Chat Bubbles
**File(s):** `src/feature/chat-player/src/main/java/com/storybuilder/feature/chatplayer/components/UserBubble.kt`
**Current behavior:** Chat bubbles take up most horizontal space dynamically.
**Problem:** Without constrained side padding on alternating sides, the visual flow of conversation loses its "me vs them" iMessage readability.
**Proposed fix:** Apply `padding(end = 64.dp)` to Narrator messages and `padding(start = 64.dp)` to User messages.
**Impact:** Medium
**Effort:** Low
**Priority score:** 5

### [UI-8] No "Generation" Indicator
**File(s):** `src/feature/chat-player/src/main/java/com/storybuilder/feature/chatplayer/ChatPlayerScreen.kt`
**Current behavior:** When waiting for an API response, the screen remains static.
**Problem:** Without an active loading visual, users immediately presume the frozen screen means the application has soft-locked.
**Proposed fix:** Build an animated "..." typography composable within a temporary bubble while `isGenerating` is true.
**Impact:** High
**Effort:** Medium
**Priority score:** 5

### [UI-9] Option Stack Occlusion
**File(s):** `src/feature/option-selection/src/main/java/com/storybuilder/feature/optionselection/OptionCards.kt`
**Current behavior:** Option cards populate as massive vertical blocks anchoring to the bottom.
**Problem:** Showing three large options frequently pushes the narrative query entirely offscreen, preventing users from re-reading what they're deciding.
**Proposed fix:** Encapsulate Option blocks within a horizontal Pager or constrained BottomSheet to ensure UI harmony.
**Impact:** High
**Effort:** Medium
**Priority score:** 5

### [UI-10] Deprecated Splash Screen
**File(s):** `src/app/src/main/java/com/storybuilder/app/MainActivity.kt`
**Current behavior:** No integration of the Android 12+ App Splash Screen API.
**Problem:** White frame flashes occur during cold boots before Compose attaches its views, which feels disjointed natively.
**Proposed fix:** Add `implementation("androidx.core:core-splashscreen:$version")` and configure the theme attribute properly in XML.
**Impact:** Low
**Effort:** Low
**Priority score:** 3

---

### UX / User Experience

### [UX-1] Immediate Error Feedback Omission
**File(s):** `src/app/src/main/java/com/storybuilder/app/screens/agegate/AgeGateScreen.kt`
**Current behavior:** Tapping the checkbox validates age, but `showError` state remains persistently active if triggered previously.
**Problem:** Confuses users who follow error instructions but see the red warning text persist until they finally hit Continue.
**Proposed fix:** Update `onCheckedChange` lambda to automatically assign `showError = false` upon `true`.
**Impact:** Low
**Effort:** Low
**Priority score:** 3

### [UX-2] Destructive Back Press
**File(s):** `src/feature/character-create/src/main/java/com/storybuilder/feature/charactercreate/CharacterCreateScreen.kt`
**Current behavior:** The system back button abruptly pops the navigational stack instantly.
**Problem:** If a user spends 5 minutes writing a custom backstory and accidentally grazes their screen edge, everything is erased permanently.
**Proposed fix:** Define a `BackHandler` with an AlertDialog prompt warning "Discard Character? Unsaved changes will be lost." if inputs are filled.
**Impact:** High
**Effort:** Low
**Priority score:** 7

### [UX-3] Configuration Change Wipes
**File(s):** `src/feature/text-input/src/main/java/com/storybuilder/feature/textinput/TextInputBar.kt`
**Current behavior:** Custom free-text is tracked with `mutableStateOf("")`.
**Problem:** Rotating the screen drops all keyboard input in the bar instantly, intensely frustrating mobile typists.
**Proposed fix:** Utilize `rememberSaveable { mutableStateOf("") }` to persist local values through rotation and decomposition.
**Impact:** High
**Effort:** Low
**Priority score:** 7

### [UX-4] Unresponsive Auto-Scroll
**File(s):** `src/feature/chat-player/src/main/java/com/storybuilder/feature/chatplayer/ChatPlayerScreen.kt`
**Current behavior:** LazyColumn does not observe appendations dynamically when the keyboard is dismissed or API answers return.
**Problem:** The user has to manually flick the scrollbar downward to uncover the AI's response every generation.
**Proposed fix:** Employ a `LaunchedEffect(messages.size)` invoking `listState.animateScrollToItem(messages.size - 1)`.
**Impact:** High
**Effort:** Low
**Priority score:** 7

### [UX-5] Sterile Empty States
**File(s):** `src/feature/story-library/src/main/java/com/storybuilder/feature/storylibrary/StoryLibraryScreen.kt`
**Current behavior:** If the local repository is empty, the screen displays a blank layout.
**Problem:** Fails to confidently direct the newly onboarded user toward creating their first experience.
**Proposed fix:** Draw an empty state container featuring an actionable graphic stating "You haven't forged any stories yet." pointing directly to a CTA.
**Impact:** Medium
**Effort:** Low
**Priority score:** 5

### [UX-6] Unforgiving Deletions
**File(s):** `src/feature/story-library/src/main/java/com/storybuilder/feature/storylibrary/StoryLibraryScreen.kt`
**Current behavior:** "Delete" performs an instantaneous database wipe.
**Problem:** A misclick eliminates hours of story gameplay without warning.
**Proposed fix:** Intercept the delete with a Snackbar stating "Story archived." and utilizing `.setAction("Undo")` for a 5 second grace period.
**Impact:** High
**Effort:** Medium
**Priority score:** 5

### [UX-7] Secret API Key Exposure
**File(s):** `src/app/src/main/java/com/storybuilder/app/screens/apiprovider/ApiProviderSetupScreen.kt`
**Current behavior:** Settings exposes the API text-field directly.
**Problem:** In proximity environments, someone can memorize or visually steal the plaintext API token off the user's screen.
**Proposed fix:** Change `TextField`'s `visualTransformation` to `PasswordVisualTransformation()`.
**Impact:** High
**Effort:** Low
**Priority score:** 7

### [UX-8] Keyboard Paste Friction
**File(s):** `src/app/src/main/java/com/storybuilder/app/screens/apiprovider/ApiProviderSetupScreen.kt`
**Current behavior:** Passing long API keys requires awkward long-press system menus.
**Problem:** Onboarding users quit out when API copying proves tedious with floating clipboards.
**Proposed fix:** Incorporate a direct `IconButton(onClick = { // ClipboardManager API })` attached to the `trailingIcon` to populate automatically.
**Impact:** Medium
**Effort:** Low
**Priority score:** 5

### [UX-9] Silent Offline Failure
**File(s):** `src/feature/chat-player/src/main/java/com/storybuilder/feature/chatplayer/ChatPlayerScreen.kt`
**Current behavior:** Generation crashes implicitly if connectivity fails during waiting stages.
**Problem:** Losing the text the user just typed into the void is a horrific user journey.
**Proposed fix:** Surface network failures gracefully via UI state, and automatically reinject the user's failed query into the input bar for retry.
**Impact:** High
**Effort:** Medium
**Priority score:** 5

### [UX-10] Tap Target Intersection
**File(s):** `src/app/src/main/java/com/storybuilder/app/screens/agegate/AgeGateScreen.kt`
**Current behavior:** The `Card` and its nested `Checkbox` both carry distinct tap ripple receptors.
**Problem:** Clicking precisely on the little checkbox absorbs the click while the bigger card assumes nothing occurred, duplicating material ripples visually.
**Proposed fix:** Set `Checkbox(..., interactionSource = remember { MutableInteractionSource() })` and enforce event hoisting onto the `Row` definitively.
**Impact:** Low
**Effort:** Low
**Priority score:** 3

---

### Functionality & Features

### [FUNC-1] LLM Markdown Encapsulation
**File(s):** `src/data/ai/src/main/java/com/storybuilder/data/ai/ResponseParser.kt`
**Current behavior:** System decodes `Json.decodeFromString()` immediately.
**Problem:** A vast majority of language models automatically enclose JSON inside ` ```json ... ``` ` tags anyway, preventing the raw string parser from deserializing.
**Proposed fix:** Apply strict regex `.replace("```json\\s|".toRegex(), "").replace("```$".toRegex(), "")` wrapping before execution.
**Impact:** High
**Effort:** Low
**Priority score:** 7

### [FUNC-2] Missing Haptic Integration
**File(s):** `src/feature/option-selection/src/main/java/com/storybuilder/feature/optionselection/OptionCards.kt`
**Current behavior:** Option Card items respond to standard clicks silently.
**Problem:** Fails to address the Architecture's requirement for immersive tactile feedback on branch selection.
**Proposed fix:** Assign `LocalHapticFeedback.current` and trigger `.performHapticFeedback()` per Option invocation.
**Impact:** Low
**Effort:** Low
**Priority score:** 3

### [FUNC-3] Aggressive Network Validation
**File(s):** `src/domain/src/main/java/com/storybuilder/domain/repository/StoryRepository.kt`
**Current behavior:** Flow fetching occasionally defaults toward network reliance.
**Problem:** Blocks users from interacting securely with their cached archives on an airplane.
**Proposed fix:** Assert Offline-First doctrine in `LocalStoryRepositoryImpl` by observing standard room database flows indiscriminately without online checks.
**Impact:** Medium
**Effort:** Low
**Priority score:** 5

### [FUNC-4] Microphone Rejection
**File(s):** `src/feature/text-input/src/main/java/com/storybuilder/feature/textinput/TextInputBar.kt`
**Current behavior:** If STT invokes but runtime permissions are ungranted, the request defaults.
**Problem:** Silent microphone block equates to broken functionality in the eyes of layman operators.
**Proposed fix:** Validate `Manifest.permission.RECORD_AUDIO` explicitly using Accompanist standard flow before activating dictation.
**Impact:** High
**Effort:** Medium
**Priority score:** 5

### [FUNC-5] Illegal Markdown Encoding Export
**File(s):** `src/domain/src/main/java/com/storybuilder/domain/usecase/ExportStoryUseCase.kt`
**Current behavior:** Characters containing inherent markdown syntax are smashed directly into string builder formats.
**Problem:** Quotes containing bold `**` characters natively alter downstream export presentation severely.
**Proposed fix:** Filter payload streams iteratively using standard escape slash replacement methods on inputs.
**Impact:** Low
**Effort:** Low
**Priority score:** 3

### [FUNC-6] Insecure Target Path Saving
**File(s):** `src/domain/src/main/java/com/storybuilder/domain/usecase/ExportStoryUseCase.kt`
**Current behavior:** Writing payload to `/Downloads` directly utilizing standard `java.io` methodology.
**Problem:** Android 11+ and Scoped Storage bans this outright resulting in abrupt file system security exceptions crashing the app payload.
**Proposed fix:** Implement standard `ActivityResultContracts.CreateDocument("text/markdown")` requesting URI destination permission.
**Impact:** High
**Effort:** Medium
**Priority score:** 5

### [FUNC-7] Unauthorized AI Handeling
**File(s):** `src/data/ai/src/main/java/com/storybuilder/data/ai/client/anthropic/AnthropicService.kt`
**Current behavior:** Fails and drops a 401 unauthenticated stacktrace upon invalid token.
**Problem:** The user assumes the infrastructure is malfunctioning instead of their specific token expiring.
**Proposed fix:** Intercept 401 errors, systematically wipe the matching configuration from `SecureApiKeyStorage`, and direct the UI back toward Setup natively.
**Impact:** Medium
**Effort:** Medium
**Priority score:** 3

### [FUNC-8] Temperature Drift Miscalculation
**File(s):** `src/data/ai/src/main/java/com/storybuilder/data/ai/PromptFactory.kt`
**Current behavior:** Default `GeminiRequest` bodies assume a static internal setting (typically `1.0`).
**Problem:** Highly strict genre styles (Mystery, Historical) break character easily if temperature isn't modulated alongside the tone logic.
**Proposed fix:** Tie numeric temperature explicitly to the chosen Genre data object logic parameters when generating raw API arrays.
**Impact:** Medium
**Effort:** Medium
**Priority score:** 3

### [FUNC-9] Memory Scaling Instability
**File(s):** `src/feature/chat-player/src/main/java/com/storybuilder/feature/chatplayer/ChatPlayerScreen.kt`
**Current behavior:** Fetches absolute total message count universally into memory mapping.
**Problem:** Enduring chat sagas hitting 400+ nodes will decimate memory thresholds eventually leading to OOM terminations out-of-bounds.
**Proposed fix:** Cap the active memory list via paging or limit `StoryBeatDao` selections with `.takeLast(50)`.
**Impact:** High
**Effort:** High
**Priority score:** 3

### [FUNC-10] Inexact Typing Conversions
**File(s):** `src/feature/genre-select/src/main/java/com/storybuilder/feature/genres/GenreSelectScreen.kt`
**Current behavior:** Binds continuous Float variables toward prompt systems.
**Problem:** A decimal input translating into `"Darkness tone 3.489115"` introduces hallucinatory token noise into strict AI matrices.
**Proposed fix:** Format explicitly through `.roundToInt()` masking internally logic flow.
**Impact:** Low
**Effort:** Low
**Priority score:** 3

---

### Settings & Configuration

### [SETTINGS-1] DataStore Multithread Clashing
**File(s):** `src/data/local/src/main/java/com/storybuilder/data/local/di/LocalDataModule.kt`
**Current behavior:** Initiating `PreferencesDataStore` without exact singleton enforcement mechanisms.
**Problem:** Causes random fast-switch exceptions (`There are multiple DataStores active for the same file`) under multithread stress instances.
**Proposed fix:** Anchor instantiation via Context delegate `val Context.dataStore` rigidly within the root file before singleton provision.
**Impact:** High
**Effort:** Low
**Priority score:** 7

### [SETTINGS-2] Desynced Visual Defaults
**File(s):** `src/feature/user-dashboard/src/main/java/com/storybuilder/feature/userdashboard/SettingsViewModel.kt`
**Current behavior:** UI state executes locally upon updates while committing DataStore updates async.
**Problem:** Backing out during swift mutations might reflect one setting but cache an alternative iteration underneath if cancelled prematurely.
**Proposed fix:** Refactor settings presentation strictly mapping `preferencesFlow.collectAsStateWithLifecycle()`.
**Impact:** Medium
**Effort:** Low
**Priority score:** 5

### [SETTINGS-3] API Model Cross-Contamination
**File(s):** `src/app/src/main/java/com/storybuilder/app/screens/apiprovider/ApiProviderSetupViewModel.kt`
**Current behavior:** Keeps `selectedModel` variables static when `activeProvider` transitions laterally.
**Problem:** Shifting Anthropic to Google pushes Anthropic's model names into Gemini's payload queries triggering structural API invalidations.
**Proposed fix:** Append systematic model string resets into the state shift commands.
**Impact:** High
**Effort:** Low
**Priority score:** 7

### [SETTINGS-4] Inaudible TTS Configurations
**File(s):** `src/feature/user-dashboard/src/main/java/com/storybuilder/feature/userdashboard/SettingsScreen.kt`
**Current behavior:** Provides drop-downs dictating pitch & pace controls in silence.
**Problem:** Uninformed adjustments force the player to exit settings fully to verify the audio characteristics of the chosen persona setup.
**Proposed fix:** Present an embedded Play icon that requests `TTSManager` to execute `"Here is a sample of this voice profile"` text dynamically.
**Impact:** High
**Effort:** Medium
**Priority score:** 5

### [SETTINGS-5] Ambiguous Story Overrides
**File(s):** `src/domain/src/main/java/com/storybuilder/domain/model/Story.kt`
**Current behavior:** General default modes override story modes vaguely without documented structural prioritization hierarchy.
**Problem:** Conflict occurs where users changing defaults midway unexpectedly disrupts the specific intent of previously initiated chapters.
**Proposed fix:** Lock specific story modes upon genesis inside `StoryEntity` strictly insulating historical works definitively.
**Impact:** Medium
**Effort:** Medium
**Priority score:** 3

### [SETTINGS-6] Null Configuration Ejection
**File(s):** `src/feature/user-dashboard/src/main/java/com/storybuilder/feature/userdashboard/SettingsScreen.kt`
**Current behavior:** Lack of factory reset protocols systemically.
**Problem:** Disoriented testing leaves broken preferences without simple remediation techniques isolating manual registry erasures.
**Proposed fix:** Draft a "Reset Settings" button initiating a `.clear()` parameter toward preferences DataStore mappings.
**Impact:** Low
**Effort:** Low
**Priority score:** 3

### [SETTINGS-7] Unresponsive Theme Toggling
**File(s):** `src/app/src/main/java/com/storybuilder/app/ui/theme/Theme.kt`
**Current behavior:** Theme adheres purely to `isSystemInDarkTheme()`.
**Problem:** Clashes intensely against internal color ambient requirements outlined visually throughout settings specifications dictating overrides.
**Proposed fix:** Introduce a 3-way toggle (Light, Dark, System) mapping through UserPreferences into the primary structural Compose parameters.
**Impact:** Medium
**Effort:** Low
**Priority score:** 5

### [SETTINGS-8] Onerous Input Scaling
**File(s):** `src/app/src/main/java/com/storybuilder/app/ui/theme/Type.kt`
**Current behavior:** Font typography structures execute rigidly defined dimensions universally.
**Problem:** Hard-of-sight users explicitly require custom text scaling modifications beyond internal structural density bounds to safely dictate content matrices.
**Proposed fix:** Construct an isolated "Story Font Scale" modifier scaling the ambient dimension factors independently universally across bubbles.
**Impact:** Medium
**Effort:** Medium
**Priority score:** 3

### [SETTINGS-9] Opaque API Connection Validation
**File(s):** `src/app/src/main/java/com/storybuilder/app/screens/apiprovider/ApiProviderSetupViewModel.kt`
**Current behavior:** Binds API strings immediately to secure storage persistently.
**Problem:** Missing characters or inactive tokens remain untracked until arbitrary usage triggers collapse sequences dynamically.
**Proposed fix:** Block the transition operation explicitly until basic `models.list()` authentication test queries pass securely.
**Impact:** High
**Effort:** Medium
**Priority score:** 5

### [SETTINGS-10] Synchronous IO Blockage in Getters
**File(s):** `src/data/local/src/main/java/com/storybuilder/data/local/preferences/SettingsDataStore.kt`
**Current behavior:** `.first()` calls initiate dynamically whenever logic asks for user tokens inside tight operation loops.
**Problem:** Synchronous flow conversion during generation ticks blocks UI renders inducing micro-stutters.
**Proposed fix:** Supply a `StateFlow` caching values into memory immediately during app-launch providing asynchronous memory reading.
**Impact:** High
**Effort:** Medium
**Priority score:** 5
