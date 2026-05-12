import 'package:flutter/material.dart';

class FloatingWindowService {
  static OverlayEntry? _overlayEntry;
  static OverlayState? _overlayState;

  static void showFloatingUI(
    BuildContext context,
    String message,
    String imageUrl,
  ) {
    _overlayState = Overlay.of(context);
    _overlayEntry = OverlayEntry(
      builder:
          (context) => _DraggableFloatingWidget(
            message: message,
            imageUrl: imageUrl,
            onClose: () => hideFloatingUI(),
          ),
    );
    _overlayState?.insert(_overlayEntry!);
  }

  static void hideFloatingUI() {
    _overlayEntry?.remove();
    _overlayEntry = null;
  }
}

class _DraggableFloatingWidget extends StatefulWidget {
  final String message;
  final String imageUrl;
  final VoidCallback onClose;

  const _DraggableFloatingWidget({
    required this.message,
    required this.imageUrl,
    required this.onClose,
  });

  @override
  State<_DraggableFloatingWidget> createState() =>
      _DraggableFloatingWidgetState();
}

class _DraggableFloatingWidgetState extends State<_DraggableFloatingWidget> {
  Offset _position = const Offset(
    20,
    100,
  ); // default: right 20, bottom 100? ubah ke koordinat bebas
  double _screenWidth = 0;
  double _screenHeight = 0;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _screenWidth = MediaQuery.of(context).size.width;
    _screenHeight = MediaQuery.of(context).size.height;
  }

  @override
  Widget build(BuildContext context) {
    return Positioned(
      left: _position.dx,
      top: _position.dy,
      child: GestureDetector(
        onPanUpdate: (details) {
          setState(() {
            double newLeft = _position.dx + details.delta.dx;
            double newTop = _position.dy + details.delta.dy;
            // Batasi agar tidak keluar layar
            newLeft = newLeft.clamp(
              0.0,
              _screenWidth - 150,
            ); // asumsi lebar widget 150
            newTop = newTop.clamp(0.0, _screenHeight - 200);
            _position = Offset(newLeft, newTop);
          });
        },
        child: Material(
          color: Colors.transparent,
          child: Container(
            width: 150,
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16),
              boxShadow: const [BoxShadow(blurRadius: 10)],
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Image.network(widget.imageUrl, height: 60, width: 60),
                const SizedBox(height: 8),
                Text(widget.message, textAlign: TextAlign.center),
                const SizedBox(height: 8),
                ElevatedButton(
                  onPressed: widget.onClose,
                  child: const Text('Tutup'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
