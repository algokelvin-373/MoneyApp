import 'package:flutter/material.dart';
import 'package:money_app/login_page.dart';
import 'package:workmanager/workmanager.dart';
import 'package:money_app/work_manager/schedule_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Inisialisasi WorkManager dengan callbackDispatcher
  await Workmanager().initialize(callbackDispatcher, isInDebugMode: true);

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
