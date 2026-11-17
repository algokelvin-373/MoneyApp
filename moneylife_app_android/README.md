# MoneyLife (Android)

Aplikasi Android Native (Kotlin, XML Views) untuk mencatat pengeluaran harian
**Makan**, **Transport**, dan **Jajan** dengan batas pengeluaran **Rp 100.000
per hari**.

Tidak menggunakan Jetpack Compose. Arsitektur: **Clean Architecture + MVVM +
Repository Pattern** dengan **Room** sebagai database lokal.

## Fitur

- Catat pengeluaran pada hari ini atau tanggal sebelumnya (kategori: Makan,
  Transport, Jajan). Ketuk tanggal transaksi atau tombol **Ubah** saat mengisi.
- Pilih tanggal pada beranda, atau gunakan panah hari sebelumnya/berikutnya,
  untuk melihat ringkasan dan riwayat pengeluaran pada tanggal tersebut.
- Progress bar batas harian yang berubah warna: hijau (aman), oranye
  (mendekati batas, >= 80%), merah (melebihi batas).
- Peringatan nominal jika pengeluaran melebihi batas Rp 100.000/hari.
- Riwayat pengeluaran menurut tanggal (terbaru di atas) + hapus dengan konfirmasi.
- Sisa batas harian otomatis menjadi tabungan saat berganti hari; jika
  pengeluaran melebihi batas Rp 100.000, tabungan hari itu bernilai 0.
- Saldo tabungan per bulan ditampilkan di beranda dan tersimpan permanen
  secara lokal dengan Room. Saldo otomatis kembali ke Rp 0 saat memasuki
  bulan baru.
- Jika transaksi tanggal lampau ditambah atau dihapus, tabungan tanggal itu
  dihitung ulang agar saldo tetap sesuai.

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

## Logika Tabungan (Rollover)

Setiap kali aplikasi dibuka (`HomeViewModel.refresh`), `RolloverDailySavingsUseCase`
membandingkan tanggal aktif terakhir dengan tanggal hari ini:

- Jika tanggal belum pernah tercatat, hanya mencatat tanggal aktif hari ini.
- Jika sudah berganti hari, sisa batas hari sebelumnya dihitung sebagai
  `limit - total pengeluaran` lalu disimpan ke tabel `daily_savings` sebagai
  tabungan harian.
- Jika pengeluaran hari itu **lebih dari Rp 100.000 (batas)**, tabungan hari itu
  bernilai **0** (`coerceAtLeast(0L)`).
- Saldo tabungan di beranda adalah penjumlahan tabungan harian pada bulan
  yang sedang dilihat (`SavingsDao.observeBalance`), sehingga otomatis
  bernilai 0 saat memasuki bulan baru. Tabungan bulan sebelumnya tetap
  tersimpan dan bisa dilihat dengan membuka tanggal pada bulan tersebut.
- Perubahan pada pengeluaran tanggal lampau langsung menghitung ulang tabungan
  tanggal tersebut.

## Build

```bash
cd moneylife_app_android
./gradlew.bat :app:assembleDebug
```

APK debug: `app/build/outputs/apk/debug/app-debug.apk`.

### Signed release

Release signing memakai file properti eksternal agar keystore dan password tidak
masuk Git. Salin `signing.properties.example` ke lokasi aman, isi nilai sebenarnya,
lalu set environment variable `MONEYLIFE_SIGNING_PROPERTIES` ke path file tersebut.

```powershell
$env:MONEYLIFE_SIGNING_PROPERTIES = "D:\secure\moneylife-signing.properties"
.\gradlew.bat :app:bundleRelease
```

Signed AAB tersedia di `app/build/outputs/bundle/release/app-release.aab`. Simpan
upload key dan file propertinya di password manager/backup terenkripsi; kehilangan
upload key akan menghambat proses update aplikasi.

Jalankan unit test domain:

```bash
./gradlew.bat :app:testDebugUnitTest
```

## CI

Workflow `.github/workflows/android.yml` berjalan pada push/PR yang menyentuh
`moneylife_app_android/**` dan bisa dijalankan manual:

- **verify** — unit test, ktlint, `lintDebug`, `lintRelease`,
  `assembleDebugAndroidTest`, dan `bundleRelease` (R8 + `shrinkResources`).
  AAB dan checksum SHA-256 diunggah sebagai artifact; laporan lint/test disimpan.
- **instrumentation** — emulator API 27 dan API 36 menjalankan
  `connectedDebugAndroidTest` (5 test). Job ini adalah release gate, bukan
  opsional.

Release signing di CI bersifat opsional dan hanya aktif jika repository secrets
berikut diisi. Tidak ada keystore atau password di dalam repo:

| Secret                     | Isi                                      |
| -------------------------- | ---------------------------------------- |
| `MONEYLIFE_KEYSTORE_BASE64`| isi file `.jks` yang di-encode base64    |
| `MONEYLIFE_STORE_PASSWORD` | password keystore                        |
| `MONEYLIFE_KEY_ALIAS`      | alias key                                |
| `MONEYLIFE_KEY_PASSWORD`   | password key                             |

Tanpa secrets, CI tetap membangun AAB unsigned untuk memverifikasi R8. Dengan
secrets, CI juga menjalankan `jarsigner -verify` pada AAB hasil build.

## Privasi

MoneyLife tidak memiliki akun, iklan, analytics, maupun izin internet. Seluruh
data disimpan lokal di perangkat; `android:allowBackup="false"` mematikan backup
otomatis.

- Kebijakan publik: <https://algokelvin-373.github.io/MoneyApp/>
- Sumber halaman: `docs/privacy-policy/index.html` (dideploy oleh
  `.github/workflows/pages.yml`)
- Dokumen internal: `PRIVACY_POLICY.md` dan `docs/DATA_SAFETY_V1.0.0.md`

Halaman publik hanya memuat privacy policy; dokumen internal dan source code
tidak ikut dipublikasikan.

## Catatan

- `minSdk 27`, `targetSdk 36`, Kotlin 1.9, Room 2.6.1, Material 3.
- Sumber daya `local.properties` menunjuk ke Android SDK lokal dan tidak
  disertakan ke version control.
