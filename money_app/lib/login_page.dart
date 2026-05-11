// ------------------------------------------
// Halaman Login (dengan pengecekan otomatis)
// ------------------------------------------

import 'package:flutter/material.dart';
import 'package:money_app/auth_service.dart';
import 'package:money_app/main.dart';

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
                carrierId: account.carrierId, // Gunakan carrierId
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
                  carrierId: result.account!.carrierId,
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
