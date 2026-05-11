import 'package:flutter/services.dart';
import 'package:huawei_account/huawei_account.dart';

class LoginResult {
  final AuthAccount? account;
  final String method; // 'silent', 'explicit', 'cancelled', 'error'
  final String errMessage;

  LoginResult({this.account, required this.method, this.errMessage = ''});
}

class AuthService {
  final AccountAuthParamsHelper _paramsHelper =
      AccountAuthParamsHelper()
        ..setProfile()
        ..setEmail()
        ..setCarrierId();

  /// Cek login diam‑diam – TANPA menampilkan UI.
  Future<AuthAccount?> silentLoginOnly() async {
    final AccountAuthService authService = AccountAuthManager.getService(
      _paramsHelper.createParams(),
    );
    try {
      return await authService.silentSignIn();
    } catch (_) {
      // Gagal diam‑diam → tidak perlu tindakan khusus
      return null;
    }
  }

  /// Login penuh – silent lalu fallback ke explicit (UI) jika perlu.
  Future<LoginResult> login() async {
    final AccountAuthService authService = AccountAuthManager.getService(
      _paramsHelper.createParams(),
    );

    try {
      final account = await authService.silentSignIn();
      return LoginResult(account: account, method: 'silent');
    } on PlatformException catch (_) {
      // Silent gagal → hapus sesi lama, lalu tampilkan UI izin baru
      await authService.signOut();
      try {
        final account = await authService.signIn();
        if (account != null) {
          return LoginResult(account: account, method: 'explicit');
        } else {
          return LoginResult(account: null, method: 'cancelled');
        }
      } catch (e) {
        return LoginResult(
          account: null,
          method: 'error',
          errMessage: e.toString(),
        );
      }
    } catch (e) {
      // Exception non‑Platform
      return LoginResult(
        account: null,
        method: 'error',
        errMessage: e.toString(),
      );
    }
  }

  Future<void> logout() async {
    try {
      final AccountAuthService authService = AccountAuthManager.getService(
        _paramsHelper.createParams(),
      );
      await authService.signOut();
    } catch (_) {}
  }
}
