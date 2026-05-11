// ------------------------------------------
// Halaman Utama (after login) dengan Logout
// ------------------------------------------

import 'package:flutter/material.dart';
import 'package:money_app/auth_service.dart';
import 'package:money_app/login_page.dart';

class MyHomePage extends StatefulWidget {
  final String title;
  final String? displayName;
  final String? avatarUri;
  final String? email;
  final int? gender;
  final int? carrierId;

  const MyHomePage({
    super.key,
    required this.title,
    this.displayName,
    this.avatarUri,
    this.email,
    this.gender,
    this.carrierId,
  });

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  Future<void> _logout() async {
    await AuthService().logout();
    if (!mounted) return;
    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(builder: (_) => const LoginPage(afterLogout: true)),
      (route) => false,
    );
  }

  Widget _infoRow(String label, String? value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 100,
            child: Text(
              '$label:',
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
          ),
          Expanded(child: Text(value ?? 'Tidak tersedia')),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final isAvatarAvailable =
        widget.avatarUri != null && widget.avatarUri!.isNotEmpty;

    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Column(
            children: [
              // Foto profil
              CircleAvatar(
                radius: 50,
                backgroundImage:
                    isAvatarAvailable ? NetworkImage(widget.avatarUri!) : null,
                child:
                    isAvatarAvailable
                        ? null
                        : const Icon(Icons.person, size: 50),
              ),
              const SizedBox(height: 20),
              // Sapaan
              if (widget.displayName != null)
                Text(
                  'Halo, ${widget.displayName}!',
                  style: Theme.of(context).textTheme.headlineSmall,
                ),
              const SizedBox(height: 16),
              // Kartu data profil
              Card(
                margin: const EdgeInsets.symmetric(vertical: 8),
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      _infoRow('Nama', widget.displayName),
                      _infoRow('Email', widget.email),
                      _infoRow(
                        'Jenis Kelamin',
                        widget.gender != null
                            ? (widget.gender == 1 ? 'Perempuan' : 'Laki-laki')
                            : null,
                      ),
                      _infoRow('Carrier ID', widget.carrierId?.toString()),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 24),
              // Tombol Logout
              ElevatedButton.icon(
                onPressed: _logout,
                icon: const Icon(Icons.logout),
                label: const Text('Keluar'),
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 24,
                    vertical: 12,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
