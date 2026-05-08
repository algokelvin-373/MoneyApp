import 'package:flutter/services.dart';
import 'package:huawei_account/huawei_account.dart';

class AuthService {
  final AccountAuthParamsHelper _paramsHelper = AccountAuthParamsHelper();

  /// Mencoba login.
  /// - Return [AuthAccount] jika berhasil.
  /// - Return `null` jika pengguna **membatalkan** dialog login.
  /// - Throw exception jika terjadi error lain (konfigurasi, jaringan, dll).
  Future<AuthAccount?> login() async {
    final AccountAuthService authService = AccountAuthManager.getService(
      _paramsHelper.createParams(),
    );

    try {
      // 1. Coba silent sign in (jika sudah pernah izin)
      return await authService.silentSignIn();
    } on PlatformException catch (e) {
      // 2. Tangkap error jika pengguna BELUM memberikan izin
      if (e.code == '2002') {
        // ⚠️ Ini kuncinya! Tampilkan halaman login.
        return await authService.signIn();
      }

      // Error lain (misal 907135000 jika fingerprint salah)
      print('Login error (${e.code}): ${e.message}');
      return null;
    } catch (e) {
      // Exception di luar PlatformException
      print('Login error: $e');
      return null;
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
