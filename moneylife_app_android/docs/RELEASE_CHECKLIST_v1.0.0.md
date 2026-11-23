# Release Checklist — MoneyLife v1.0.0 (RC)

Status: **release candidate disiapkan, belum di-tag, belum disubmit ke Play Console.**

Tanggal persiapan: 21 November 2026 · Branch: `staging`

## Metadata build

| Item | Nilai |
| --- | --- |
| `applicationId` release | `com.kelvinht.moneylife` |
| `applicationId` staging | `com.kelvinht.moneylife.staging` |
| `versionName` | `1.0.0` |
| `versionName` staging | `1.0.0-staging` |
| `versionCode` | `10000` |
| JDK / Gradle / AGP | 17 / 8.11.1 / 8.9.1 |
| `minSdk` / `targetSdk` / `compileSdk` | 27 / 36 / 36 |

Kode aplikasi yang menjadi dasar RC: commit `e46cc7a`. Perubahan dokumentasi
setelah commit tersebut tidak mengubah isi AAB aplikasi.

## Perintah build dan verifikasi

```bash
cd moneylife_app_android
MONEYLIFE_SIGNING_PROPERTIES=<path-ke-signing.properties> \
  ./gradlew :app:testDebugUnitTest :app:ktlintCheck \
  :app:lintDebug :app:lintRelease :app:assembleDebugAndroidTest \
  :app:bundleRelease :app:bundleStaging

jarsigner -verify app/build/outputs/bundle/release/app-release.aab
jarsigner -verify app/build/outputs/bundle/staging/app-staging.aab
sha256sum app/build/outputs/bundle/release/app-release.aab
sha256sum app/build/outputs/bundle/staging/app-staging.aab
```

Keystore dan `signing.properties` disimpan di `.vault/moneylife-release/` dan
tidak masuk Git. Jangan pernah menyalin isi password ke dokumen ini.

## Artefak RC

| Artefak | Ukuran | SHA-256 |
| --- | ---: | --- |
| `app-release.aab` | 2.752.195 byte | `770aabeee3ce4546bbdaab5dd773546a2a56bd417222454dc18bea8b220be3b3` |
| `app-staging.aab` | 2.752.225 byte | `3ee6279df9cf112f7893134bca57e81c52bfd3c518f3eaf2f9440a79c3056243` |

`jarsigner` menyatakan **`jar verified`** untuk kedua AAB. Peringatan
*self-signed certificate* dan *no timestamp* adalah konsekuensi upload key
lokal; Play App Signing tetap wajib diaktifkan saat upload ke Play Console.

## Bukti pengujian

- Unit test: **60 test lulus** (`app/build/test-results/testDebugUnitTest`).
- `ktlintCheck`: lulus.
- `lintDebug` dan `lintRelease`: lulus (`abortOnError = true`).
- `assembleDebugAndroidTest`: lulus.
- `bundleRelease` dan `bundleStaging`: lulus.
- CI Android terakhir untuk kode aplikasi:
  [`44505218192`](https://github.com/algokelvin-373/MoneyApp/actions/runs/44505218192)
  pada `a13bba4` — `verify`, `Instrumentation API 27`, dan
  `Instrumentation API 36` hijau. Workflow juga membangun `bundleStaging`.
- Verifikasi ulang lokal RC: 21 November 2026.

## Gate yang masih terbuka

| Gate | Status | Tindakan |
| --- | --- | --- |
| R5 Play Console | ⏳ | Pemilik akun menempel privacy URL dan submit Data Safety. |
| R5 deploy halaman | ⏳ | Halaman sumber sudah diperbarui, tetapi deploy dari `staging` diblokir environment protection GitHub Pages. Izinkan `staging` atau deploy setelah merge ke `master`. |
| R7 smoke test | ✅ | Lulus pada TECNO KJ5n / Android 13 (API 33), 23 Nov 2026. Seluruh skenario release signed dan 5/5 instrumentation test hijau; lihat `docs/R7_PHYSICAL_SMOKE_TEST.md`. |
| API 36 release smoke | ⏳ | Instrumentation debug API 36 sudah hijau; APK release masih perlu smoke test singkat pada emulator API 36 untuk Definition of Done final. |
| R8 Play Console | ⏳ | Isi App content, content rating, target audience, ads, app access, store listing, testers, dan rollout. |
| R9 ekspor/restore | ⏳ | Putuskan minimal disclosure atau implementasi ekspor/impor. |
| R10 aksesibilitas | ⏳ | Uji TalkBack, font scale 1.0×/2.0×, contrast, dan touch target. |
| R11 tag final | ⏳ | Tag `v1.0.0` hanya setelah smoke APK release API 36, R8/R10, dan keputusan R9 selesai. |
| Keputusan staging | ⏳ | Tetap aplikasi terpisah (`.staging`) atau hapus suffix jika harus memakai app Play yang sama. |

## Checklist Play Console (belum dieksekusi)

- [ ] Privacy policy URL: `https://algokelvin-373.github.io/MoneyApp/`
- [ ] Data Safety: jawab memakai `docs/DATA_SAFETY.md`; tidak ada data
      dikumpulkan/dibagikan, tidak ada akun, iklan, analytics, atau server.
- [ ] Content rating.
- [ ] Target audience dan Families Policy.
- [ ] Ads declaration: tidak ada iklan.
- [ ] App access: tidak ada login.
- [ ] Store listing: ikon, feature graphic, screenshot, short/full description.
- [ ] Release notes versi `1.0.0`.
- [ ] Testers dan internal testing track.
- [ ] Rollout internal testing.

## Prosedur tag final

1. Selesaikan smoke APK release API 36, R8, dan R10; putuskan R9 dan keputusan staging. R7 sudah lulus pada perangkat fisik.
2. Freeze commit release dan pastikan working tree bersih.
3. Jalankan ulang seluruh gate dari checkout bersih.
4. Build AAB final, verifikasi `jarsigner`, hitung SHA-256 final, dan perbarui
   tabel artefak di dokumen ini serta `CHANGELOG.md`.
5. Upload AAB final ke Play Console dan simpan checksum.
6. Tag commit yang sama sebagai `v1.0.0`, lalu push tag.
