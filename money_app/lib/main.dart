import 'package:flutter/material.dart';
import 'auth_service.dart'; // pastikan import sesuai

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
      home: const LoginPage(),
    );
  }
}

// ------------------------------------------
// Halaman Login
// ------------------------------------------
class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  bool _isLoading = false;

  Future<void> _loginWithHuawei() async {
    setState(() => _isLoading = true);
    try {
      final result = await AuthService().login();

      if (!mounted) return;

      if (result.account != null) {
        // Tampilkan snackbar singkat "Metode masuk: ..." sebelum pindah halaman
        final methodText =
            result.method == 'silent' ? 'Silent Sign-In' : 'Sign-In';
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Metode masuk: $methodText'),
            duration: const Duration(seconds: 1),
          ),
        );

        // Langsung pindah ke halaman utama sambil membawa info metode login
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder:
                (_) => MyHomePage(
                  title: 'Dashboard Keuangan',
                  loginMethod: result.method,
                ),
          ),
        );
      } else {
        // result.account == null → tangani sesuai penyebabnya
        String message;
        switch (result.method) {
          case 'cancelled':
            message = 'Login dibatalkan.';
            break;
          case 'error':
            message = 'Terjadi kesalahan teknis. Coba lagi.';
            break;
          default:
            message = 'Gagal login.';
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
// Halaman Utama (setelah login sukses)
// ------------------------------------------
class MyHomePage extends StatefulWidget {
  final String title;
  final String? loginMethod; // 'silent', 'explicit', dll.

  const MyHomePage({super.key, required this.title, this.loginMethod});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;

  @override
  void initState() {
    super.initState();
    // Tampilkan snackbar sambutan begitu halaman pertama kali ditampilkan
    if (widget.loginMethod != null) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        final methodText =
            widget.loginMethod == 'silent' ? 'Silent Sign-In' : 'Sign-In';
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Selamat datang! kamu sukses masuk via $methodText'),
            duration: const Duration(seconds: 2),
            backgroundColor: Colors.green,
          ),
        );
      });
    }
  }

  void _incrementCounter() {
    setState(() {
      _counter++;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text('Login berhasil! Counter:'),
            Text(
              '$_counter',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        tooltip: 'Increment',
        child: const Icon(Icons.add),
      ),
    );
  }
}
