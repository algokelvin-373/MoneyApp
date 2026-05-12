import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class FloatingWindowService {
  static OverlayEntry? _overlayEntry;
  static OverlayState? _overlayState;

  // Panggil method ini untuk menampilkan floating UI
  static void showFloatingUI(
    BuildContext context,
    String message,
    String imageUrl,
  ) {
    _overlayState = Overlay.of(context);
    _overlayEntry = OverlayEntry(
      builder:
          (context) => _FloatingWidget(message: message, imageUrl: imageUrl),
    );
    _overlayState?.insert(_overlayEntry!);

    // Auto-hide setelah 10 detik
    Future.delayed(Duration(seconds: 10), () {
      hideFloatingUI();
    });
  }

  static void hideFloatingUI() {
    _overlayEntry?.remove();
    _overlayEntry = null;
  }
}

class _FloatingWidget extends StatelessWidget {
  final String message;
  final String imageUrl;
  const _FloatingWidget({required this.message, required this.imageUrl});

  @override
  Widget build(BuildContext context) {
    return Positioned(
      bottom: 100,
      right: 20,
      child: Material(
        color: Colors.transparent,
        child: Container(
          padding: EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(20),
            boxShadow: [BoxShadow(blurRadius: 10)],
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Image.network(imageUrl, height: 80),
              SizedBox(height: 8),
              Text(message),
              ElevatedButton(
                onPressed: () => FloatingWindowService.hideFloatingUI(),
                child: Text('Tutup'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
