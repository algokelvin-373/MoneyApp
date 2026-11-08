# Inspect Code Report - MoneyLife (moneylife_app_android)

Date: 2026-10-24  
Branch: dev/improvements (HEAD dbc7e8c, merged fix/gelombang-6)  
Scope: moneylife_app_android/ only

## Quick Status

- Git: clean working tree after ktlint reformat (only formatting changes vs merged state; build tools ran)
- Build/Verify (from last runs): testDebugUnitTest BUILD SUCCESSFUL (54 tests), assembleDebug/assembleRelease BUILD SUCCESSFUL, lintDebug BUILD SUCCESSFUL (warnings only), ktlintCheck BUILD SUCCESSFUL, assembleDebugAndroidTest BUILD SUCCESSFUL (instrumentation not executed - no device/emulator)
- JDK 17, AGP 8.6.1, Kotlin 1.9.0, compileSdk 35, minSdk 27, Room 2.6.1, KSP

## Architecture

- Clean Architecture: presentation -> domain <- data
- DI: manual AppContainer (no DI framework)
- DB: Room (v2, schema exported to app/schemas)
- Async: Coroutines + StateFlow (no LiveData)
- Transactions: domain uses TransactionRunner (Room.withTransaction) for atomic expense+savings writes

## Module Layout (key files)

Data/Local:

- data/local/MoneyLifeDatabase.kt (v2, MIGRATION_1_2 adds daily_savings/app_state)
- data/local/dao/ExpenseDao.kt, SavingsDao.kt
- data/local/entity/\* (ExpenseEntity, DailySavingEntity, AppStateEntity)
- data/mapper/ExpenseMapper.kt
- data/repository/{ExpenseRepositoryImpl,SavingsRepositoryImpl,RoomTransactionRunner}.kt

Domain:

- domain/model/{Expense,ExpenseCategory,DailyBudget,CategoryBudget}.kt
- domain/repository/{ExpenseRepository,SavingsRepository,TransactionRunner}.kt
- domain/usecase/{GetDailyExpenses,CalculateDailyBudget,AddExpense,DeleteExpense,ObserveSavingsBalance,RolloverDailySavings}.kt

Presentation:

- presentation/home/{MainActivity,HomeViewModel,HomeViewModelFactory,HomeUiState,ExpenseAdapter,ExpenseUiModel,BudgetStatus}.kt
- presentation/addedit/{AddExpenseActivity,AddExpenseViewModel,AddExpenseViewModelFactory,AddExpenseUiState}.kt
- presentation/common/CategoryStyle.kt
- MoneyLifeApplication.kt, di/AppContainer.kt

Resources:

- res/layout/{activity_main,activity_add_expense,item_expense}.xml (data binding not used, ViewBinding)
- res/values/{strings,colors,dimens,styles}.xml
- res/drawable/_, res/mipmap/_

Tests:

- Unit: 13 test files (+3 fakes) - domain/usecase, ViewModel, formatter, state
- Instrumentation: 2 files (AddExpenseFlowTest, MoneyLifeDatabaseMigrationTest) - compiled, not executed

## Code Quality Assessment

### Strengths

- Atomicity: AddExpense/DeleteExpense use TransactionRunner; Rollover settles missed days in one transaction
- Correct rollover: settles lastActiveDate..today-1, sets lastActiveDate=today, treats no-spend as full save, over-limit saves nothing
- StateFlow reactivity with flatMapLatest/combine/stateIn; HomeUiState holds raw data, formatting in UI
- SavedStateHandle for AddExpense form state (restores after process death)
- refresh() serialized with Mutex (prevents race on lastTodayKey)
- Strings moved to resources, 0 hardcoded android:text literals; error as @StringRes
- Release: minifyEnabled true, shrinkResources true, Room keep rules in proguard-rules.pro
- CI: .github/workflows/android.yml (testDebugUnitTest, ktlintCheck, lintDebug)
- Lint abortOnError true, ktlint configured and passing
- Room schema exported (v1,v2) and migration tested
- DateUtils centralizes parsing/formatting; uses LocalDate (type-safe)
- allowBackup false

### Issues Found (Lint/Static)

**Errors/Blockers:** None (lint abortOnError true, all checks pass). ktlint failures were from unformatted changes and fixed by ./gradlew ktlintFormat.

**Warnings (from lint-results-debug.xml):**

- Dependency updates available: AGP 8.6.1 -> 9.4.1; many AndroidX (core-ktx, lifecycle, room 2.6.1->2.8.5, activity, appcompat, material, recyclerview, constraintlayout, test libs). (Informational)
- OldTargetApi: targetSdk 35 is fine but lint notes compatibility; consider keeping current.
- DataExtractionRules: allowBackup false is set but Android 12+ recommends dataExtractionRules; acceptable with allowBackup false.
- ObsoleteSdkInt: mipmap-anydpi-v26 not needed since minSdk 27 (can merge to mipmap-anydpi).
- DisableBaselineAlignment: LinearLayout in activity_main.xml:255 should set baselineAligned=false for perf.
- NestedWeights: two occurrences in activity_main.xml (perf).
- Overdraw: root LinearLayouts paint background with theme background (activity_main/add_expense) - minor.
- UnusedResources: R.drawable.ic_add (appears unused), R.color.softRed (unused).
- MonochromeLauncherIcon: missing monochrome tag in adaptive icons.
- Autofill: EditText inputs missing autofillHints (amount/note).

**Code Style:** ktlint now passes after auto-format. Some trailing commas/formatting applied across data/presentation/domain files.

### Specific Observations

- AddExpenseViewModel uses SavedStateHandle with default SavedStateHandle() for tests; factory provides handle via CreationExtras (good)
- HomeViewModel.refresh() uses Mutex.withLock; lastTodayKey updated atomically in critical section
- RolloverDailySavingsUseCase: when lastActiveDate null sets to todayKey (initial state) - correct to avoid retroactive settlement
- ExpenseAdapter uses ListAdapter with DiffUtil
- CategoryBudget in domain, usage calculated
- Date format changed to abbreviated month (MMM) per spec
- textSavings renamed from textAverage

## Recommendations (non-blocking)

1. Add autofillHints to amount/note inputs
2. Remove unused resources (ic_add, softRed) or use them
3. Add monochrome icons for adaptive launcher
4. Consider dataExtractionRules for Android 12+ even with allowBackup false if needed
5. Minor layout perf: baselineAligned=false, avoid nested weights
6. Dependency updates: evaluate Room 2.8.x, lifecycle 2.11.x (may need testing)
7. Merge mipmap-anydpi-v26 into mipmap-anydpi since minSdk 27

## Files Modified Since Merge (ktlint reformat)

11 files reformatted for style (MoneyLifeDatabase, mappers/repos, AppContainer, usecases, AddExpenseActivity, ExpenseAdapter, MainActivity). No functional changes.
