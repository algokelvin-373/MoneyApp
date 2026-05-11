import 'package:flutter/material.dart';
import 'auth_service.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Keuangan Pribadi',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
      ),
      home: const LoginPage(), // pengecekan dilakukan di sini
    );
  }
}

// ------------------------------------------
// Halaman Login (dengan pengecekan otomatis)
// ------------------------------------------
class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  bool _isLoading = false;
  bool _checkingSession = true; // sedang mengecek sesi?

  @override
  void initState() {
    super.initState();
    _checkExistingSession();
  }

  Future<void> _checkExistingSession() async {
    // Coba login diam-diam
    final account = await AuthService().silentLoginOnly();
    if (!mounted) return;
    if (account != null) {
      // Sudah login → langsung masuk dashboard
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder:
              (_) => MyHomePage(
                title: 'Dashboard Keuangan',
                displayName: account.displayName,
                avatarUri: account.avatarUri,
                email: account.email,
                gender: account.gender,
                unionId: account.unionId,
              ),
        ),
      );
    } else {
      // Belum login → tampilkan tombol login
      setState(() => _checkingSession = false);
    }
  }

  Future<void> _loginWithHuawei() async {
    setState(() => _isLoading = true);
    try {
      final result = await AuthService().login();
      if (!mounted) return;

      if (result.account != null) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              'Metode masuk: ${result.method == 'silent' ? 'Silent Sign-In' : 'Sign-In'}',
            ),
            duration: const Duration(seconds: 1),
          ),
        );
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder:
                (_) => MyHomePage(
                  title: 'Dashboard Keuangan',
                  displayName: result.account!.displayName,
                  avatarUri: result.account!.avatarUri,
                  email: result.account!.email,
                  gender: result.account!.gender,
                  unionId: result.account!.unionId,
                ),
          ),
        );
      } else {
        String message = 'Gagal login.';
        if (result.method == 'cancelled') {
          message =
              result.errMessage.isNotEmpty
                  ? 'Login dibatalkan: ${result.errMessage}'
                  : 'Login dibatalkan.';
        }
        if (result.method == 'error1') {
          message =
              result.errMessage.isNotEmpty
                  ? 'Terjadi kesalahan teknis 1: ${result.errMessage}'
                  : 'Terjadi kesalahan teknis 1.';
        }
        if (result.method == 'error2') {
          message =
              result.errMessage.isNotEmpty
                  ? 'Terjadi kesalahan teknis 2: ${result.errMessage}'
                  : 'Terjadi kesalahan teknis 2.';
        }
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(message)));
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('Gagal login: ${e.toString()}')));
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    // Saat mengecek sesi, tampilkan indikator loading
    if (_checkingSession) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text('Login'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
      body: Center(
        child:
            _isLoading
                ? const CircularProgressIndicator()
                : ElevatedButton.icon(
                  icon: const Icon(Icons.account_circle),
                  label: const Text('Masuk dengan Huawei ID'),
                  onPressed: _loginWithHuawei,
                  style: ElevatedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 24,
                      vertical: 12,
                    ),
                  ),
                ),
      ),
    );
  }
}

// ------------------------------------------
// Halaman Utama (after login) dengan Logout
// ------------------------------------------
class MyHomePage extends StatefulWidget {
  final String title;
  final String? displayName;
  final String? avatarUri;
  final String? email;
  final int? gender;
  final String? unionId;

  const MyHomePage({
    super.key,
    required this.title,
    this.displayName,
    this.avatarUri,
    this.email,
    this.gender,
    this.unionId,
  });

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  Future<void> _logout() async {
    await AuthService().logout();
    if (!mounted) return;
    // Kembali ke halaman login, hapus semua history
    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(builder: (_) => const LoginPage()),
      (route) => false,
    );
  }

  Widget _buildInfoRow(String label, String? value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 80,
            child: Text(
              '$label:',
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
          ),
          Expanded(child: Text(value ?? 'Tidak tersedia')),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Keluar',
            onPressed: _logout,
          ),
        ],
      ),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // Foto profil
              CircleAvatar(
                radius: 50,
                backgroundImage:
                    widget.avatarUri != null && widget.avatarUri!.isNotEmpty
                        ? NetworkImage(widget.avatarUri!)
                        : null,
                child:
                    (widget.avatarUri == null || widget.avatarUri!.isEmpty)
                        ? const Icon(Icons.person, size: 50)
                        : null,
              ),
              const SizedBox(height: 20),

              // Nama
              if (widget.displayName != null)
                Text(
                  'Halo, ${widget.displayName}!',
                  style: Theme.of(context).textTheme.headlineSmall,
                ),

              const SizedBox(height: 16),

              // Kartu data
              Card(
                margin: const EdgeInsets.symmetric(vertical: 8),
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      _buildInfoRow('Nama', widget.displayName),
                      _buildInfoRow('Email', widget.email),
                      _buildInfoRow(
                        'Jenis Kelamin',
                        widget.gender != null
                            ? (widget.gender == 1 ? 'Perempuan' : 'Laki-laki')
                            : 'Tidak tersedia',
                      ),
                      _buildInfoRow('Union ID', widget.unionId),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
