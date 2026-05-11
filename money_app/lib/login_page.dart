// ------------------------------------------
// Halaman Login (dengan pengecekan otomatis)
// ------------------------------------------

import 'package:flutter/material.dart';
import 'package:huawei_account/huawei_account.dart';
import 'package:money_app/auth_service.dart';
import 'package:money_app/home_page.dart';

class LoginPage extends StatefulWidget {
  final bool afterLogout; // <-- tambahkan

  const LoginPage({super.key, this.afterLogout = false});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  bool _isLoading = false;
  bool _checkingSession = true;

  @override
  void initState() {
    super.initState();
    _checkExistingSession();
  }

  Future<void> _checkExistingSession() async {
    // Jika habis logout, langsung tampilkan tombol login
    if (widget.afterLogout) {
      setState(() => _checkingSession = false);
      return;
    }

    final account = await AuthService().silentLoginOnly();
    if (!mounted) return;
    if (account != null) {
      _goToHome(account);
    } else {
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
        _goToHome(result.account!);
      } else {
        // Tampilkan pesan sesuai method
        final message = switch (result.method) {
          'cancelled' =>
            result.errMessage.isNotEmpty
                ? 'Login dibatalkan: ${result.errMessage}'
                : 'Login dibatalkan.',
          'error' =>
            result.errMessage.isNotEmpty
                ? 'Terjadi kesalahan teknis: ${result.errMessage}'
                : 'Terjadi kesalahan teknis.',
          _ => 'Gagal login.',
        };
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(message)));
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('Gagal login: $e')));
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  void _goToHome(AuthAccount account) {
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
              carrierId: account.carrierId,
            ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
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
