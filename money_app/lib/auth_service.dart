import 'package:flutter/services.dart';
import 'package:huawei_account/huawei_account.dart';

class LoginResult {
  final AuthAccount? account;
  final String method; // 'silent', 'explicit', 'cancelled', 'error'

  LoginResult({this.account, required this.method});
}

class AuthService {
  final AccountAuthParamsHelper _paramsHelper = AccountAuthParamsHelper();

  /// Pengecekan diam-diam – TIDAK akan menampilkan UI login.
  Future<AuthAccount?> silentLoginOnly() async {
    final AccountAuthService authService = AccountAuthManager.getService(
      _paramsHelper.createParams(),
    );
    try {
      return await authService.silentSignIn();
    } catch (e) {
      // Jika belum ada izin (2002) atau error lain, kembalikan null saja.
      return null;
    }
  }

  /// Login penuh – coba silent dulu, lalu fallback ke UI (signIn).
  Future<LoginResult> login() async {
    final AccountAuthService authService = AccountAuthManager.getService(
      _paramsHelper.createParams(),
    );

    try {
      final account = await authService.silentSignIn();
      return LoginResult(account: account, method: 'silent');
    } on PlatformException catch (e) {
      if (e.code == '2002') {
        final account = await authService.signIn();
        if (account != null) {
          return LoginResult(account: account, method: 'explicit');
        } else {
          return LoginResult(account: null, method: 'cancelled');
        }
      }
      return LoginResult(account: null, method: 'error');
    } catch (e) {
      return LoginResult(account: null, method: 'error');
    }
  }

  Future<void> logout() async {
    try {
      final AccountAuthService authService = AccountAuthManager.getService(
        _paramsHelper.createParams(),
      );
      await authService.signOut();
    } catch (e) {}
  }
}
