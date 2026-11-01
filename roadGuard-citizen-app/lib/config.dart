class AppConfig {
  // Android emulator -> host machine. For a real phone, use the PC's LAN IP.
  static const apiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    // defaultValue: 'http://10.0.2.2:8080/roadguard/v1',
      defaultValue: 'http://10.109.170.55:8080/roadguard/v1'
  );
}
