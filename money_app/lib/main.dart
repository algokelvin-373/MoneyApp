import 'package:flutter/material.dart';
import 'package:money_app/login_page.dart';
import 'auth_service.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Keuangan Pribadi',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
      ),
      home: const LoginPage(), // pengecekan dilakukan di sini
    );
  }
}

// ------------------------------------------
// Halaman Utama (after login) dengan Logout
// ------------------------------------------
class MyHomePage extends StatefulWidget {
  final String title;
  final String? displayName;
  final String? avatarUri;
  final String? email;
  final int? gender;
  final int? carrierId; // Ganti mobile dengan carrierId

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
    // Kembali ke halaman login, hapus semua history
    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(builder: (_) => const LoginPage()),
      (route) => false,
    );
  }

  Widget _buildInfoRow(String label, String? value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 80,
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
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Keluar',
            onPressed: _logout,
          ),
        ],
      ),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // Foto profil
              CircleAvatar(
                radius: 50,
                backgroundImage:
                    widget.avatarUri != null && widget.avatarUri!.isNotEmpty
                        ? NetworkImage(widget.avatarUri!)
                        : null,
                child:
                    (widget.avatarUri == null || widget.avatarUri!.isEmpty)
                        ? const Icon(Icons.person, size: 50)
                        : null,
              ),
              const SizedBox(height: 20),

              // Nama
              if (widget.displayName != null)
                Text(
                  'Halo, ${widget.displayName}!',
                  style: Theme.of(context).textTheme.headlineSmall,
                ),

              const SizedBox(height: 16),

              // Kartu data
              Card(
                margin: const EdgeInsets.symmetric(vertical: 8),
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      _buildInfoRow('Nama', widget.displayName),
                      _buildInfoRow('Email', widget.email),
                      _buildInfoRow(
                        'Jenis Kelamin',
                        widget.gender != null
                            ? (widget.gender == 1 ? 'Perempuan' : 'Laki-laki')
                            : 'Tidak tersedia',
                      ),
                      _buildInfoRow('Carrier ID', widget.carrierId?.toString()),
                    ],
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
