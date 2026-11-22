# R7 — Physical Smoke Test v1.0.0

Tanggal audit: 23 November 2026  
Branch: `r7-physical-smoke`  
Perangkat: TECNO KJ5n (`11163693C4003530`)  
OS: Android 13 / API 33  
Build: signed release APK, `com.kelvinht.moneylife`, `versionName 1.0.0`, `versionCode 10000`, R8 + resource shrinking

## Hasil

| Skenario | Hasil | Bukti perilaku |
| --- | --- | --- |
| Clean install dan cold start | ✅ Lulus | Uninstall → install `app-release.apk` → `MainActivity` cold-start; total awal Rp0. |
| Tambah transaksi | ✅ Lulus | FAB membuka form; transaksi tersimpan dan agregat Home berubah. |
| Tepat batas Rp100.000 | ✅ Lulus | Total Rp100.000, 100% terpakai, sisa Rp0. |
| Lebih dari batas | ✅ Lulus | Total Rp110.000, pesan “Lebih Rp10.000 dari batas harian”, kondisi “Melebihi Limit”, tabungan Rp0. |
| Hapus transaksi | ✅ Lulus | Dialog konfirmasi tampil; setelah Rp10.000 dihapus total kembali Rp100.000. |
| Transaksi tanggal lampau | ✅ Lulus | Transaksi Rp50.000 tersimpan pada 30 Nov 2026 tanpa mengubah data 1 Des; limit hari lampau terkunci. |
| Restart / process recreation | ✅ Lulus | Setelah force-stop dan cold restart, data Rp100.000 tetap ada. |
| Rotasi | ✅ Lulus | Activity recreation landscape mempertahankan total dan state; rotasi perangkat dikembalikan ke otomatis. |
| Rollover lintas hari/bulan | ✅ Lulus | Waktu uji dikontrol 30 Nov→1 Des saat auto-time nonaktif. Pengeluaran 30 Nov Rp50.000 menghasilkan saldo tabungan Nov Rp50.000 pada cold start 1 Des. |
| Migrasi Room v1→v2 | ✅ Lulus | `connectedDebugAndroidTest`: 2 migration test lulus pada perangkat fisik. |
| Instrumentation flow | ✅ Lulus | `connectedDebugAndroidTest`: 3 add-expense flow test lulus pada perangkat fisik. |
| Uninstall/reinstall final | ✅ Lulus | APK release dapat dipasang kembali, cold-start berhasil, database bersih, versi tetap 1.0.0. |

## Instrumentation

Perintah:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest --rerun-tasks
```

Hasil: `BUILD SUCCESSFUL`, **5/5 test**, 0 skipped, 0 failed, 71 task dieksekusi.

Report lokal:

- `app/build/reports/androidTests/connected/debug/index.html`
- `app/build/outputs/androidTest-results/connected/debug/TEST-TECNO KJ5n - 13-_app-.xml`

## Catatan

- Perubahan waktu hanya dipakai untuk memicu rollover nyata karena waktu otomatis perangkat sudah nonaktif. Zona waktu tetap `Asia/Jakarta`, lalu jam dikembalikan ke 1 Des 2026 sesuai baseline perangkat.
- Instalasi ADB sebelumnya memerlukan verifikasi aplikasi via USB/Play Protect diizinkan oleh pemilik perangkat.
- Tidak ditemukan crash, kehilangan data yang tidak diharapkan, atau bug aplikasi selama R7. Tidak ada perubahan source code aplikasi.
