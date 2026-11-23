# Changelog

Semua perubahan penting pada MoneyLife Android dicatat di sini. Format mengikuti
[Keep a Changelog](https://keepachangelog.com/id/1.1.0/) dan versi mengikuti
[Semantic Versioning](https://semver.org/lang/id/).

> Status: **release candidate `1.0.0`** disiapkan 21 November 2026. Tanggal final
> mengikuti tag setelah R7/R8/R10 selesai. Lihat
> `docs/RELEASE_CHECKLIST_v1.0.0.md`.

## [1.0.0] - 2026-11-21 (release candidate)

### Added

- Aplikasi pencatatan pengeluaran harian untuk kategori Makan, Transport, dan
  Jajan.
- Pilihan batas harian **Rp 50.000** atau **Rp 100.000**.
- Penyimpanan snapshot batas harian per tanggal; tanggal lampau terkunci pada
  batas yang berlaku saat hari itu berjalan.
- Ringkasan pengeluaran, progress batas harian, dan riwayat per tanggal.
- Rollover sisa batas harian menjadi saldo tabungan bulanan.
- Penyimpanan lokal dengan Room dan dukungan migrasi database.
- Privacy policy publik dan deklarasi Data Safety.
- Build variant `staging` dengan application ID
  `com.kelvinht.moneylife.staging`, label **MoneyLife Staging**, dan konfigurasi
  R8 yang mengikuti release.
- Verifikasi `bundleStaging` di CI.

### Changed

- Perhitungan tabungan dan rollover memakai batas harian tanggal masing-masing,
  termasuk untuk hari yang terlewat.
- README disinkronkan dengan fitur batas harian dan struktur project terbaru.
- Dokumen privasi dan Data Safety tidak lagi terikat pada satu nomor versi
  aplikasi.

### Removed

- Field **Tanggal Transaksi** pada halaman Catat Pengeluaran. Tanggal transaksi
  kini mengikuti hari yang dipilih di Home saat tombol Catat Pengeluaran
  ditekan dan tidak dapat diubah lagi dari form.

### Fixed

- Daftar pengeluaran dapat discroll saat isinya melebihi tinggi layar.
- Jarak tombol **Catat Pengeluaran** dari daftar pengeluaran diperbaiki.
- Resource view yang tidak terpakai dihapus.
- Tanggal tersimpan yang jatuh di masa depan (misalnya setelah jam perangkat
  yang sempat dimajukan dikoreksi mundur) tidak lagi membuat form atau
  navigasi tanggal terbuka pada hari yang salah; pilihan tanggal dan rollover
  tabungan otomatis kembali ke hari ini.
- Perubahan tanggal, jam, atau zona waktu perangkat langsung menyegarkan
  tanggal di layar Home tanpa perlu menutup aplikasi.

### Release gates

- CI menjalankan unit test, ktlint, lint debug/release, instrumentation test API
  27/API 36, `bundleRelease`, dan `bundleStaging`.
- Smoke test perangkat fisik (R7), Play Console (R8), aksesibilitas (R10), dan
  tag final (R11) masih terbuka.
