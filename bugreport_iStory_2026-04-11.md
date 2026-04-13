# Bug Report — iStory
Date: 2026-04-11

## Phase 0 — Known Critical Bugs
*None listed in context.*

## Phase 1 — New Bugs Discovered

### [BUG-001] ProGuard Obfuscation Crash
**Severity:** Critical  
**Screen / Module:** All AI-dependent flows  
**File:** `src/app/proguard-rules.pro` (line 36-37)  
**Root cause:** The ProGuard rules keep models in `com.storybuilder.domain.model` and entities in `com.storybuilder.data.local.entity`, but completely omit `com.storybuilder.data.ai.model`. When the app is built for release, the AI response models (like `StoryBeatResponse`) are obfuscated, causing Gson to fail parsing JSON responses, leading to a total app failure on story generation.  
**Reproduction:** Build app with `minifyEnabled true`, start a story, wait for AI response.  
**Fix:** Add `-keep class com.storybuilder.data.ai.model.** { *; }` to `proguard-rules.pro`.  
**Test:** Verify parsing works in a release-signed APK.

### [BUG-002] Unencrypted Fallback for API Keys
**Severity:** High  
**Screen / Module:** API Setup / Security  
**File:** `SecureApiKeyStorage.kt` (line 56)  
**Root cause:** If `EncryptedSharedPreferences` fails to initialize (e.g., due to Keystore corruption), the code falls back to a plain-text `SharedPreferences` in `Context.MODE_PRIVATE`. This silently compromises user security by storing high-value API keys unencrypted on the filesystem.  
**Reproduction:** Simulate Keystore corruption on a rooted device or by clearing Keystore data.  
**Fix:** Instead of falling back to non-encrypted prefs, the app should show a critical error and prompt the user to reset their keys or reinstall.  
**Test:** Check `shared_prefs` directory after a failed encrypted initialization.

### [BUG-003] Hardcoded Story Metadata
**Severity:** High  
**Screen / Module:** Chat Player  
**File:** `ChatPlayerViewModel.kt` (lines 51-67)  
**Root cause:** The `ChatPlayerViewModel` hardcodes `storyId`, `characterName`, `genreName`, and the `Story` object itself. This makes the "Multi-Story Library" feature non-functional as every story will use the same Detective Sarah Blackwood context regardless of user selection.  
**Reproduction:** Select any story from the library; it will always show the "Blackwood Manor" context.  
**Fix:** Fetch story, character, and genre details from the repository using the `storyId` passed from the navigation arguments.  
**Test:** Generate two stories with different characters and verify they remain distinct.

### [BUG-004] Strict Parser Failure
**Severity:** Medium  
**Screen / Module:** AI Generation  
**File:** `ResponseParser.kt` (line 38)  
**Root cause:** The parser throws an exception if the AI returns anything other than exactly 3 options. LLMs (especially smaller ones or under high temperature) frequently fail to count correctly. This causes a hard failure for the user instead of displaying the story with available options.  
**Reproduction:** Force the AI to return 2 or 4 options.  
**Fix:** Implement a "graceful degradation" where it accepts 1-4 options or falls back to Free Text mode if options are mangled.  
**Test:** Mock a response with 2 options and verify the app survives.

### [BUG-005] Race Condition in AI Provider Setup
**Severity:** Low  
**Screen / Module:** Data Layer  
**File:** `StoryAIRepositoryImpl.kt` (line 42)  
**Root cause:** The active provider is loaded in a floating `CoroutineScope(Dispatchers.IO).launch` in the `init` block. If `generateOpeningBeat` is called immediately after repository creation, `_activeProvider` might still be the default (GOOGLE) even if the user selected something else.  
**Reproduction:** App start -> Immediate story start on slow storage.  
**Fix:** Use a properly initialized `SharedFlow` or a blocking initial load if necessary for critical setup components.  
**Test:** Trace provider usage immediately after app boot.
