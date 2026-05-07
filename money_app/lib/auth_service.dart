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

    // 1. Cek apakah sebelumnya sudah login
    AuthAccount? account = await authService.silentSignIn();
    if (account != null) return account;

    // 2. Tampilkan UI login (bisa throw exception jika error)
    account = await authService.signIn();
    // signIn() mengembalikan null saat user tekan back/cancel
    return account;
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
