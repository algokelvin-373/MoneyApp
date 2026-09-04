# Product Requirements Document (PRD)

## Financial Literacy & Expense Management App

**Document Type:** Product Requirements Document  
**Product Stage:** Portfolio / MVP  
**Platform:** Android Native  
**Backend:** Golang REST API  
**Primary Focus:** Financial literacy, personal cash-flow tracking, and expense awareness  
**Product Owner / Project Manager:** Project Portfolio Owner  
**Document Status:** Draft v1.0

---

## 1. Product Overview

Financial Literacy & Expense Management App adalah aplikasi pencatatan keuangan pribadi yang membantu pengguna memahami arus uang mereka secara sederhana melalui pencatatan pemasukan dan pengeluaran.

Aplikasi berfokus pada tiga sumber dana utama:

1. **Dompet** — uang tunai yang dimiliki pengguna.
2. **GoPay** — saldo digital pada akun GoPay.
3. **OVO** — saldo digital pada akun OVO.

Setiap transaksi dicatat dengan status **Debit** atau **Kredit** dan dapat diberikan **Code** untuk mengidentifikasi tipe transaksi, khususnya kategori pengeluaran.

Produk ini tidak hanya bertujuan menjadi aplikasi pencatat transaksi, tetapi juga menjadi media literasi finansial yang membantu pengguna menjawab pertanyaan sederhana seperti:

- Uang saya paling banyak digunakan untuk apa?
- Saldo saya tersimpan paling banyak di mana?
- Berapa total pengeluaran saya bulan ini?
- Apakah arus kas saya masih sehat?
- Pengeluaran kategori apa yang paling sering terjadi?

---

## 2. Background & Problem Statement

Banyak pengguna mengetahui jumlah uang yang mereka miliki, tetapi tidak mengetahui pola penggunaan uang tersebut secara terstruktur.

Permasalahan yang ingin diselesaikan:

1. Pengeluaran harian sering tidak tercatat.
2. Uang tersebar pada beberapa tempat seperti tunai dan e-wallet.
3. Pengguna sulit mengetahui sumber pengeluaran terbesar.
4. Pengguna belum terbiasa membedakan pemasukan dan pengeluaran secara konsisten.
5. Pengguna tidak memiliki ringkasan arus keuangan yang sederhana dan mudah dipahami.
6. Aplikasi pencatatan keuangan yang terlalu kompleks dapat membuat pengguna enggan mencatat transaksi kecil.

Produk ini dirancang dengan pendekatan **simple financial tracking** sehingga pengguna dapat mencatat transaksi dengan cepat tanpa harus memahami konsep akuntansi yang rumit.

---

## 3. Product Vision

> Membantu pengguna membangun kebiasaan sadar finansial melalui pencatatan transaksi sederhana, terstruktur, dan mudah dipahami.

---

## 4. Product Goals

### 4.1 Primary Goals

- Mempermudah pengguna mencatat pemasukan dan pengeluaran.
- Memisahkan saldo berdasarkan sumber dana.
- Memberikan klasifikasi transaksi menggunakan Code.
- Menampilkan ringkasan kondisi keuangan secara sederhana.
- Membantu pengguna memahami pola pengeluaran.

### 4.2 Secondary Goals

- Mendorong kebiasaan mencatat transaksi secara rutin.
- Menyediakan data historis untuk evaluasi keuangan pribadi.
- Menjadi fondasi untuk fitur budgeting dan financial insight pada fase berikutnya.

---

## 5. Non-Goals — MVP

Pada versi MVP, aplikasi **tidak** berfokus pada:

- Integrasi langsung dengan API bank.
- Integrasi langsung dengan akun GoPay atau OVO.
- Pembayaran dari dalam aplikasi.
- Investasi saham, reksa dana, crypto, atau produk investasi lainnya.
- Credit scoring.
- Sistem akuntansi double-entry yang kompleks.
- Open Banking.
- Pinjaman atau layanan finansial berlisensi.

Saldo dan transaksi pada MVP dimasukkan oleh pengguna secara manual.

---

## 6. Target User

### Primary Persona

Pengguna individu yang ingin mulai mengatur keuangan pribadi tetapi membutuhkan sistem yang sederhana.

Karakteristik:

- Memiliki uang tunai dan beberapa e-wallet.
- Sering melakukan transaksi kecil setiap hari.
- Belum memiliki kebiasaan budgeting yang konsisten.
- Ingin mengetahui penggunaan uang tanpa menggunakan spreadsheet.
- Menggunakan Android sebagai perangkat utama.

---

## 7. Core Financial Concept

### 7.1 Account / Money Location

Sumber dana yang tersedia pada MVP:

| Code | Name | Description |
|---|---|---|
| `WALLET` | Dompet | Uang tunai pengguna |
| `GOPAY` | GoPay | Saldo GoPay |
| `OVO` | OVO | Saldo OVO |

Struktur harus dibuat extensible agar sumber dana lain dapat ditambahkan tanpa perubahan besar pada arsitektur.

Contoh pengembangan selanjutnya:

- Bank BCA
- Bank Mandiri
- DANA
- ShopeePay
- Tabungan
- Dana Darurat

---

## 8. Debit & Credit Definition

Untuk menghindari kebingungan pengguna, istilah transaksi pada aplikasi didefinisikan sebagai berikut:

### Debit

**Debit = uang masuk ke saldo pengguna.**

Contoh:

- Gaji masuk.
- Pengembalian uang dari teman.
- Cashback.
- Penambahan saldo secara manual.

### Credit

**Credit = uang keluar dari saldo pengguna.**

Contoh:

- Membeli makanan.
- Membayar transportasi.
- Membeli barang.
- Membayar tagihan.

> Catatan produk: definisi Debit/Kredit pada aplikasi dibuat dari perspektif pengguna agar mudah dipahami. Label UI dapat dikembangkan menjadi **Uang Masuk** dan **Uang Keluar**, sementara backend tetap menyimpan enum `DEBIT` dan `CREDIT`.

---

## 9. Transaction Code

Setiap transaksi memiliki **Code** untuk mengidentifikasi jenis transaksi.

### 9.1 Initial Expense Codes

| Code | Category | Example |
|---|---|---|
| `FOOD` | Makanan & Minuman | makan siang, kopi |
| `TRANSPORT` | Transportasi | ojol, bensin, parkir |
| `SHOPPING` | Belanja | pakaian, kebutuhan pribadi |
| `BILL` | Tagihan | listrik, internet, pulsa |
| `ENTERTAINMENT` | Hiburan | bioskop, game, billiard |
| `HEALTH` | Kesehatan | obat, klinik |
| `EDUCATION` | Pendidikan | buku, kursus |
| `SOCIAL` | Sosial | traktir, donasi, hadiah |
| `OTHER_EXPENSE` | Pengeluaran Lainnya | transaksi yang belum terklasifikasi |

### 9.2 Initial Income Codes

| Code | Category | Example |
|---|---|---|
| `SALARY` | Gaji | gaji bulanan |
| `BONUS` | Bonus | bonus pekerjaan |
| `REFUND` | Refund | pengembalian uang |
| `CASHBACK` | Cashback | cashback e-wallet |
| `OTHER_INCOME` | Pemasukan Lainnya | pemasukan lainnya |

### 9.3 Code Rules

- Code wajib memiliki identifier unik.
- Code memiliki tipe `INCOME` atau `EXPENSE`.
- Code dapat memiliki status aktif/nonaktif.
- Backend harus menjadi source of truth daftar Code.
- Android tidak boleh hard-code seluruh daftar Code secara permanen.

---

## 10. MVP Scope

### In Scope

1. Melihat total saldo seluruh akun.
2. Melihat saldo Dompet, GoPay, dan OVO.
3. Menambahkan transaksi Debit.
4. Menambahkan transaksi Kredit.
5. Memilih account sumber transaksi.
6. Memilih Code transaksi.
7. Menambahkan nominal transaksi.
8. Menambahkan tanggal transaksi.
9. Menambahkan catatan transaksi.
10. Melihat riwayat transaksi.
11. Filter riwayat berdasarkan periode.
12. Filter transaksi berdasarkan account.
13. Filter transaksi berdasarkan Debit/Kredit.
14. Filter transaksi berdasarkan Code.
15. Melihat total pemasukan.
16. Melihat total pengeluaran.
17. Melihat pengeluaran berdasarkan kategori.
18. Edit transaksi.
19. Hapus transaksi.
20. Sinkronisasi data melalui API.

---

## 11. Core User Journey

### Journey A — Add Expense

1. User membuka aplikasi.
2. User melihat dashboard saldo.
3. User menekan **Tambah Transaksi**.
4. User memilih **Uang Keluar / Credit**.
5. User memilih sumber dana, misalnya GoPay.
6. User memasukkan nominal.
7. User memilih Code, misalnya `FOOD`.
8. User menambahkan catatan opsional.
9. User menyimpan transaksi.
10. Sistem mengurangi saldo GoPay.
11. Dashboard dan riwayat transaksi diperbarui.

### Journey B — Add Income

1. User menekan **Tambah Transaksi**.
2. User memilih **Uang Masuk / Debit**.
3. User memilih account tujuan.
4. User memasukkan nominal.
5. User memilih Code pemasukan.
6. User menyimpan transaksi.
7. Sistem menambah saldo account.

### Journey C — Review Monthly Spending

1. User membuka Dashboard / Insight.
2. User memilih periode bulan berjalan.
3. Sistem menampilkan total pemasukan.
4. Sistem menampilkan total pengeluaran.
5. Sistem menampilkan kategori pengeluaran terbesar.
6. User dapat membuka detail transaksi dari kategori terkait.

---

## 12. Functional Requirements

## FR-01 — Dashboard

Dashboard harus menampilkan:

- Total saldo seluruh account.
- Saldo Dompet.
- Saldo GoPay.
- Saldo OVO.
- Total Debit periode aktif.
- Total Credit periode aktif.
- Selisih arus kas.
- Transaksi terbaru.

### Acceptance Criteria

- Total saldo adalah penjumlahan seluruh account aktif.
- Nilai dashboard harus diperbarui setelah transaksi baru berhasil disimpan.
- User dapat melihat nominal dalam format Rupiah.

---

## FR-02 — Create Transaction

Field minimum:

- Transaction Type: `DEBIT` / `CREDIT`
- Account
- Transaction Code
- Amount
- Transaction Date
- Note — optional

### Validation

- Amount harus lebih besar dari 0.
- Account wajib dipilih.
- Code wajib dipilih.
- Transaction Type wajib dipilih.
- Tanggal tidak boleh kosong.
- Untuk transaksi Credit, saldo account tidak boleh menjadi negatif pada MVP kecuali fitur negative balance diaktifkan pada fase berikutnya.

---

## FR-03 — Transaction History

User dapat melihat transaksi secara kronologis.

Informasi minimum:

- Tanggal
- Code / kategori
- Account
- Nominal
- Debit / Credit
- Catatan

Filter minimum:

- Rentang tanggal
- Account
- Transaction Type
- Code

---

## FR-04 — Edit Transaction

User dapat mengubah transaksi yang sudah dibuat.

Sistem harus melakukan recalculation saldo.

Contoh:

Saldo GoPay awal setelah transaksi = Rp80.000.

User mengubah transaksi pengeluaran dari Rp20.000 menjadi Rp10.000.

Saldo harus dikoreksi menjadi Rp90.000.

---

## FR-05 — Delete Transaction

User dapat menghapus transaksi.

Sebelum transaksi dihapus:

- Sistem menampilkan confirmation dialog.
- Setelah transaksi dihapus, saldo account harus dikalkulasi ulang.

Disarankan menggunakan **soft delete** di backend.

---

## FR-06 — Spending Summary

User dapat melihat:

- Total pengeluaran per hari.
- Total pengeluaran per minggu.
- Total pengeluaran per bulan.
- Total berdasarkan Code.
- Persentase setiap kategori terhadap total pengeluaran.

---

## FR-07 — Account Management

Pada MVP account utama adalah:

- Dompet
- GoPay
- OVO

Setiap account memiliki:

- ID
- Code
- Name
- Balance
- Status

Pada fase berikutnya user dapat membuat account sendiri.

---

## 13. Suggested Screens — Android

### 13.1 Splash Screen

Menampilkan branding aplikasi sebelum masuk ke aplikasi.

### 13.2 Dashboard

Komponen:

- Total Saldo
- Card Dompet
- Card GoPay
- Card OVO
- Total Pemasukan
- Total Pengeluaran
- Quick Add Transaction
- Recent Transactions

### 13.3 Add Transaction

Komponen:

- Debit / Credit selector
- Account selector
- Amount input
- Category / Code selector
- Date picker
- Note input
- Save button

### 13.4 Transaction History

Komponen:

- Search
- Filter
- Transaction list
- Date grouping

### 13.5 Transaction Detail

Komponen:

- Amount
- Type
- Account
- Code
- Date
- Note
- Edit
- Delete

### 13.6 Financial Insight

Komponen:

- Income vs Expense
- Expense by Category
- Monthly Summary
- Largest Expense Category

---

## 14. Data Model — High Level

## 14.1 users

```text
id
name
email
password_hash
created_at
updated_at
```

## 14.2 accounts

```text
id
user_id
code
name
balance
is_active
created_at
updated_at
```

Example:

```text
WALLET | Dompet
GOPAY  | GoPay
OVO    | OVO
```

## 14.3 transaction_codes

```text
id
code
name
type
is_active
created_at
updated_at
```

Type:

```text
INCOME
EXPENSE
```

## 14.4 transactions

```text
id
user_id
account_id
transaction_code_id
transaction_type
amount
transaction_date
note
created_at
updated_at
deleted_at
```

Transaction type:

```text
DEBIT
CREDIT
```

---

## 15. Balance Calculation

Balance account dapat dihitung dengan konsep:

```text
Current Balance = Initial Balance + Total Debit - Total Credit
```

Contoh:

```text
Saldo Awal GoPay     : Rp100.000
Debit / Uang Masuk   : Rp50.000
Credit / Uang Keluar : Rp30.000

Saldo Akhir          : Rp120.000
```

Backend harus menjadi source of truth dalam perhitungan saldo.

Android tidak boleh menentukan saldo final hanya dari local state tanpa validasi backend.

---

## 16. API Requirements — Golang

API menggunakan prinsip RESTful.

Base example:

```text
/api/v1
```

### Authentication

```text
POST /auth/register
POST /auth/login
POST /auth/refresh
POST /auth/logout
```

### Account

```text
GET    /accounts
GET    /accounts/{id}
POST   /accounts
PATCH  /accounts/{id}
```

### Transaction Code

```text
GET /transaction-codes
GET /transaction-codes?type=EXPENSE
GET /transaction-codes?type=INCOME
```

### Transaction

```text
GET    /transactions
GET    /transactions/{id}
POST   /transactions
PATCH  /transactions/{id}
DELETE /transactions/{id}
```

Example filter:

```text
GET /transactions?account=GOPAY&type=CREDIT&code=FOOD&start_date=2026-08-01&end_date=2026-08-31
```

### Dashboard

```text
GET /dashboard/summary
```

Example response:

```json
{
  "total_balance": 1250000,
  "total_income": 5000000,
  "total_expense": 3750000,
  "cash_flow": 1250000,
  "accounts": [
    {
      "code": "WALLET",
      "balance": 250000
    },
    {
      "code": "GOPAY",
      "balance": 600000
    },
    {
      "code": "OVO",
      "balance": 400000
    }
  ]
}
```

### Insight

```text
GET /insights/spending
GET /insights/cash-flow
GET /insights/categories
```

---

## 17. API Response Standard

Success:

```json
{
  "success": true,
  "message": "Transaction created successfully",
  "data": {}
}
```

Error:

```json
{
  "success": false,
  "message": "Insufficient account balance",
  "error_code": "INSUFFICIENT_BALANCE"
}
```

---

## 18. Android Technical Requirements

Recommended architecture:

```text
UI
↓
ViewModel
↓
Use Case
↓
Repository
↓
Remote Data Source / Local Data Source
↓
REST API
```

Recommended Android stack:

- Kotlin
- Android Native
- MVVM or Clean Architecture
- Retrofit / OkHttp
- Kotlin Coroutines
- Flow / StateFlow
- Room Database
- Hilt Dependency Injection
- Jetpack Navigation
- Material Design 3

Room digunakan untuk:

- Cache account.
- Cache transaction history.
- Menampilkan data terakhir ketika koneksi tidak tersedia.

Backend tetap menjadi source of truth.

---

## 19. Backend Technical Requirements

Recommended Golang structure:

```text
cmd/
internal/
  handler/
  service/
  repository/
  model/
  middleware/
  config/
pkg/
migrations/
```

Recommended components:

- Golang
- Gin / Echo / Fiber
- PostgreSQL or MySQL
- JWT Authentication
- Database migration
- Structured logging
- Request validation
- Unit testing
- Swagger / OpenAPI documentation

---

## 20. Business Rules

### BR-01

Setiap transaksi wajib terhubung ke satu account.

### BR-02

Setiap transaksi wajib memiliki satu Code.

### BR-03

Debit menambah saldo account.

### BR-04

Credit mengurangi saldo account.

### BR-05

Nominal transaksi harus positif.

### BR-06

Transaksi tidak boleh menghasilkan saldo negatif pada MVP.

### BR-07

Edit dan delete transaksi harus melakukan penyesuaian saldo.

### BR-08

Semua nominal disimpan menggunakan integer terkecil dalam Rupiah untuk menghindari floating-point error.

Example:

```text
Rp25.000 => 25000
```

### BR-09

Transaction Code yang inactive tidak dapat digunakan pada transaksi baru tetapi tetap tampil pada transaksi historis.

---

## 21. Financial Insight Rules

Insight awal yang dapat diberikan:

### Spending by Category

```text
FOOD          Rp1.000.000
TRANSPORT     Rp500.000
ENTERTAINMENT Rp300.000
```

### Expense Percentage

```text
Food = Food Expense / Total Expense × 100%
```

### Cash Flow

```text
Cash Flow = Total Debit - Total Credit
```

Status sederhana:

| Condition | Status |
|---|---|
| Debit > Credit | Positive Cash Flow |
| Debit = Credit | Balanced |
| Debit < Credit | Negative Cash Flow |

---

## 22. Financial Literacy Layer

Untuk membedakan produk dari aplikasi expense tracker biasa, aplikasi dapat memberikan edukasi berbasis data pengguna.

Contoh:

> Pengeluaran makanan Anda mencapai 38% dari total pengeluaran bulan ini.

> Pengeluaran bulan ini meningkat 15% dibanding bulan sebelumnya.

> Anda paling sering menggunakan GoPay untuk transaksi makanan.

Pada MVP insight bersifat informatif dan tidak memberikan nasihat investasi atau keputusan finansial berisiko tinggi.

---

## 23. Analytics Events

Event yang disarankan:

```text
app_open
transaction_create_open
transaction_created
transaction_updated
transaction_deleted
dashboard_viewed
transaction_history_viewed
transaction_filter_used
financial_insight_viewed
```

Parameter example:

```text
account_type
transaction_type
transaction_code
amount_range
```

Data analytics tidak boleh menyimpan informasi finansial sensitif secara tidak diperlukan.

---

## 24. Security Requirements

- Seluruh API menggunakan HTTPS.
- Password tidak disimpan dalam plain text.
- Password menggunakan hashing yang aman.
- Authentication menggunakan access token dan refresh token.
- API wajib melakukan authorization berdasarkan user ID.
- User hanya dapat mengakses transaksi miliknya sendiri.
- Input API wajib divalidasi.
- Rate limiting diterapkan pada endpoint authentication.
- Sensitive token tidak ditulis ke application log.
- Android menggunakan EncryptedSharedPreferences / DataStore yang aman untuk credential token.
- Backend melakukan audit logging untuk aktivitas penting.

---

## 25. Non-Functional Requirements

### Performance

- Dashboard target response API < 1 second pada kondisi normal.
- Transaction history menggunakan pagination.

### Reliability

- Pembuatan transaksi dan perubahan saldo harus atomic.
- Database transaction wajib digunakan pada operasi yang mengubah transaksi dan saldo.

### Scalability

Struktur account dan transaction code harus dapat menerima penambahan tipe baru tanpa migration yang kompleks.

### Maintainability

- Backend menggunakan separation of concerns.
- Android menggunakan modular architecture jika scope aplikasi berkembang.
- API terdokumentasi melalui OpenAPI/Swagger.

---

## 26. Edge Cases

Sistem harus menangani:

- User memasukkan amount 0.
- User memasukkan amount negatif.
- Credit melebihi saldo.
- Transaksi di-submit dua kali.
- Request timeout setelah user menekan Save.
- Edit transaksi dari account A ke account B.
- Edit Debit menjadi Credit.
- Delete transaksi lama.
- Code sudah inactive.
- Account sudah inactive.
- User offline ketika membuka transaction history.

Untuk menghindari duplicate transaction, client dapat mengirim `idempotency_key` pada create transaction.

---

## 27. MVP Acceptance Criteria

MVP dianggap siap apabila:

- User dapat login.
- User dapat melihat Dompet, GoPay, dan OVO.
- User dapat menambahkan transaksi Debit.
- User dapat menambahkan transaksi Credit.
- Saldo berubah dengan benar setelah transaksi.
- User dapat memilih Code transaksi.
- User dapat melihat transaction history.
- User dapat menggunakan filter transaksi.
- User dapat edit transaksi.
- User dapat delete transaksi.
- Dashboard menampilkan total pemasukan dan pengeluaran.
- Insight menampilkan pengeluaran berdasarkan kategori.
- Seluruh endpoint utama memiliki validation dan error handling.
- Data antar-user terisolasi.
- Aplikasi tetap menampilkan cached data ketika koneksi internet sementara tidak tersedia.

---

## 28. Prioritization

### P0 — Must Have

- Authentication
- Account balance
- Dompet / GoPay / OVO
- Debit
- Credit
- Transaction Code
- Add transaction
- Transaction history
- Edit transaction
- Delete transaction
- Dashboard summary

### P1 — Should Have

- Filter history
- Monthly summary
- Spending category chart
- Offline cache
- Financial insight

### P2 — Nice to Have

- Custom account
- Custom transaction code
- Budgeting
- Saving goals
- Recurring transactions
- Export CSV
- Backup
- Push reminder

---

## 29. Product Roadmap

### Phase 1 — Foundation

- Android project setup
- Backend project setup
- Authentication
- Database schema
- Default account
- Transaction Code master

### Phase 2 — Transaction MVP

- Create transaction
- Transaction history
- Edit transaction
- Delete transaction
- Balance calculation

### Phase 3 — Dashboard

- Total balance
- Account balance
- Income summary
- Expense summary
- Recent transactions

### Phase 4 — Financial Literacy

- Monthly spending breakdown
- Category analysis
- Cash-flow summary
- Basic financial insight

### Phase 5 — Advanced Personal Finance

Potential future features:

- Monthly budget limit
- Saving target
- Emergency fund tracking
- Recurring transaction
- Custom accounts
- Export report
- Notification reminder
- Expense anomaly detection

---

## 30. Success Metrics

Untuk portfolio project, metrics dapat dibuat sebagai product hypothesis.

### Product Metrics

- Transaction Creation Completion Rate
- Average Transactions Logged per Active User
- Weekly Active Users
- Monthly Active Users
- Transaction Logging Retention
- Percentage of Users Viewing Monthly Insight

### Suggested MVP Targets

| Metric | Target |
|---|---:|
| Transaction creation success rate | > 98% |
| API error rate | < 1% |
| Dashboard API P95 | < 1.5 sec |
| Crash-free Android sessions | > 99.5% |
| Transaction sync success | > 99% |

---

## 31. QA Scope

Minimum testing:

### Functional Testing

- Create Debit.
- Create Credit.
- Edit transaction.
- Delete transaction.
- Filter transaction.
- Balance calculation.

### Negative Testing

- Amount 0.
- Negative amount.
- Credit > balance.
- Missing Code.
- Missing account.
- Invalid token.
- User accessing another user's transaction.

### API Testing

- Status code validation.
- Response schema validation.
- Authorization.
- Pagination.
- Filtering.
- Idempotency.

### Android Testing

- Online state.
- Offline state.
- Poor connection.
- App restart.
- Process death.
- Orientation / configuration handling where applicable.

---

## 32. Definition of Done

Sebuah feature dianggap selesai apabila:

- Requirement sudah diimplementasikan.
- Acceptance criteria terpenuhi.
- Unit test tersedia untuk business logic penting.
- API sudah terdokumentasi.
- QA test sudah Passed.
- Tidak terdapat blocker atau critical bug.
- Error state sudah ditangani.
- Loading state sudah ditangani.
- Empty state sudah ditangani.
- Code sudah direview.
- Feature sudah terintegrasi dengan branch utama development.

---

## 33. Suggested Portfolio Positioning

Project ini dapat diposisikan sebagai:

> **Personal Financial Literacy & Expense Management Platform** yang dibangun menggunakan Android Native dan Golang, dengan fokus pada pencatatan cash flow, multi-wallet balance management, transaction categorization, dan basic financial insight.

Nilai teknis yang dapat ditonjolkan:

- Native Android architecture.
- REST API menggunakan Golang.
- Secure authentication.
- Transactional balance management.
- Financial data categorization.
- Offline caching.
- Dashboard dan insight.
- Product requirement yang dirancang dari perspektif bisnis dan user behavior.

---

## 34. Open Questions for Next Iteration

Beberapa keputusan produk yang perlu ditentukan sebelum pengembangan lanjutan:

1. Apakah user wajib login atau tersedia local-only mode?
2. Apakah saldo awal account dimasukkan saat onboarding?
3. Apakah transfer antar account perlu tersedia pada MVP?
4. Jika transfer Dompet → GoPay dilakukan, apakah dicatat sebagai dua transaksi atau satu transaction type `TRANSFER`?
5. Apakah user diperbolehkan memiliki saldo negatif?
6. Apakah user dapat membuat Code sendiri?
7. Apakah target awal hanya satu currency, yaitu IDR?
8. Apakah aplikasi membutuhkan biometric lock?
9. Apakah financial insight hanya rule-based atau akan menggunakan AI pada fase berikutnya?
10. Apakah data transaksi akan disimpan cloud-only, local-first, atau hybrid sync?

---

## 35. Recommended Next Product Decision

Sebelum mulai implementasi, keputusan paling penting adalah mendefinisikan **TRANSFER antar account**.

Contoh:

```text
Dompet -> GoPay Rp100.000
```

Transaksi ini sebenarnya bukan pengeluaran karena total kekayaan pengguna tidak berubah.

Karena itu pada iterasi berikutnya disarankan menambahkan transaction type:

```text
DEBIT
CREDIT
TRANSFER
```

Dengan rule:

```text
TRANSFER:
source account      -= amount
destination account += amount
total balance        = unchanged
```

Fitur ini penting apabila aplikasi akan digunakan sebagai tracker keuangan multi-account secara akurat.

---

# End of PRD
