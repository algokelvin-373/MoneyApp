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

## URL privacy policy

Gunakan URL berikut setelah commit ini tersedia di remote:

`https://raw.githubusercontent.com/algokelvin-373/MoneyApp/dev/improvements/moneylife_app_android/PRIVACY_POLICY.md`

Sebelum production release, gunakan URL dari branch/tag permanen atau situs
publik yang tidak akan dihapus.
