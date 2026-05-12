import 'package:flutter/foundation.dart';
import 'package:permission_handler/permission_handler.dart';

class PermissionService {
  static Future<bool> requestOverlayPermission() async {
    if (kIsWeb) return true;

    // Ganti Permission.overlay -> Permission.systemAlertWindow
    if (await Permission.systemAlertWindow.isGranted) return true;

    final result = await Permission.systemAlertWindow.request();

    return result.isGranted;
  }

  static Future<bool> isOverlayPermissionGranted() async {
    if (kIsWeb) return true;

    return await Permission.systemAlertWindow.isGranted;
  }
}
