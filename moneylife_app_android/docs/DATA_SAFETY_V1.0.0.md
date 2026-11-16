# Deklarasi Data Safety — MoneyLife v1.0.0

Dokumen ini adalah sumber jawaban untuk form **Data safety** di Play Console.
Verifikasi ulang terhadap source code dan seluruh SDK sebelum setiap rilis.

## Ringkasan jawaban

- Apakah aplikasi mengumpulkan atau membagikan jenis data pengguna yang wajib
  dideklarasikan? **Tidak.** Data finansial hanya diproses dan disimpan lokal;
  tidak pernah ditransmisikan keluar perangkat.
- Apakah data dibagikan kepada pihak ketiga? **Tidak.**
- Apakah aplikasi memiliki akun pengguna? **Tidak.**
- Apakah pengguna dapat meminta penghapusan data? **Tidak relevan untuk server**
  karena tidak ada akun/server. Pengguna dapat menghapus transaksi di aplikasi;
  uninstall menghapus seluruh data lokal.
- Apakah data dienkripsi saat transit? **Tidak relevan**, karena aplikasi tidak
  mentransmisikan data.
- Apakah pengguna dapat memilih ikut/tidak ikut pengumpulan data? **Tidak
  relevan**, karena tidak ada pengumpulan data.
- Apakah aplikasi mengikuti Families Policy? **Tidak ditujukan khusus untuk
  anak-anak.** Target audience final tetap harus dipilih secara akurat di Play
  Console.
- Apakah aplikasi berisi iklan? **Tidak.**
- Apakah aplikasi telah memperoleh independent security review? **Tidak.**

## Bukti implementasi

- Manifest tidak mendeklarasikan `android.permission.INTERNET` maupun permission
  berbahaya.
- Data disimpan melalui Room di sandbox aplikasi.
- `android:allowBackup="false"` mencegah backup otomatis data finansial.
- Tidak ada SDK analytics, ads, crash reporting, authentication, atau cloud.
- Privacy policy tersedia penuh di dalam aplikasi dan di `PRIVACY_POLICY.md`.

## URL privacy policy (publik)

`https://algokelvin-373.github.io/MoneyApp/`

- Sumber halaman: `moneylife_app_android/docs/privacy-policy/index.html`.
- Deploy: `.github/workflows/pages.yml` (GitHub Pages). Artifact hanya memuat
  folder `docs/privacy-policy`, sehingga dokumen internal dan source code tidak
  ikut dipublikasikan.
- Repo `MoneyApp` bersifat private, jadi URL `raw.githubusercontent.com` tidak
  dapat dipakai (selalu 404 untuk pengguna tanpa login).
- Karena repo private, GitHub Pages harus diaktifkan oleh pemilik repo di
  **Settings → Pages → Source: GitHub Actions** (memerlukan GitHub Pro).
- Verifikasi wajib: buka URL pada jendela private/incognito **tanpa login**,
  pastikan HTTP 200, sertifikat HTTPS valid, dan isinya sama dengan
  `PRIVACY_POLICY.md` serta kebijakan di dalam aplikasi.
- String aplikasi `privacy_policy_url` harus tetap sama dengan URL di atas.

## Langkah submit di Play Console

1. **App content → Privacy policy**: tempel URL publik di atas, lalu simpan.
2. **App content → Data safety**: jawab formulir memakai ringkasan di dokumen
   ini (tidak ada data dikumpulkan/dibagikan, tidak ada akun, tidak ada iklan,
   tidak ada analytics), lalu submit.
3. Simpan tangkapan layar konfirmasi dan tanggal submit sebagai bukti release.
