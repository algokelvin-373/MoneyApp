// auth_service.dart
import 'package:flutter/services.dart';
import 'package:huawei_account/huawei_account.dart';

class LoginResult {
  final AuthAccount? account;
  final String method; // 'silent', 'explicit', 'cancelled', 'error'

  LoginResult({this.account, required this.method});
}

class AuthService {
  final AccountAuthParamsHelper _paramsHelper = AccountAuthParamsHelper();

  Future<LoginResult> login() async {
    final AccountAuthService authService = AccountAuthManager.getService(
      _paramsHelper.createParams(),
    );

    try {
      // 1. Coba silent sign-in
      final account = await authService.silentSignIn();
      return LoginResult(account: account, method: 'silent');
    } on PlatformException catch (e) {
      if (e.code == '2002') {
        // 2. Belum pernah izin → tampilkan UI login
        final account = await authService.signIn();
        if (account != null) {
          return LoginResult(account: account, method: 'explicit');
        } else {
          // Pengguna membatalkan dialog signIn
          return LoginResult(account: null, method: 'cancelled');
        }
      }
      // Error lain (konfigurasi, jaringan, dll.)
      print('Login error (${e.code}): ${e.message}');
      return LoginResult(account: null, method: 'error');
    } catch (e) {
      print('Login error: $e');
      return LoginResult(account: null, method: 'error');
    }
  }

  Future<void> logout() async {
    try {
      final AccountAuthService authService = AccountAuthManager.getService(
        _paramsHelper.createParams(),
      );
      await authService.signOut();
    } catch (e) {
      print('Logout error: $e');
    }
  }
}
