# MoneyLife (Android)

Aplikasi Android Native (Kotlin, XML Views) untuk mencatat pengeluaran harian
**Makan**, **Transport**, dan **Jajan** dengan batas pengeluaran **Rp 100.000
per hari**.

Tidak menggunakan Jetpack Compose. Arsitektur: **Clean Architecture + MVVM +
Repository Pattern** dengan **Room** sebagai database lokal.

## Fitur

- Catat pengeluaran harian (kategori: Makan, Transport, Jajan).
- Ringkasan pengeluaran hari ini dan sisa batas harian.
- Progress bar batas harian yang berubah warna: hijau (aman), oranye
  (mendekati batas, >= 80%), merah (melebihi batas).
- Peringatan nominal jika pengeluaran melebihi batas Rp 100.000/hari.
- Riwayat pengeluaran hari ini (terbaru di atas) + hapus dengan konfirmasi.
- Data tersimpan permanen secara lokal dengan Room.

## Struktur Proyek (Clean Architecture)

```
app/src/main/java/com/kelvinht/moneylife/
├── MoneyLifeApplication.kt        # Entry point, membuat AppContainer
├── di/
│   └── AppContainer.kt            # Manual dependency injection
├── core/
│   ├── format/RupiahFormatter.kt  # Format mata uang
│   └── util/DateUtils.kt          # Helper tanggal (date key harian)
├── domain/                        # Lapisan domain (tanpa Android)
│   ├── model/                     # Expense, ExpenseCategory, DailyBudget
│   ├── repository/                # ExpenseRepository (kontrak)
│   └── usecase/                   # AddExpense, GetDailyExpenses,
│                                  # CalculateDailyBudget, DeleteExpense
├── data/                          # Lapisan data (implementasi)
│   ├── local/entity/              # ExpenseEntity (Room)
│   ├── local/dao/                 # ExpenseDao
│   ├── local/MoneyLifeDatabase.kt # RoomDatabase
│   ├── mapper/ExpenseMapper.kt    # Entity <-> Domain
│   └── repository/                # ExpenseRepositoryImpl
└── presentation/                  # Lapisan presentasi (MVVM)
    ├── common/CategoryStyle.kt    # Label/emoji/warna kategori
    ├── home/                      # MainActivity, HomeViewModel, adapter
    └── addedit/                   # AddExpenseActivity, AddExpenseViewModel
```

Aliran dependensi: `presentation -> domain <- data`. Lapisan domain tidak
bergantung pada Android/Room, sehingga mudah diuji.

## Logika Batas Harian

Semua perhitungan ada di domain model `DailyBudget`:

- `spent`: total pengeluaran hari itu.
- `remaining`: sisa batas (minimal 0).
- `overSpent`: kelebihan di atas batas.
- `isOverLimit`: `spent > limit`.
- `usagePercent`: 0..100 untuk progress bar.

Batas default: `DailyBudget.DEFAULT_DAILY_LIMIT = 100_000L`.

## Build

```bash
cd moneylife_app_android
./gradlew.bat :app:assembleDebug
```

APK debug: `app/build/outputs/apk/debug/app-debug.apk`.

Jalankan unit test domain:

```bash
./gradlew.bat :app:testDebugUnitTest
```

## Catatan

- `minSdk 27`, `targetSdk 35`, Kotlin 1.9, Room 2.6.1, Material 3.
- Sumber daya `local.properties` menunjuk ke Android SDK lokal dan tidak
  disertakan ke version control.
