# Catatan Perbaikan — `dev/moneylife`

> Branch: `dev/moneylife` @ `297b6e6` · Audit: 22 Okt 2026
> Baseline verifikasi: `moneylife_app_android` → `:app:testDebugUnitTest` **BUILD SUCCESSFUL** · `money_app_android` → `:app:compileDebugKotlin` **BUILD FAILED (52 error)**

---

## P0 — Blocker

### 1. `money_app_android` tidak bisa dikompilasi

Redesign beranda + form entri menulis ulang seluruh Kotlin-nya, tapi file layout-nya tidak pernah di-commit. ViewBinding jadi tidak menghasilkan properti yang dipanggil.

- **Bukti**: `git diff --name-only develop dev/moneylife -- money_app_android/app/src/main/res/layout` → kosong.
- **Dampak**: 52 error — 37 `Unresolved reference` + 15 `Variable expected` di 2 file
  - `feature/home/presentation/HomeFragment.kt` — 33 error
  - `feature/transactionentry/presentation/TransactionEntryFragment.kt` — 19 error
- **31 ID layout hilang**: `textDate`, `textSpent`, `textUsage`, `textLimit`, `textRemaining`, `textAverage`, `textCondition`, `progressDaily`, `textCategoryOne/Two/Three`, `textCategoryAmountOne/Two/Three`, `progressCategoryOne/Two/Three`, `emptyState`, `buttonAddExpense`, `navStatistics`, `navBudget`, `buttonReset`, `quick10/20/50/100`, `categoryFood/Transport/Snack`, `checkFood/Transport/Snack`, `textSource`, `textNote`.
- **Fix**: port `activity_main.xml` + `activity_add_expense.xml` (yang sudah jadi di `moneylife_app_android`) ke `fragment_home.xml` + `fragment_transaction_entry.xml`, **atau** revert redesign `money_app_android` bila app baru sudah menggantikan perannya. Prioritas: putuskan dulu apakah `money_app_android` masih hidup.

---

## P1 — Logika inti

### 2. Rollover tabungan kehilangan hari yang terlewat
`RolloverDailySavingsUseCase` hanya menulis satu baris `daily_savings` — untuk `lastActiveDate` saja. Kalau app tidak dibuka 7 hari, 6 hari sisanya tidak pernah masuk tabungan (padahal tiap hari tanpa pengeluaran seharusnya +Rp 100.000).

- **Lokasi**: `domain/usecase/RolloverDailySavingsUseCase.kt`
- **Fix**: loop dari `lastActiveDate + 1 hari` sampai `today`, hitung `limit - spent` per hari, tulis semua. Tetapkan aturan eksplisit untuk hari tanpa data (tabung penuh atau nihil).

### 3. Form "Catat Pengeluaran" terbuka dengan data dummy
`AddExpenseUiState` punya default `amountInput = "45000"` dan `note = "Makan siang Rendang & Es Teh"`. Install baru → form langsung terisi transaksi fiktif. Sisa mockup yang belum dibersihkan.

- **Lokasi**: `presentation/addedit/AddExpenseUiState.kt`
- **Fix**: defaultkan `amountInput = ""`, `note = ""`.

### 4. Simpan expense + update tabungan tidak atomik
`AddExpenseUseCase` dan `DeleteExpenseUseCase` memanggil dua DAO berbeda secara berurutan. Crash di antara keduanya → expense tersimpan tapi tabungan stale (saldo tidak sesuai).

- **Lokasi**: `domain/usecase/AddExpenseUseCase.kt`, `domain/usecase/DeleteExpenseUseCase.kt`
- **Fix**: bungkus dalam satu `RoomDatabase.withTransaction { }`, atau buat DAO `@Transaction` method yang menulis expense + `daily_savings` secara atomik.

### 5. Batas per kategori hanya string tampilan
Pagu kategori (`50k` / `30k` / `20k`) di-hardcode sebagai literal di `HomeViewModel.categoryTotal(...)`, sedangkan batas harian yang sebenarnya ada di `DailyBudget.DEFAULT_DAILY_LIMIT = 100_000`. Belum ada konsep pagu per kategori di domain — progress bar kategori cuma menampilkan rasio terhadap angka yang tidak bisa diubah user dan tidak disimpan.

- **Lokasi**: `presentation/home/HomeViewModel.kt`, `domain/model/DailyBudget.kt`
- **Fix**: pindahkan pagu kategori ke `DailyBudget` / config terpusat (mis. `CategoryBudget`), hapus literal dari ViewModel.

### 6. Limit harian ter-hardcode di dua tempat
`strings.xml` punya `"dari Rp 100.000"` sementara limit sebenarnya konstanta di `DailyBudget`. Ubah satu, yang lain liar.

- **Fix**: satu sumber kebenaran; string resources pakai placeholder (`%1$s`).

---

## P2 — Kualitas kode

### 7. String Indonesia ter-hardcode di Kotlin
Bukan di `strings.xml` — padahal `strings.xml` sudah ada dan dipakai. Semua string ini masuk ke viewModel, jadi tidak lokalizable dan tidak bisa diuji.

| Lokasi | String |
|---|---|
| `presentation/home/HomeViewModel.kt` | `"Batas harian: "`, `"Sisa "`, `"% Terpakai"`, `"Sangat Aman"`, `"Perlu Dijaga"`, `"Melebihi Limit"`, `"Lebih ... dari batas harian"` |
| `presentation/addedit/AddExpenseViewModel.kt` | `"Masukkan nominal lebih dari 0"`, `"Gagal menyimpan pengeluaran"` |
| `domain/usecase/AddExpenseUseCase.kt` | `"Nominal harus lebih dari 0"` |
| `presentation/home/HomeUiState.kt` | `"Rp 0"`, `"Sangat Aman"`, `"Rp 0/50k"`, `"Rp 0/30k"`, `"Rp 0/20k"` |
| `res/layout/activity_add_expense.xml` | `contentDescription="Kembali"`, `"Atur ulang"` |

### 8. `HomeUiState` menyimpan string siap-tampil, bukan data
`spentText`, `conditionText`, `makanText`, `usageText` sudah diformat di ViewModel. State jadi sulit diuji (membandingkan string, bukan angka), dan formatting terpecah di dua layer.

- **Fix**: `HomeUiState` pegang nilai mentah (`spent: Long`, `condition: BudgetStatus`, `spentByCategory: Map<ExpenseCategory, Long>`); formatting dipindah ke layer presentation/resource.

### 9. Perbandingan tanggal pakai string
`lastActiveDate >= todayKey` (`RolloverDailySavingsUseCase`) dan `key <= DateUtils.todayKey()` (`HomeViewModel.selectDate`, `AddExpenseViewModel.selectDate`) membandingkan `String`. Kebetulan benar untuk format `yyyy-MM-dd`, tapi rapuh dan tidak eksplisit.

- **Fix**: bandingkan `LocalDate`, konversi di satu tempat.

### 10. `HomeViewModel.lastTodayKey` bisa race
`private var lastTodayKey` dibaca & ditulis di dalam `viewModelScope.launch` tanpa jaminan thread-safety; `refresh()` dipanggil dari `init`, `onResume`, dan callback activity result.

- **Fix**: jadikan bagian dari `StateFlow`, atau jalankan bagian kritis di `Mutex` / dispatcher single-shot.

### 11. `refresh()` dipanggil 2–3× per alur tambah pengeluaran
`init` + `MainActivity.onResume()` + `addExpenseLauncher` result. Tiap panggilan menjalankan pengecekan rollover ke DB.

- **Fix**: panggil sekali di `onResume` saja (hapus `init`).

### 12. Nama view tidak sesuai isinya
`MainActivity.render()`: `binding.textAverage.text = state.savingsText` — id layout bilang "average", isinya saldo tabungan. Sisa dari desain mockup.

- **Fix**: rename id layout → `textSavings` (perlu update layout + binding).

### 13. Format tanggal beda dengan mockup
`DateUtils.displayDate` pakai `"EEEE, d MMMM yyyy"` → "Jumat, 9 Oktober 2026", sedangkan mockup & README menulis "Jumat, 9 Okt 2026".

- **Fix**: pakai `MMM`, atau samakan mockup.

---

## P3 — Testing

### 14. Regresi cakupan test
`money_app_android` punya 5 file test ViewModel (`Home`, `TransactionEntry`, `History`, `Reports`, `Wallets`). App baru **nol** test ViewModel — padahal persis fitur yang paling rawan (navigasi tanggal, rollover, hitung ulang).

- **Fix**: tambah `HomeViewModelTest` (navigasi tanggal, ganti status budget), `AddExpenseViewModelTest` (validasi nominal, guard tanggal lampau), `RolloverDailySavingsUseCaseTest` (hari terlewat).

### 15. Navigasi tanggal belum diuji
`previousDay()` / `nextDay()` / `selectDate()` / `showToday()` + guard "tidak boleh pilih tanggal di masa depan" belum ada test-nya.

### 16. Tidak ada test instrumentasi di app baru
`moneylife_app_android` tidak punya folder `androidTest` sama sekali. Minimal: test navigation `MainActivity` → `AddExpenseActivity` (kirim `EXTRA_DATE_KEY`, balik dengan `RESULT_OK`).

### 17. Migrasi Room tidak diuji
`MoneyLifeDatabase` `version = 2` dengan `MIGRATION_1_2`, tapi tidak ada `MigrationTestHelper` test — padahal justru saat deploy user sudah punya DB v1.

### 18. `exportSchema = false`
Skema tidak pernah diekspor → riwayat skema hilang, tidak bisa divalidasi antar versi.

---

## P4 — Build & konfigurasi

### 19. Release tanpa R8
`minifyEnabled false` di kedua app, `proguard-rules.pro` masih template Android (10 baris). Tidak ada aturan keep untuk Room/serialization.

### 20. Dependency sia-sia di app baru
`androidx.fragment.ktx` dan `androidx.lifecycle.livedata.ktx` di `dependencies`, tapi **tidak ada** `import androidx.fragment.*` maupun `LiveData` di seluruh `app/src/main`. App ini pure Activity + StateFlow.

### 21. Tidak ada CI
Folder `.github/` tidak ada sama sekali. Tidak ada workflow yang menjalankan unit test — padahal test sudah ada dan hijau.

- **Fix**: GitHub Actions minimal `./gradlew :app:testDebugUnitTest` per app.

### 22. Tidak ada lint / ktlint / detekt
Tidak ada `lint { }` block, tidak ada `ktlint`/`detekt`. Style kotlin (`kotlin.code.style=official`) hanya anjuran, tidak ditegakkan.

### 23. JDK target tertinggal
`compileOptions` + `jvmTarget` masih `1.8`, padahal build berjalan di JDK 17 dan `compileSdk 35`.

### 24. `versionName "1.0"`
Harus `1.0.0` (semver) kalau akan masuk Play Store. `versionCode 1` juga belum ada skemanya.

### 25. `android.suppressUnsupportedCompileSdk=35`
Menekan warning AGP 8.5 alih-alih menaikkan versi AGP.

---

## P5 — Kebersihan repo

### 26. Aset PNG duplikat
`moneylife_app_android/ui/*.png` dan `ui/moneylife/*.png` isinya byte-identik (533.548 + 651.271 B), total ~2,4 MB terduplikasi. Salah satu cukup.

### 27. Folder sampah `%GRADLE_USER_HOME%`
`moneylife_app_android/%GRADLE_USER_HOME%/` — hasil ekspansi env var yang gagal. Kosong, tapi tetap tidak seharusnya ada di working tree.

### 28. README `money_app_android` kosong
Isinya cuma `# MoneyApp` / `MoneyApp` — tidak mendeskripsikan redesign beranda/entri yang sudah masuk.

### 29. README root belum menyebut modul baru
`README.md` repo belum mendokumentasikan `moneylife_app_android` sebagai salah satu Gradle project di root.

---

## P6 — Privasi & aksesibilitas

### 30. Backup data keuangan aktif
`android:allowBackup="true"` di kedua app. App baru **tidak** punya `dataExtractionRules`/`fullBackupContent` seperti `money_app_android`, jadi seluruh DB pengeluaran ikut ter-backup ke cloud tanpa filter.

- **Fix**: `allowBackup="false"`, atau tambahkan `dataExtractionRules` yang mengecualikan `moneylife.db`.

### 31. Permission storage mati di `money_app_android`
`READ_EXTERNAL_STORAGE` & `WRITE_EXTERNAL_STORAGE` dideklarasikan **dan** diminta runtime di `InputMoneyInFragment.kt:172`, padahal `targetSdk 35` membuat keduanya deprecated/no-op. Permission `CAMERA` sendiri memang dipakai (foto bukti) — yang itu jangan dihapus.

### 32. `contentDescription` masih hardcoded
`activity_add_expense.xml` memakai literal `"Kembali"` / `"Atur ulang"`; `activity_main.xml` sudah rapi pakai `@string/*`. Samakan.

---

## Urutan pengerjaan yang disarankan

| Gelombang | Isi | Estimasi |
|---|---|---|
| 1 | #1 (blocker compile) | 2–4 jam — atau revert, 15 menit |
| 2 | #2, #3, #4 (logika inti + data dummy) | 1–2 hari |
| 3 | #7, #8, #14, #15 (string + state + test ViewModel) | 2–3 hari |
| 4 | #21, #19, #16 (CI, R8, test instrumentasi) | 1–2 hari |
| 5 | sisanya (P2 detail, P4–P6 hygiene) | 2–3 hari |

> Catatan: #1 dan #7/#8 saling terkait — kalau `money_app_android` diputuskan **di-revert**, banyak temuan P1–P3 di app lama otomatis moot dan seluruh fokus pindah ke `moneylife_app_android`.
