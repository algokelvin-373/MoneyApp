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
                  displayName: result.account!.avatarUri,
                  avatarUri: result.account!.avatarUri, // 👈 tambahkan ini
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

  const MyHomePage({
    super.key,
    required this.title,
    this.displayName,
    this.avatarUri,
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
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            // 🖼️ Avatar
            if (widget.avatarUri != null && widget.avatarUri!.isNotEmpty)
              CircleAvatar(
                radius: 40,
                backgroundImage: NetworkImage(widget.avatarUri!),
                onBackgroundImageError:
                    (_, __) => const Icon(Icons.person, size: 40),
              )
            else
              const CircleAvatar(
                radius: 40,
                child: Icon(Icons.person, size: 40),
              ),
            const SizedBox(height: 16),

            // 👋 Sapaan
            if (widget.displayName != null)
              Text(
                'Halo, ${widget.displayName}!',
                style: Theme.of(context).textTheme.headlineSmall,
              ),
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }
}
