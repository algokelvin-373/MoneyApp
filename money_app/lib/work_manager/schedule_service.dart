import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:workmanager/workmanager.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

final FlutterLocalNotificationsPlugin flutterLocalNotifications =
    FlutterLocalNotificationsPlugin();

// Callback global untuk WorkManager
@pragma('vm:entry-point')
void callbackDispatcher() {
  Workmanager().executeTask((taskName, inputData) async {
    if (taskName == "showFloatingTask") {
      // Fetch data terbaru dari API (opsional, bisa pakai data tersimpan)
      await _showFloatingFromSchedule();
    }
    return Future.value(true);
  });
}

Future<void> _showFloatingFromSchedule() async {
  print("Waktu jadwal terpenuhi, aktifkan notifikasi");

  // Inisialisasi plugin notifikasi
  const AndroidInitializationSettings androidSettings =
      AndroidInitializationSettings('@mipmap/ic_launcher');
  const InitializationSettings initSettings = InitializationSettings(
    android: androidSettings,
  );
  await flutterLocalNotifications.initialize(initSettings);

  const AndroidNotificationDetails androidDetails = AndroidNotificationDetails(
    'floating_channel',
    'Floating Notification',
    importance: Importance.high,
    priority: Priority.high,
  );
  const NotificationDetails details = NotificationDetails(
    android: androidDetails,
  );
  await flutterLocalNotifications.show(
    0,
    'Pengingat',
    'Klik untuk menampilkan floating UI',
    details,
  );

  final prefs = await SharedPreferences.getInstance();
  await prefs.setBool('pending_floating', true);
}

// Fungsi untuk mengambil jadwal dari API dan mendaftarkan ke WorkManager
Future<void> fetchAndSchedule() async {
  final response = await http.get(Uri.parse('https://api.anda.com/schedule'));
  if (response.statusCode == 200) {
    final data = jsonDecode(response.body);
    String scheduleTime = data['showTime']; // misal "18:00"
    String imageUrl = data['imageUrl'];
    String message = data['message'];

    // Simpan data untuk ditampilkan nanti
    SharedPreferences prefs = await SharedPreferences.getInstance();
    await prefs.setString('floating_image_url', imageUrl);
    await prefs.setString('floating_message', message);

    // Konversi "18:00" menjadi DateTime hari ini
    List<String> parts = scheduleTime.split(':');
    int hour = int.parse(parts[0]);
    int minute = int.parse(parts[1]);

    DateTime now = DateTime.now();
    DateTime scheduledDate = DateTime(
      now.year,
      now.month,
      now.day,
      hour,
      minute,
    );
    if (scheduledDate.isBefore(now)) {
      scheduledDate = scheduledDate.add(Duration(days: 1));
    }

    // Hitung delay dalam milidetik
    int delayInMillis = scheduledDate.difference(now).inMilliseconds;

    // Jadwalkan satu kali eksekusi
    await Workmanager().registerOneOffTask(
      "floating_schedule_${scheduledDate.toIso8601String()}",
      "showFloatingTask",
      initialDelay: Duration(milliseconds: delayInMillis),
      existingWorkPolicy: ExistingWorkPolicy.replace,
      constraints: Constraints(
        networkType: NetworkType.not_required,
      ), // hapus const
    );
  }
}

// Di file schedule_service.dart, setelah fungsi fetchAndSchedule()

Future<void> testScheduleNow() async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  await prefs.setString('floating_image_url', 'https://picsum.photos/200');
  await prefs.setString(
    'floating_message',
    'Ini adalah test floating UI dari jadwal lokal',
  );

  const delay = Duration(seconds: 5);
  await Workmanager().registerOneOffTask(
    "test_floating_task",
    "showFloatingTask",
    initialDelay: delay,
    existingWorkPolicy: ExistingWorkPolicy.replace,
    constraints: Constraints(
      networkType: NetworkType.not_required,
    ), // hapus const
  );
  print("Test task dijadwalkan dalam 5 detik");
}
