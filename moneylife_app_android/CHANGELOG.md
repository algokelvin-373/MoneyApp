# Changelog

Semua perubahan penting pada MoneyLife Android dicatat di sini. Format mengikuti
[Keep a Changelog](https://keepachangelog.com/id/1.1.0/) dan versi mengikuti
[Semantic Versioning](https://semver.org/lang/id/).

## [Unreleased]

### Added

- Pilihan batas harian **Rp 50.000** atau **Rp 100.000**.
- Penyimpanan snapshot batas harian per tanggal; tanggal lampau terkunci pada
  batas yang berlaku saat hari itu berjalan.
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

### Fixed

- Daftar pengeluaran dapat discroll saat isinya melebihi tinggi layar.
- Jarak tombol **Catat Pengeluaran** dari daftar pengeluaran diperbaiki.
- Resource view yang tidak terpakai dihapus.

## [1.0.0] - 2026-11-16

### Added

- Aplikasi pencatatan pengeluaran harian untuk kategori Makan, Transport, dan
  Jajan.
- Ringkasan pengeluaran, progress batas harian, dan riwayat per tanggal.
- Rollover sisa batas harian menjadi saldo tabungan bulanan.
- Penyimpanan lokal dengan Room dan dukungan migrasi database.
- Privacy policy publik dan deklarasi Data Safety.
- CI untuk unit test, ktlint, lint, instrumentation test API 27/API 36, dan
  release bundle dengan R8.
