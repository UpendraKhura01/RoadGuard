import 'dart:async';
import 'dart:io';
import 'dart:math' as math;
import 'dart:ui' as ui;

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:geolocator/geolocator.dart';
import 'package:image_picker/image_picker.dart';
import 'package:latlong2/latlong.dart';
import 'package:app_links/app_links.dart';
import 'package:url_launcher/url_launcher.dart';

import 'api.dart';

void main() => runApp(const RoadGuardApp());

const green = Color(0xFF11866A);
const ink = Color(0xFF173247);
const pale = Color(0xFFF5F8F8);
const _securityChannel = MethodChannel('roadguard/security');
final ValueNotifier<ThemeMode> roadGuardThemeMode =
    ValueNotifier(ThemeMode.light);

class RoadGuardMark extends StatelessWidget {
  const RoadGuardMark({super.key, this.size = 46});
  final double size;
  @override
  Widget build(BuildContext context) => Image.asset(
        'assets/images/roadguard-logo.png',
        width: size,
        height: size,
        fit: BoxFit.contain,
      );
}

class RoadGuardThemeButton extends StatelessWidget {
  const RoadGuardThemeButton({super.key});

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    return IconButton(
      tooltip: isDark ? 'Switch to light mode' : 'Switch to dark mode',
      onPressed: () {
        roadGuardThemeMode.value = isDark ? ThemeMode.light : ThemeMode.dark;
      },
      icon: Icon(isDark ? Icons.light_mode_outlined : Icons.dark_mode_outlined),
    );
  }
}

class _RoadGuardMarkPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    canvas.scale(size.width / 100, size.height / 100);
    final shield = ui.Path()
      ..moveTo(50, 4)
      ..lineTo(91, 18)
      ..lineTo(88, 54)
      ..cubicTo(86, 74, 72, 89, 50, 97)
      ..cubicTo(28, 89, 14, 74, 12, 54)
      ..lineTo(9, 18)
      ..close();
    canvas.drawShadow(shield, Colors.black26, 2, true);
    canvas.drawPath(shield, Paint()..color = const Color(0xFF153F73));
    canvas.drawPath(
      shield,
      Paint()
        ..color = const Color(0xFFFFC247)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 3,
    );
    canvas.save();
    canvas.clipPath(shield);
    final road = ui.Path()
      ..moveTo(24, 78)
      ..cubicTo(37, 63, 36, 51, 46, 38)
      ..cubicTo(54, 27, 65, 18, 80, 13);
    final goldEdge = Paint()
      ..color = const Color(0xFFFFC247)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 20
      ..strokeCap = StrokeCap.butt
      ..strokeJoin = StrokeJoin.round;
    canvas.drawPath(road, goldEdge);
    canvas.drawPath(
      road,
      Paint()
        ..color = const Color(0xFFF7F8FA)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 12
        ..strokeCap = StrokeCap.butt
        ..strokeJoin = StrokeJoin.round,
    );
    final metrics = road.computeMetrics().first;
    final dashPaint = Paint()
      ..color = const Color(0xFF153F73)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3
      ..strokeCap = StrokeCap.round;
    for (var start = 5.0; start < metrics.length - 3; start += 13) {
      canvas.drawPath(
          metrics.extractPath(start, math.min(start + 5, metrics.length)),
          dashPaint);
    }
    canvas.restore();
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class RoadHeroBanner extends StatelessWidget {
  const RoadHeroBanner({
    super.key,
    required this.title,
    required this.subtitle,
    this.height = 155,
  });
  final String title;
  final String subtitle;
  final double height;
  @override
  Widget build(BuildContext context) => ClipRRect(
        borderRadius: BorderRadius.circular(20),
        child: CustomPaint(
          painter: _RoadScenePainter(),
          child: SizedBox(
            height: height,
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                      shadows: [Shadow(color: Colors.black54, blurRadius: 8)],
                    ),
                  ),
                  const SizedBox(height: 3),
                  Text(
                    subtitle,
                    style: const TextStyle(
                      color: Colors.white,
                      shadows: [Shadow(color: Colors.black54, blurRadius: 8)],
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      );
}

class _RoadScenePainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final w = size.width, h = size.height;
    canvas.drawRect(
      Offset.zero & size,
      Paint()
        ..shader = const LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [Color(0xFF8CD2E8), Color(0xFFCEE9E8)],
        ).createShader(Offset.zero & size),
    );
    final sun = Paint()..color = const Color(0xFFFFE7A8).withValues(alpha: .8);
    canvas.drawCircle(Offset(w * .82, h * .25), h * .11, sun);
    final skyline = Paint()
      ..color = const Color(0xFF5C8D91).withValues(alpha: .82);
    final buildings = <Rect>[
      Rect.fromLTWH(w * .04, h * .35, w * .09, h * .28),
      Rect.fromLTWH(w * .15, h * .27, w * .07, h * .36),
      Rect.fromLTWH(w * .24, h * .39, w * .10, h * .24),
      Rect.fromLTWH(w * .67, h * .33, w * .08, h * .30),
      Rect.fromLTWH(w * .77, h * .24, w * .09, h * .39),
      Rect.fromLTWH(w * .88, h * .36, w * .08, h * .27),
    ];
    for (final rect in buildings) {
      canvas.drawRRect(
        RRect.fromRectAndRadius(rect, const Radius.circular(3)),
        skyline,
      );
      final window = Paint()
        ..color = const Color(0xFFD8F1E9).withValues(alpha: .72);
      for (var row = 0; row < 3; row++) {
        canvas.drawRect(
          Rect.fromLTWH(rect.left + 5, rect.top + 8 + row * 10, 3, 4),
          window,
        );
        canvas.drawRect(
          Rect.fromLTWH(
            rect.left + rect.width - 9,
            rect.top + 8 + row * 10,
            3,
            4,
          ),
          window,
        );
      }
    }
    final trees = Paint()..color = const Color(0xFF1D8B70);
    for (final x in [w * .02, w * .34, w * .62, w * .96]) {
      canvas.drawCircle(Offset(x, h * .61), h * .07, trees);
      canvas.drawRect(
        Rect.fromLTWH(x - 2, h * .61, 4, h * .09),
        Paint()..color = const Color(0xFF695B49),
      );
    }
    final road = ui.Path()
      ..moveTo(w * .43, h * .55)
      ..lineTo(w * .57, h * .55)
      ..lineTo(w, h)
      ..lineTo(0, h)
      ..close();
    canvas.drawPath(
      road,
      Paint()
        ..shader = const LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [Color(0xFF36566A), Color(0xFF132F45)],
        ).createShader(Offset.zero & size),
    );
    final lane = Paint()
      ..color = Colors.white.withValues(alpha: .82)
      ..strokeWidth = 3
      ..strokeCap = StrokeCap.round;
    canvas.drawLine(Offset(w * .5, h * .60), Offset(w * .5, h * .68), lane);
    canvas.drawLine(Offset(w * .5, h * .73), Offset(w * .5, h * .83), lane);
    canvas.drawLine(Offset(w * .5, h * .89), Offset(w * .5, h), lane);
    final shade = Paint()
      ..shader = LinearGradient(
        begin: Alignment.topCenter,
        end: Alignment.bottomCenter,
        colors: [Colors.transparent, ink.withValues(alpha: .82)],
      ).createShader(Offset.zero & size);
    canvas.drawRect(Offset.zero & size, shade);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class RoadGuardApp extends StatefulWidget {
  const RoadGuardApp({super.key});

  @override
  State<RoadGuardApp> createState() => _RoadGuardAppState();
}

class _RoadGuardAppState extends State<RoadGuardApp>
    with WidgetsBindingObserver {
  bool _checking = true;
  bool _locationEnabled = false;
  bool _permissionGranted = false;
  bool? _vpnActive;
  Timer? _timer;
  bool _checkRunning = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    WidgetsBinding.instance.addPostFrameCallback(
      (_) => _checkAccess(requestPermission: true),
    );
    _timer = Timer.periodic(const Duration(seconds: 5), (_) {
      if (WidgetsBinding.instance.lifecycleState == AppLifecycleState.resumed) {
        _checkAccess();
      }
    });
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _timer?.cancel();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed)
      _checkAccess(requestPermission: true);
  }

  Future<void> _checkAccess({bool requestPermission = false}) async {
    if (_checkRunning) return;
    _checkRunning = true;
    if (mounted && !_checking) setState(() => _checking = true);
    var serviceEnabled = false;
    var permissionGranted = false;
    bool? vpn;
    try {
      serviceEnabled = await Geolocator.isLocationServiceEnabled();
      var permission = await Geolocator.checkPermission();
      if (serviceEnabled &&
          permission == LocationPermission.denied &&
          requestPermission) {
        permission = await Geolocator.requestPermission();
      }
      permissionGranted = serviceEnabled &&
          (permission == LocationPermission.whileInUse ||
              permission == LocationPermission.always);
    } catch (_) {
      serviceEnabled = false;
    }

    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) {
      // Browsers and other platforms do not expose a reliable VPN state to Flutter.
      vpn = false;
    } else {
      try {
        vpn = await _securityChannel.invokeMethod<bool>('isVpnActive');
      } on PlatformException {
        vpn = null;
      } on MissingPluginException {
        vpn = null;
      }
    }

    if (mounted) {
      setState(() {
        _locationEnabled = serviceEnabled;
        _permissionGranted = permissionGranted;
        _vpnActive = vpn;
        _checking = false;
      });
    }
    _checkRunning = false;
  }

  bool get _locationAllowed => _locationEnabled && _permissionGranted;
  Future<void> _openVpnSettings() async {
    try {
      await _securityChannel.invokeMethod<void>('openVpnSettings');
    } on PlatformException {
      await Geolocator.openAppSettings();
    } on MissingPluginException {
      await Geolocator.openAppSettings();
    }
  }

  @override
  Widget build(BuildContext context) => ValueListenableBuilder<ThemeMode>(
      valueListenable: roadGuardThemeMode,
      builder: (context, mode, _) => MaterialApp(
            title: 'RoadGuard',
            debugShowCheckedModeBanner: false,
            themeMode: mode,
            theme: ThemeData(
              colorScheme: ColorScheme.fromSeed(seedColor: green),
              scaffoldBackgroundColor: pale,
              useMaterial3: true,
              appBarTheme: const AppBarTheme(
                backgroundColor: pale,
                foregroundColor: ink,
              ),
              cardTheme: const CardThemeData(
                color: Colors.white,
                elevation: 1,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.all(Radius.circular(18)),
                ),
                margin: EdgeInsets.only(bottom: 10),
              ),
              filledButtonTheme: FilledButtonThemeData(
                style: FilledButton.styleFrom(
                  minimumSize: const Size.fromHeight(48),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                ),
              ),
              inputDecorationTheme: InputDecorationTheme(
                filled: true,
                fillColor: Colors.white,
                border:
                    OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(14),
                  borderSide: const BorderSide(color: Color(0xFFDCE7E7)),
                ),
              ),
            ),
            darkTheme: ThemeData(
              colorScheme: ColorScheme.fromSeed(
                seedColor: green,
                brightness: Brightness.dark,
              ),
              scaffoldBackgroundColor: const Color(0xFF101A22),
              useMaterial3: true,
              appBarTheme: const AppBarTheme(
                backgroundColor: Color(0xFF101A22),
                foregroundColor: Color(0xFFEAF2F5),
              ),
              cardTheme: const CardThemeData(
                color: Color(0xFF1A2934),
                elevation: 0,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.all(Radius.circular(18)),
                ),
                margin: EdgeInsets.only(bottom: 10),
              ),
              filledButtonTheme: FilledButtonThemeData(
                style: FilledButton.styleFrom(
                  minimumSize: const Size.fromHeight(48),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                ),
              ),
              inputDecorationTheme: InputDecorationTheme(
                filled: true,
                fillColor: const Color(0xFF1A2934),
                border:
                    OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
              ),
            ),
            home: Stack(
              children: [
                const AuthPage(),
                if (!_locationAllowed)
                  _AccessGate(
                    checking: _checking,
                    locationEnabled: _locationEnabled,
                    permissionGranted: _permissionGranted,
                    refresh: () => _checkAccess(requestPermission: true),
                    openLocationSettings: Geolocator.openLocationSettings,
                    openAppSettings: Geolocator.openAppSettings,
                  ),
                if (_locationAllowed &&
                    (_vpnActive == true || (_vpnActive == null && !_checking)))
                  _VpnBlocker(
                    checking: _checking,
                    detected: _vpnActive == true,
                    refresh: () => _checkAccess(requestPermission: true),
                    openSettings: _openVpnSettings,
                  ),
              ],
            ),
          ));
}

class _AccessGate extends StatelessWidget {
  const _AccessGate({
    required this.checking,
    required this.locationEnabled,
    required this.permissionGranted,
    required this.refresh,
    required this.openLocationSettings,
    required this.openAppSettings,
  });
  final bool checking;
  final bool locationEnabled;
  final bool permissionGranted;
  final Future<void> Function() refresh;
  final Future<bool> Function() openLocationSettings;
  final Future<bool> Function() openAppSettings;

  @override
  Widget build(BuildContext context) {
    final locationOkay = locationEnabled && permissionGranted;
    return Positioned.fill(
      child: Material(
        color: pale,
        child: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(26),
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 440),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const RoadGuardMark(size: 68),
                    const SizedBox(height: 12),
                    const Text(
                      'RoadGuard',
                      style: TextStyle(
                        fontSize: 25,
                        fontWeight: FontWeight.bold,
                        color: ink,
                      ),
                    ),
                    const SizedBox(height: 8),
                    const Text(
                      'Turn on location to see hazards near you and submit reports.',
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 22),
                    _GateStatus(
                      ok: locationOkay,
                      title: 'Device location',
                      detail: !locationEnabled
                          ? 'Location services are turned off.'
                          : permissionGranted
                              ? 'Location permission is available.'
                              : 'RoadGuard needs location permission.',
                    ),
                    if (!locationEnabled) ...[
                      const SizedBox(height: 8),
                      FilledButton.icon(
                        onPressed: openLocationSettings,
                        icon: const Icon(Icons.location_on),
                        label: const Text('Turn on location in Settings'),
                      ),
                    ] else if (!permissionGranted) ...[
                      const SizedBox(height: 8),
                      FilledButton.icon(
                        onPressed: openAppSettings,
                        icon: const Icon(Icons.settings),
                        label: const Text('Allow location in App settings'),
                      ),
                    ],
                    const SizedBox(height: 14),
                    if (checking)
                      const Padding(
                        padding: EdgeInsets.all(10),
                        child: CircularProgressIndicator(),
                      )
                    else
                      TextButton.icon(
                        onPressed: refresh,
                        icon: const Icon(Icons.refresh),
                        label: const Text('Check again'),
                      ),
                    if (kIsWeb)
                      const Padding(
                        padding: EdgeInsets.only(top: 10),
                        child: Text(
                          'Location access is required to use RoadGuard.',
                          textAlign: TextAlign.center,
                          style: TextStyle(color: Colors.black54, fontSize: 12),
                        ),
                      ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _VpnBlocker extends StatelessWidget {
  const _VpnBlocker({
    required this.checking,
    required this.detected,
    required this.refresh,
    required this.openSettings,
  });
  final bool checking;
  final bool detected;
  final Future<void> Function() refresh;
  final Future<void> Function() openSettings;

  @override
  Widget build(BuildContext context) => Positioned.fill(
        child: Material(
          color: Colors.black.withValues(alpha: 0.42),
          child: SafeArea(
            child: Align(
              alignment: Alignment.topCenter,
              child: Container(
                margin: const EdgeInsets.fromLTRB(14, 14, 14, 0),
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: const Color(0xFF202124),
                  borderRadius: BorderRadius.circular(20),
                  boxShadow: const [
                    BoxShadow(
                      color: Colors.black38,
                      blurRadius: 24,
                      offset: Offset(0, 10),
                    ),
                  ],
                ),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const RoadGuardMark(size: 42),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            detected
                                ? 'Disconnect your VPN'
                                : 'Connection check unavailable',
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 17,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            detected
                                ? 'RoadGuard detected an active VPN. Turn it off to continue using the app.'
                                : 'RoadGuard could not verify your connection. Retry the check to continue.',
                            style: const TextStyle(
                              color: Colors.white70,
                              height: 1.35,
                            ),
                          ),
                          const SizedBox(height: 12),
                          Wrap(
                            spacing: 8,
                            runSpacing: 6,
                            children: [
                              if (detected)
                                OutlinedButton(
                                  onPressed: openSettings,
                                  style: OutlinedButton.styleFrom(
                                    foregroundColor: Colors.white,
                                    side:
                                        const BorderSide(color: Colors.white70),
                                  ),
                                  child: const Text('VPN settings'),
                                ),
                              FilledButton(
                                onPressed: checking ? null : refresh,
                                style: FilledButton.styleFrom(
                                  backgroundColor: Colors.white,
                                  foregroundColor: const Color(0xFF202124),
                                ),
                                child: Text(
                                  checking ? 'Checking…' : 'I turned it off',
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      );
}

class _GateStatus extends StatelessWidget {
  const _GateStatus({
    required this.ok,
    required this.title,
    required this.detail,
  });
  final bool ok;
  final String title;
  final String detail;
  @override
  Widget build(BuildContext context) => Card(
        child: ListTile(
          leading: Icon(
            ok ? Icons.check_circle : Icons.error_outline,
            color: ok ? green : Colors.deepOrange,
          ),
          title:
              Text(title, style: const TextStyle(fontWeight: FontWeight.w700)),
          subtitle: Text(detail),
        ),
      );
}

class _IndiaCountryCodeBadge extends StatelessWidget {
  const _IndiaCountryCodeBadge();

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(left: 12, right: 8),
        child: Center(
          widthFactor: 1,
          child: DecoratedBox(
            decoration: BoxDecoration(
              color: const Color(0xFFEAF5F1),
              borderRadius: BorderRadius.circular(6),
            ),
            child: const Padding(
              padding: EdgeInsets.symmetric(horizontal: 8, vertical: 5),
              child: Text(
                '+91',
                style: TextStyle(
                  color: green,
                  fontSize: 12,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
          ),
        ),
      );
}

class AuthPage extends StatefulWidget {
  const AuthPage({super.key});
  @override
  State<AuthPage> createState() => _AuthPageState();
}

class _AuthPageState extends State<AuthPage> {
  final api = RoadGuardApi();
  late final AppLinks appLinks;
  StreamSubscription<Uri>? appLinkSubscription;
  bool handlingOAuthCallback = false;
  bool restoringSession = true;
  final phone = TextEditingController();
  final password = TextEditingController();
  final otp = TextEditingController();
  final name = TextEditingController();
  final email = TextEditingController();
  bool otpSent = false;
  bool usePassword = false;
  bool signup = false;
  bool busy = false;
  bool started = false;
  String? demoCode;

  @override
  void initState() {
    super.initState();
    appLinks = AppLinks();
    appLinkSubscription = appLinks.uriLinkStream.listen(
      _handleOAuthCallback,
      onError: (Object error) {
        if (mounted) _message('Could not receive the Google sign-in callback.');
      },
    );
    unawaited(_restoreSession());
  }

  Future<void> _restoreSession() async {
    try {
      await api.restoreToken();
      if (api.token == null || api.token!.isEmpty) return;
      try {
        final profile = await api.profile();
        final role = (profile['roles'] ?? profile['role'] ?? '')
            .toString()
            .toUpperCase();
        if (role.isNotEmpty && role != 'CITIZEN') {
          await api.clearToken();
          return;
        }
        _openHome((profile['name'] ?? 'Citizen').toString());
      } catch (error) {
        final message = error.toString().toLowerCase();
        if (message.contains('sign-in failed') ||
            message.contains('not allowed to use the citizen app')) {
          await api.clearToken();
          return;
        }
        // Keep a still-stored session during temporary network/API failures.
        _openHome('Citizen');
      }
    } catch (_) {
      // If secure storage cannot be read, leave the user at the sign-in screen.
    } finally {
      if (mounted) setState(() => restoringSession = false);
    }
  }

  void _openHome(String displayName) {
    if (!mounted) return;
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(
        builder: (_) => HomePage(api: api, username: displayName),
      ),
    );
  }

  Future<void> _handleOAuthCallback(Uri uri) async {
    if (uri.scheme != 'roadguard' ||
        uri.host != 'oauth2' ||
        uri.path != '/callback') return;
    final code = uri.queryParameters['code'];
    if (code == null || code.isEmpty || handlingOAuthCallback) return;
    handlingOAuthCallback = true;
    try {
      await _act(() async => _enter(await api.exchangeOAuthCode(code)));
    } finally {
      handlingOAuthCallback = false;
    }
  }

  Future<void> _startGoogleLogin() async {
    final launched = await launchUrl(
      api.mobileOAuthStartUri,
      mode: LaunchMode.inAppBrowserView,
    );
    if (!launched)
      throw Exception('Could not open the browser for Google sign-in.');
  }

  Future<void> _act(Future<void> Function() work) async {
    setState(() => busy = true);
    try {
      await work();
    } catch (e) {
      if (mounted) _message(e.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => busy = false);
    }
  }

  bool _isStrongPassword(String value) =>
      value.length >= 8 &&
      RegExp(r'[a-z]').hasMatch(value) &&
      RegExp(r'[A-Z]').hasMatch(value) &&
      RegExp(r'\d').hasMatch(value) &&
      RegExp(r'[^A-Za-z\d\s]').hasMatch(value) &&
      !RegExp(r'\s').hasMatch(value);

  Future<void> _sendOtp() async {
    if (!RegExp(r'^[6-9][0-9]{9}$').hasMatch(phone.text.trim()))
      throw Exception('Enter a valid 10-digit Indian mobile number.');
    final result = await api.sendOtp(phone.text.trim());
    setState(() {
      otpSent = true;
      demoCode = _otpFrom(result);
    });
    _message('OTP sent. Enter the code to continue.');
  }

  String? _otpFrom(Map<String, dynamic> result) {
    final value = (result['otp'] ??
            result['demoOtp'] ??
            result['code'] ??
            result['message'] ??
            '')
        .toString();
    return RegExp(r'\b\d{6}\b').firstMatch(value)?.group(0);
  }

  Future<void> _enter(Map<String, dynamic> result) async {
    if (result['token'] == null)
      throw Exception('The backend response did not include a token.');
    if ((result['role'] ?? '').toString().toUpperCase() != 'CITIZEN') {
      await api.clearToken();
      throw Exception(
        'This account is for the admin website. Sign in with a citizen account here.',
      );
    }
    try {
      final position = await Geolocator.getCurrentPosition();
      await api.updateLocation(position.latitude, position.longitude);
    } catch (_) {
      // Login can proceed; the map and profile offer a location refresh action.
    }
    if (!mounted) return;
    final displayName = (result['name'] ??
            result['username'] ??
            (name.text.trim().isNotEmpty ? name.text.trim() : 'Citizen'))
        .toString();
    _openHome(displayName);
  }

  void _message(String value) => ScaffoldMessenger.of(context)
      .showSnackBar(SnackBar(content: Text(value)));

  @override
  void dispose() {
    appLinkSubscription?.cancel();
    phone.dispose();
    password.dispose();
    otp.dispose();
    name.dispose();
    email.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (restoringSession) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }
    if (!started) {
      return _RoadGuardWelcomePage(
        onGetStarted: () => setState(() => started = true),
      );
    }
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          tooltip: 'Back to welcome page',
          onPressed: () => setState(() => started = false),
          icon: const Icon(Icons.arrow_back),
        ),
        actions: [_RenderHealthButton(api: api), const RoadGuardThemeButton()],
      ),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 430),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const RoadGuardMark(size: 62),
                  const SizedBox(height: 10),
                  Text(
                    'RoadGuard',
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                          fontSize: 30,
                          fontWeight: FontWeight.bold,
                          color: Theme.of(context).colorScheme.onSurface,
                        ),
                  ),
                  const Text(
                    'Safer roads start with you',
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 18),
                  Text(
                    'Safer roads start with you',
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          color: Theme.of(context).colorScheme.onSurfaceVariant,
                        ),
                  ),
                  const SizedBox(height: 28),
                  Text(
                    signup ? 'Create your account' : 'Welcome back',
                    style: const TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 14),
                  if (signup) ...[
                    _field(name, 'Full name'),
                    _field(
                      email,
                      'Gmail or email (optional)',
                      keyboard: TextInputType.emailAddress,
                    ),
                  ],
                  _field(
                    phone,
                    'Phone number',
                    keyboard: TextInputType.phone,
                    indianPhone: true,
                    onChanged: (_) {
                      if (otpSent) {
                        setState(() {
                          otpSent = false;
                          otp.clear();
                          demoCode = null;
                        });
                      }
                    },
                  ),
                  if (usePassword || signup) ...[
                    _field(
                      password,
                      'Password',
                      obscure: true,
                      onChanged: signup ? (_) => setState(() {}) : null,
                    ),
                    if (signup)
                      Padding(
                        padding: const EdgeInsets.only(left: 4, bottom: 12),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            _passwordRule(
                                password.text.length >= 8, 'At least 8 characters'),
                            _passwordRule(RegExp(r'[A-Z]').hasMatch(password.text),
                                'One uppercase letter'),
                            _passwordRule(RegExp(r'[a-z]').hasMatch(password.text),
                                'One lowercase letter'),
                            _passwordRule(RegExp(r'\d').hasMatch(password.text),
                                'One number'),
                            _passwordRule(
                                RegExp(r'[^A-Za-z\d\s]').hasMatch(password.text),
                                'One special character'),
                            _passwordRule(!RegExp(r'\s').hasMatch(password.text),
                                'No spaces'),
                          ],
                        ),
                      ),
                  ],
                  if (!usePassword || signup) ...[
                    if (otpSent)
                      _field(otp, 'OTP code', keyboard: TextInputType.number),
                    if (demoCode != null)
                      Padding(
                        padding: const EdgeInsets.only(bottom: 10),
                        child: Text(
                          'Demo OTP: $demoCode',
                          textAlign: TextAlign.center,
                          style: const TextStyle(
                            color: green,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    if (!otpSent)
                      OutlinedButton.icon(
                        onPressed: busy ? null : () => _act(_sendOtp),
                        icon: const Icon(Icons.sms_outlined),
                        label: const Text('Send OTP'),
                      ),
                  ],
                  const SizedBox(height: 8),
                  FilledButton(
                    onPressed: busy
                        ? null
                        : () => _act(() async {
                              if (signup) {
                                if (!_isStrongPassword(password.text)) {
                                  throw Exception(
                                      'Password does not meet the requirements shown above.');
                                }
                                if (!otpSent) {
                                  await _sendOtp();
                                  return;
                                }
                                await _enter(
                                  await api.signup(
                                    name.text.trim(),
                                    phone.text.trim(),
                                    password.text,
                                    otp.text.trim(),
                                    email: email.text.trim(),
                                  ),
                                );
                              } else if (usePassword) {
                                if (!RegExp(r'^[6-9][0-9]{9}$')
                                    .hasMatch(phone.text.trim())) {
                                  throw Exception(
                                      'Enter a valid 10-digit Indian mobile number.');
                                }
                                await _enter(
                                  await api.loginPassword(
                                    phone.text.trim(),
                                    password.text,
                                  ),
                                );
                              } else if (otpSent) {
                                await _enter(
                                  await api.loginOtp(
                                    phone.text.trim(),
                                    otp.text.trim(),
                                  ),
                                );
                              } else {
                                await _sendOtp();
                              }
                            }),
                    child: Text(
                      busy
                          ? 'Please wait…'
                          : signup
                              ? (otpSent
                                  ? 'Create account'
                                  : 'Send OTP to sign up')
                              : usePassword
                                  ? 'Sign in'
                                  : otpSent
                                      ? 'Verify OTP'
                                      : 'Send OTP',
                    ),
                  ),
                  if (!signup) ...[
                    const SizedBox(height: 10),
                    OutlinedButton.icon(
                      onPressed: busy ? null : () => _act(_startGoogleLogin),
                      icon: const Icon(Icons.account_circle_outlined),
                      label: const Text('Continue with Google'),
                    ),
                  ],
                  TextButton(
                    onPressed: () => setState(() {
                      signup = !signup;
                      otpSent = false;
                      usePassword = false;
                      demoCode = null;
                    }),
                    child: Text(
                      signup
                          ? 'Already registered? Sign in'
                          : 'New to RoadGuard? Create account',
                    ),
                  ),
                  if (!signup)
                    TextButton(
                      onPressed: () => setState(() {
                        usePassword = !usePassword;
                        otpSent = false;
                        demoCode = null;
                      }),
                      child: Text(
                        usePassword
                            ? 'Use phone OTP instead'
                            : 'Use phone and password',
                      ),
                    ),
                  if (!signup && usePassword)
                    const Padding(
                      padding: EdgeInsets.only(top: 6),
                      child: Text(
                        'Sign in with the phone number and RoadGuard password registered to your account.',
                        textAlign: TextAlign.center,
                        style: TextStyle(fontSize: 12, color: Colors.black54),
                      ),
                    ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _passwordRule(bool satisfied, String label) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 2),
        child: Row(
          children: [
            Icon(
              satisfied ? Icons.check_circle : Icons.circle_outlined,
              size: 15,
              color: satisfied ? green : Colors.black54,
            ),
            const SizedBox(width: 7),
            Text(
              label,
              style: TextStyle(
                fontSize: 12,
                color: satisfied ? green : Colors.black54,
              ),
            ),
          ],
        ),
      );

  Widget _field(
    TextEditingController controller,
    String label, {
    bool obscure = false,
    TextInputType? keyboard,
    bool indianPhone = false,
    ValueChanged<String>? onChanged,
  }) =>
      Padding(
        padding: const EdgeInsets.only(bottom: 12),
        child: TextField(
          controller: controller,
          obscureText: obscure,
          keyboardType: keyboard,
          onChanged: onChanged,
          inputFormatters:
              indianPhone ? [FilteringTextInputFormatter.digitsOnly] : null,
          maxLength: indianPhone ? 10 : null,
          decoration: InputDecoration(
            prefixIcon: indianPhone ? const _IndiaCountryCodeBadge() : null,
            prefixIconConstraints:
                const BoxConstraints(minWidth: 60, minHeight: 0),
            labelText: label,
            border: const OutlineInputBorder(),
            filled: true,
            fillColor: Colors.white,
          ),
        ),
      );
}

class _RoadGuardWelcomePage extends StatelessWidget {
  const _RoadGuardWelcomePage({required this.onGetStarted});
  final VoidCallback onGetStarted;

  @override
  Widget build(BuildContext context) => Scaffold(
        body: DecoratedBox(
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [Color(0xFF174A89), Color(0xFF0D3470), Color(0xFF082653)],
            ),
          ),
          child: SafeArea(
            child: Stack(
              children: [
                const Positioned(
                    top: 8, right: 8, child: RoadGuardThemeButton()),
                Center(
                  child: SingleChildScrollView(
                    padding: const EdgeInsets.fromLTRB(26, 48, 26, 28),
                    child: ConstrainedBox(
                      constraints: const BoxConstraints(maxWidth: 440),
                      child: Column(
                        children: [
                          const RoadGuardMark(size: 126),
                          const SizedBox(height: 18),
                          const Text(
                            'ROADGUARD',
                            style: TextStyle(
                              color: Colors.white,
                              fontSize: 30,
                              fontWeight: FontWeight.w900,
                              letterSpacing: 1.2,
                            ),
                          ),
                          const SizedBox(height: 5),
                          const Text(
                            'Smart. Safe. Secure Roads.',
                            style: TextStyle(
                              color: Color(0xFFE3EDF8),
                              fontSize: 15,
                              letterSpacing: .3,
                            ),
                          ),
                          const SizedBox(height: 38),
                          SizedBox(
                            width: double.infinity,
                            height: 54,
                            child: FilledButton.icon(
                              onPressed: onGetStarted,
                              style: FilledButton.styleFrom(
                                backgroundColor: const Color(0xFFFFC247),
                                foregroundColor: const Color(0xFF153F73),
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(16),
                                ),
                              ),
                              icon: const Icon(Icons.arrow_forward_rounded),
                              label: const Text(
                                'Get started',
                                style: TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.w800,
                                ),
                              ),
                            ),
                          ),
                          const SizedBox(height: 15),
                          const Text(
                            'Report. Track. Make a difference.',
                            style: TextStyle(
                                color: Color(0xFFCBD9E8), fontSize: 12),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      );
}

class _WelcomeRoadArtwork extends StatelessWidget {
  const _WelcomeRoadArtwork();
  @override
  Widget build(BuildContext context) =>
      CustomPaint(painter: _WelcomeRoadPainter());
}

class _WelcomeRoadPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final horizon = size.height * .68;
    final skyline = Paint()..color = Colors.white.withValues(alpha: .055);
    for (var i = 0; i < 9; i++) {
      final x = size.width * (i / 9);
      final h = size.height * (.09 + (i % 3) * .035);
      canvas.drawRect(
          Rect.fromLTWH(x, horizon - h, size.width / 12, h), skyline);
    }
    final road = ui.Path()
      ..moveTo(size.width * .46, horizon)
      ..cubicTo(size.width * .43, horizon + 36, size.width * .16,
          size.height * .82, size.width * .02, size.height)
      ..lineTo(size.width * .98, size.height)
      ..cubicTo(size.width * .78, size.height * .84, size.width * .57,
          horizon + 36, size.width * .54, horizon)
      ..close();
    canvas.drawPath(road, Paint()..color = Colors.black.withValues(alpha: .14));
    final lane = Paint()
      ..color = const Color(0xFFFFC247).withValues(alpha: .72)
      ..strokeWidth = 3
      ..strokeCap = StrokeCap.round;
    canvas.drawLine(Offset(size.width * .5, horizon + 12),
        Offset(size.width * .5, horizon + 25), lane);
    canvas.drawLine(Offset(size.width * .5, horizon + 38),
        Offset(size.width * .5, horizon + 58), lane);
    canvas.drawLine(Offset(size.width * .5, horizon + 77),
        Offset(size.width * .5, horizon + 110), lane);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}


class _RenderHealthButton extends StatefulWidget {
  const _RenderHealthButton({required this.api});
  final RoadGuardApi api;
  @override
  State<_RenderHealthButton> createState() => _RenderHealthButtonState();
}

class _RenderHealthButtonState extends State<_RenderHealthButton> {
  bool _checking = false;
  _RenderHealthState _status = _RenderHealthState.checking;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _check());
  }

  Future<void> _check() async {
    if (_checking) return;
    setState(() { _checking = true; _status = _RenderHealthState.checking; });
    for (var attempt = 0; attempt < 5; attempt++) {
      if (attempt > 0) {
        if (mounted) setState(() => _status = _RenderHealthState.waking);
        await Future<void>.delayed(const Duration(seconds: 8));
      }
      if (!mounted) return;
      if (await widget.api.checkServiceHealth()) {
        if (mounted) setState(() { _status = _RenderHealthState.online; _checking = false; });
        return;
      }
    }
    if (mounted) setState(() { _status = _RenderHealthState.unavailable; _checking = false; });
  }

  @override
  Widget build(BuildContext context) {
    final color = switch (_status) {
      _RenderHealthState.online => Colors.green,
      _RenderHealthState.waking || _RenderHealthState.checking => Colors.orange,
      _RenderHealthState.unavailable => Colors.red,
    };
    final tooltip = switch (_status) {
      _RenderHealthState.online => 'Render is online. Tap to check again.',
      _RenderHealthState.waking => 'Render may be waking up. Checking again shortly.',
      _RenderHealthState.checking => 'Checking Render service status.',
      _RenderHealthState.unavailable => 'Could not reach Render; it may be asleep or unavailable. Tap to retry.',
    };
    return IconButton(
      tooltip: tooltip,
      visualDensity: VisualDensity.compact,
      padding: EdgeInsets.zero,
      constraints: const BoxConstraints.tightFor(width: 36, height: 36),
      onPressed: _checking ? null : _check,
      icon: Container(
        width: 10,
        height: 10,
        decoration: BoxDecoration(
          color: color,
          shape: BoxShape.circle,
          border: Border.all(color: Theme.of(context).colorScheme.surface, width: 1),
        ),
      ),
    );
  }
}

enum _RenderHealthState { checking, waking, online, unavailable }

class HomePage extends StatefulWidget {
  const HomePage({super.key, required this.api, required this.username});
  final RoadGuardApi api;
  final String username;
  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  void _message(String value) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(value)),
    );
  }

  int tab = 0;
  bool loading = false;
  List<dynamic> mine = [];
  List<dynamic> nearby = [];
  List<dynamic> notifications = [];
  Map<String, dynamic> profile = {};
  String? loadError;
  String? currentCoordinates;
  LatLng? currentLocation;

  @override
  void initState() {
    super.initState();
    loadAll();
  }

  Future<void> loadAll() async {
    setState(() {
      loading = true;
      loadError = null;
    });
    Position? currentPosition;
    try {
      currentPosition = await Geolocator.getCurrentPosition();
      currentLocation =
          LatLng(currentPosition.latitude, currentPosition.longitude);
      currentCoordinates =
          '${currentPosition.latitude.toStringAsFixed(5)}, ${currentPosition.longitude.toStringAsFixed(5)}';
    } catch (_) {
      currentCoordinates = null;
      currentLocation = null;
    }
    try {
      if (currentPosition != null) {
        try {
          await widget.api.updateLocation(
            currentPosition.latitude,
            currentPosition.longitude,
          );
        } catch (_) {
          // Continue loading the profile and own reports if reverse geocoding is temporarily unavailable.
        }
      }
      final results = await Future.wait<Object?>([
        widget.api.reports('/citizen/myReports').catchError((_) => <dynamic>[]),
        widget.api
            .reports('/citizen/nearbyReports')
            .catchError((_) => <dynamic>[]),
        widget.api.notifications().catchError((_) => <dynamic>[]),
        widget.api.profile().catchError((_) => <String, dynamic>{}),
      ]);
      if (!mounted) return;
      setState(() {
        mine = results[0] as List<dynamic>;
        nearby = (results[1] as List<dynamic>).where(_isPublicReport).toList();
        notifications = results[2] as List<dynamic>;
        profile = results[3] as Map<String, dynamic>;
      });
    } catch (e) {
      if (mounted)
        setState(
          () => loadError = e.toString().replaceFirst('Exception: ', ''),
        );
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }

  bool _isPublicReport(dynamic raw) {
    if (raw is! Map) return false;
    final status = (raw['reportStatus'] ?? '').toString().toUpperCase();
    return status != 'REJECTED' &&
        status != 'AI_REJECTED' &&
        status != 'RESOLVED';
  }

  Future<void> _centerMapOnCurrentLocation() async {
    try {
      if (!await Geolocator.isLocationServiceEnabled()) {
        _message('Turn on device location to center the map.');
        await Geolocator.openLocationSettings();
        return;
      }

      var permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
      }
      if (permission == LocationPermission.denied ||
          permission == LocationPermission.deniedForever) {
        _message('Allow location permission to center the map.');
        await Geolocator.openAppSettings();
        return;
      }

      final position = await Geolocator.getCurrentPosition(
        locationSettings: const LocationSettings(
          accuracy: LocationAccuracy.high,
        ),
      );
      if (!mounted) return;
      setState(() {
        currentLocation = LatLng(position.latitude, position.longitude);
        currentCoordinates =
            '${position.latitude.toStringAsFixed(5)}, ${position.longitude.toStringAsFixed(5)}';
      });
      try {
        await widget.api.updateLocation(position.latitude, position.longitude);
      } catch (_) {
        // Map centering should still work if reverse geocoding is unavailable.
      }
    } catch (_) {
      if (mounted) {
        _message('Could not get your current location. Check GPS and try again.');
      }
    }
  }

  Future<void> _refreshMap() async {
    await loadAll();
    await _centerMapOnCurrentLocation();
  }

  int get unreadCount =>
      notifications.where((n) => n is Map && n['isRead'] != true).length;

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(
          title: const Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              RoadGuardMark(size: 32),
              SizedBox(width: 8),
              Text('RoadGuard', style: TextStyle(fontWeight: FontWeight.bold)),
            ],
          ),
          actions: [
            const RoadGuardThemeButton(),
            IconButton(
              tooltip: 'Notifications',
              onPressed: () => Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => NotificationsPage(
                    api: widget.api,
                    items: notifications,
                    refresh: loadAll,
                  ),
                ),
              ),
              icon: Badge(
                isLabelVisible: unreadCount > 0,
                label: Text('$unreadCount'),
                child: const Icon(Icons.notifications_outlined),
              ),
            ),
            IconButton(
              tooltip: 'Refresh',
              onPressed: loading ? null : loadAll,
              icon: const Icon(Icons.refresh),
            ),
          ],
        ),
        body: IndexedStack(
          index: tab,
          children: [
            _HomeTab(
              username: widget.username,
              nearby: nearby,
              mine: mine,
              loading: loading,
              error: loadError,
              refresh: loadAll,
              openReport: _showReport,
              createReport: _createReport,
              showNearby: () => setState(() => tab = 2),
            ),
            PublicMapPage(
              api: widget.api,
              reports: nearby,
              currentLocation: currentLocation,
              fallbackLocation:
                  profile['latitude'] is num && profile['longitude'] is num
                      ? LatLng(
                          (profile['latitude'] as num).toDouble(),
                          (profile['longitude'] as num).toDouble(),
                        )
                      : null,
              refresh: _refreshMap,
              openReport: _showReport,
            ),
            NearbyReportsPage(
              reports: nearby,
              loading: loading,
              error: loadError,
              refresh: loadAll,
              openReport: _showReport,
            ),
            MyReportsPage(
              api: widget.api,
              reports: mine,
              loading: loading,
              refresh: loadAll,
              openReport: _showReport,
            ),
            ProfilePage(
              api: widget.api,
              username: widget.username,
              profile: profile,
              currentCoordinates: currentCoordinates,
              refresh: loadAll,
            ),
          ],
        ),
        bottomNavigationBar: NavigationBar(
          selectedIndex: tab,
          onDestinationSelected: (value) {
            setState(() => tab = value);
            if (value == 1) _centerMapOnCurrentLocation();
          },
          destinations: const [
            NavigationDestination(
                icon: Icon(Icons.home_outlined), label: 'Home'),
            NavigationDestination(icon: Icon(Icons.map_outlined), label: 'Map'),
            NavigationDestination(
              icon: Icon(Icons.warning_amber_outlined),
              label: 'Nearby',
            ),
            NavigationDestination(
              icon: Icon(Icons.assignment_outlined),
              label: 'My reports',
            ),
            NavigationDestination(
              icon: Icon(Icons.person_outline),
              label: 'Profile',
            ),
          ],
        ),
      );

  Future<void> _createReport() async {
    await Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => CreateReportPage(api: widget.api)),
    );
    await loadAll();
  }

  void _showReport(Map<String, dynamic> report) =>
      showReportDetails(context, report, widget.api);
}

class _HomeTab extends StatelessWidget {
  const _HomeTab({
    required this.username,
    required this.nearby,
    required this.mine,
    required this.loading,
    required this.error,
    required this.refresh,
    required this.openReport,
    required this.createReport,
    required this.showNearby,
  });
  final String username;
  final List<dynamic> nearby;
  final List<dynamic> mine;
  final bool loading;
  final String? error;
  final Future<void> Function() refresh;
  final void Function(Map<String, dynamic>) openReport;
  final VoidCallback createReport;
  final VoidCallback showNearby;

  @override
  Widget build(BuildContext context) => RefreshIndicator(
        onRefresh: refresh,
        child: ListView(
          padding: const EdgeInsets.all(18),
          children: [
            Text(
              'Good day, $username',
              style: const TextStyle(
                fontSize: 23,
                fontWeight: FontWeight.bold,
                color: ink,
              ),
            ),
            const SizedBox(height: 5),
            const Text('Help make your neighborhood safer.'),
            const SizedBox(height: 18),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Row(
                  children: [
                    const RoadGuardMark(size: 46),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Report a road hazard',
                            style: Theme.of(context)
                                .textTheme
                                .titleMedium
                                ?.copyWith(fontWeight: FontWeight.bold),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            'Take a camera photo and share its location.',
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 10),
            FilledButton.icon(
              onPressed: createReport,
              icon: const Icon(Icons.add_a_photo_outlined),
              label: const Text('Create report'),
            ),
            const SizedBox(height: 20),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'Nearby hazards',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
                TextButton(
                    onPressed: showNearby, child: const Text('View list')),
              ],
            ),
            if (loading)
              const Center(child: CircularProgressIndicator())
            else if (error != null)
              _ErrorCard(message: error!)
            else
              ...nearby.take(3).map(
                    (r) => ReportCard(
                      report: Map<String, dynamic>.from(r as Map),
                      onTap: () =>
                          openReport(Map<String, dynamic>.from(r as Map)),
                    ),
                  ),
            const SizedBox(height: 12),
            const Text(
              'Your recent reports',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            if (!loading && mine.isEmpty)
              const Padding(
                padding: EdgeInsets.all(12),
                child: Text('Your submitted reports will appear here.'),
              )
            else
              ...mine.take(2).map(
                    (r) => ReportCard(
                      report: Map<String, dynamic>.from(r as Map),
                      onTap: () =>
                          openReport(Map<String, dynamic>.from(r as Map)),
                    ),
                  ),
          ],
        ),
      );
}

class NearbyReportsPage extends StatelessWidget {
  const NearbyReportsPage({
    super.key,
    required this.reports,
    required this.loading,
    required this.error,
    required this.refresh,
    required this.openReport,
  });
  final List<dynamic> reports;
  final bool loading;
  final String? error;
  final Future<void> Function() refresh;
  final void Function(Map<String, dynamic>) openReport;
  @override
  Widget build(BuildContext context) => RefreshIndicator(
        onRefresh: refresh,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            const Text(
              'Nearby hazards',
              style: TextStyle(fontSize: 23, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 4),
            const Text('Reports in your area. Rejected reports are not shown.'),
            const SizedBox(height: 14),
            if (loading)
              const Center(child: CircularProgressIndicator())
            else if (error != null)
              _ErrorCard(message: error!)
            else if (reports.isEmpty)
              const Text('No nearby reports yet.')
            else
              ...reports.map((r) {
                final report = Map<String, dynamic>.from(r as Map);
                return ReportCard(
                    report: report, onTap: () => openReport(report));
              }),
          ],
        ),
      );
}

class MyReportsPage extends StatelessWidget {
  const MyReportsPage({
    super.key,
    required this.api,
    required this.reports,
    required this.loading,
    required this.refresh,
    required this.openReport,
  });
  final RoadGuardApi api;
  final List<dynamic> reports;
  final bool loading;
  final Future<void> Function() refresh;
  final void Function(Map<String, dynamic>) openReport;
  @override
  Widget build(BuildContext context) => RefreshIndicator(
        onRefresh: refresh,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            const Text(
              'My reports',
              style: TextStyle(fontSize: 23, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            if (loading)
              const Center(child: CircularProgressIndicator())
            else if (reports.isEmpty)
              const Text('Your submitted reports will appear here.')
            else
              ...reports.map((r) {
                final report = Map<String, dynamic>.from(r as Map);
                final resolved =
                    (report['reportStatus'] ?? '').toString().toUpperCase() ==
                        'RESOLVED';
                return Column(
                  children: [
                    ReportCard(report: report, onTap: () => openReport(report)),
                    if (resolved && report['feedback'] == null)
                      Align(
                        alignment: Alignment.centerRight,
                        child: TextButton.icon(
                          onPressed: () => _giveReportFeedback(
                              context, api, report, refresh),
                          icon: const Icon(Icons.rate_review_outlined),
                          label: const Text('Give feedback'),
                        ),
                      ),
                  ],
                );
              }),
          ],
        ),
      );
}

Future<void> _giveReportFeedback(
  BuildContext context,
  RoadGuardApi api,
  Map<String, dynamic> report,
  Future<void> Function() refresh,
) async {
  final reportId = report['reportId'];
  if (reportId is! num) {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('This report is missing its report ID.')),
    );
    return;
  }
  final controller = TextEditingController();
  var rating = 5;
  final result = await showDialog<(String, int)>(
    context: context,
    builder: (context) => StatefulBuilder(
      builder: (context, setDialogState) => AlertDialog(
        title: Text('Feedback for report #${reportId.toInt()}'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            DropdownButtonFormField<int>(
              initialValue: rating,
              decoration: const InputDecoration(labelText: 'Rating'),
              items: List.generate(
                5,
                (i) => DropdownMenuItem(
                  value: i + 1,
                  child: Text('${i + 1} star${i == 0 ? '' : 's'}'),
                ),
              ),
              onChanged: (v) {
                if (v != null) setDialogState(() => rating = v);
              },
            ),
            const SizedBox(height: 12),
            TextField(
              controller: controller,
              maxLines: 3,
              decoration: const InputDecoration(
                labelText: 'Your feedback',
                border: OutlineInputBorder(),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              if (controller.text.trim().isNotEmpty)
                Navigator.pop(context, (controller.text.trim(), rating));
            },
            child: const Text('Submit'),
          ),
        ],
      ),
    ),
  );
  controller.dispose();
  if (result == null) return;
  try {
    await api.submitFeedback(reportId.toInt(), result.$1, result.$2);
    await refresh();
    if (context.mounted)
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Thanks, your feedback was submitted.')),
      );
  } catch (e) {
    if (context.mounted)
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e.toString().replaceFirst('Exception: ', ''))),
      );
  }
}

class ProfilePage extends StatefulWidget {
  const ProfilePage({
    super.key,
    required this.api,
    required this.username,
    required this.profile,
    required this.currentCoordinates,
    required this.refresh,
  });
  final RoadGuardApi api;
  final String username;
  final Map<String, dynamic> profile;
  final String? currentCoordinates;
  final Future<void> Function() refresh;

  @override
  State<ProfilePage> createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  bool savingProfile = false;
  bool changingPhone = false;

  Future<void> _editProfile() async {
    final nameController = TextEditingController(
      text: (widget.profile['name'] ?? widget.username).toString(),
    );
    final emailController = TextEditingController(
      text: (widget.profile['email'] ?? '').toString(),
    );
    final updates = await showDialog<Map<String, String>>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Edit profile'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: nameController,
                textCapitalization: TextCapitalization.words,
                decoration: const InputDecoration(labelText: 'Name'),
              ),
              TextField(
                controller: emailController,
                keyboardType: TextInputType.emailAddress,
                decoration:
                    const InputDecoration(labelText: 'Email (optional)'),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              final name = nameController.text.trim();
              final email = emailController.text.trim();
              if (name.isEmpty ||
                  (email.isNotEmpty &&
                      (!email.contains('@') || !email.contains('.')))) return;
              Navigator.pop(dialogContext, {'name': name, 'email': email});
            },
            child: const Text('Save'),
          ),
        ],
      ),
    );
    await Future<void>.delayed(const Duration(milliseconds: 300));
    nameController.dispose();
    emailController.dispose();
    if (updates == null) return;
    setState(() => savingProfile = true);
    try {
      await widget.api.updateProfile(
        name: updates['name']!,
        email: updates['email']!,
      );
      await widget.refresh();
      if (mounted)
        ScaffoldMessenger.of(context)
            .showSnackBar(const SnackBar(content: Text('Profile updated.')));
    } catch (e) {
      if (mounted)
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.toString().replaceFirst('Exception: ', ''))),
        );
    } finally {
      if (mounted) setState(() => savingProfile = false);
    }
  }

  String? _extractDemoOtp(Map<String, dynamic> result) {
    final value = (result['otp'] ??
            result['demoOtp'] ??
            result['code'] ??
            result['message'] ??
            '')
        .toString();
    return RegExp(r'\b\d{6}\b').firstMatch(value)?.group(0);
  }

  Future<void> _changePhone() async {
    final currentPhone =
        (widget.profile['phoneNumber'] ?? '').toString().trim();
    if (currentPhone.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Current phone number is unavailable.')),
      );
      return;
    }
    final newPhone = TextEditingController();
    final oldOtp = TextEditingController();
    final newOtp = TextEditingController();
    String? oldDemoOtp;
    String? newDemoOtp;
    String? dialogError;
    bool oldOtpSent = false;
    bool newOtpSent = false;
    final values = await showDialog<Map<String, String>>(
      context: context,
      builder: (dialogContext) => StatefulBuilder(
        builder: (dialogContext, setDialogState) => AlertDialog(
          title: const Text('Change phone number'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text('Current phone: $currentPhone'),
                TextButton(
                  onPressed: oldOtpSent
                      ? null
                      : () async {
                          try {
                            final response = await widget.api.sendOtp(
                              currentPhone,
                            );
                            setDialogState(() {
                              oldOtpSent = true;
                              oldDemoOtp = _extractDemoOtp(response);
                              dialogError = null;
                            });
                          } catch (e) {
                            setDialogState(
                              () => dialogError = e.toString().replaceFirst(
                                    'Exception: ',
                                    '',
                                  ),
                            );
                          }
                        },
                  child: Text(
                    oldOtpSent
                        ? 'Code sent to current phone'
                        : 'Send code to current phone',
                  ),
                ),
                if (oldDemoOtp != null) Text('Demo OTP: $oldDemoOtp'),
                if (oldOtpSent)
                  TextField(
                    controller: oldOtp,
                    keyboardType: TextInputType.number,
                    onChanged: (_) => setDialogState(() {}),
                    decoration: const InputDecoration(
                      labelText: 'Current phone OTP',
                    ),
                  ),
                TextField(
                  controller: newPhone,
                  keyboardType: TextInputType.phone,
                  inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                  maxLength: 10,
                  onChanged: (_) => setDialogState(() {
                    newOtpSent = false;
                    newDemoOtp = null;
                    newOtp.clear();
                  }),
                  decoration: const InputDecoration(
                    prefixIcon: const _IndiaCountryCodeBadge(),
                    prefixIconConstraints:
                        const BoxConstraints(minWidth: 60, minHeight: 0),
                    labelText: 'New phone number',
                  ),
                ),
                TextButton(
                  onPressed: newOtpSent || newPhone.text.trim().length != 10
                      ? null
                      : () async {
                          try {
                            final response = await widget.api.sendOtp(
                              newPhone.text.trim(),
                            );
                            setDialogState(() {
                              newOtpSent = true;
                              newDemoOtp = _extractDemoOtp(response);
                              dialogError = null;
                            });
                          } catch (e) {
                            setDialogState(
                              () => dialogError = e.toString().replaceFirst(
                                    'Exception: ',
                                    '',
                                  ),
                            );
                          }
                        },
                  child: Text(
                    newOtpSent
                        ? 'Code sent to new phone'
                        : 'Send code to new phone',
                  ),
                ),
                if (newDemoOtp != null) Text('Demo OTP: $newDemoOtp'),
                if (newOtpSent)
                  TextField(
                    controller: newOtp,
                    keyboardType: TextInputType.number,
                    onChanged: (_) => setDialogState(() {}),
                    decoration: const InputDecoration(
                      labelText: 'New phone OTP',
                    ),
                  ),
                if (dialogError != null)
                  Padding(
                    padding: const EdgeInsets.only(top: 8),
                    child: Text(
                      dialogError!,
                      style: const TextStyle(color: Colors.red),
                    ),
                  ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: oldOtpSent &&
                      newOtpSent &&
                      oldOtp.text.trim().isNotEmpty &&
                      newOtp.text.trim().isNotEmpty &&
                      newPhone.text.trim().length == 10
                  ? () => Navigator.pop(dialogContext, {
                        'phone': newPhone.text.trim(),
                        'oldOtp': oldOtp.text.trim(),
                        'newOtp': newOtp.text.trim(),
                      })
                  : null,
              child: const Text('Verify and update'),
            ),
          ],
        ),
      ),
    );
    await Future<void>.delayed(const Duration(milliseconds: 300));
    newPhone.dispose();
    oldOtp.dispose();
    newOtp.dispose();
    if (values == null) return;
    setState(() => changingPhone = true);
    try {
      await widget.api.updatePhoneNumber(
        phoneNumber: values['phone']!,
        oldPhoneNumberOtp: values['oldOtp']!,
        newPhoneNumberOtp: values['newOtp']!,
      );
      await widget.refresh();
      if (mounted)
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('Phone number updated.')));
    } catch (e) {
      if (mounted)
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.toString().replaceFirst('Exception: ', ''))),
        );
    } finally {
      if (mounted) setState(() => changingPhone = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final name = (widget.profile['name'] ?? widget.username).toString();
    final address = (widget.profile['address'] ?? '').toString().trim();
    final location = address.isNotEmpty
        ? address
        : widget.currentCoordinates ??
            'Location unavailable. Refresh to try again.';
    final email = (widget.profile['email'] ?? '').toString().trim();
    return RefreshIndicator(
      onRefresh: widget.refresh,
      child: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          const CircleAvatar(radius: 38, child: Icon(Icons.person, size: 38)),
          const SizedBox(height: 10),
          Text(
            name,
            textAlign: TextAlign.center,
            style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 16),
          Card(
            child: Column(
              children: [
                _ProfileRow(
                  icon: Icons.email_outlined,
                  label: 'Email',
                  value: email.isEmpty ? 'Not set' : email,
                ),
                _ProfileRow(
                  icon: Icons.phone_outlined,
                  label: 'Phone',
                  value: (widget.profile['phoneNumber'] ?? 'Not provided')
                      .toString(),
                ),
                _ProfileRow(
                  icon: Icons.location_on_outlined,
                  label: 'Current location',
                  value: location,
                ),
              ],
            ),
          ),
          const ListTile(
            leading: Icon(Icons.privacy_tip_outlined),
            title: Text('Privacy'),
            subtitle: Text(
              'Your identity is not shown on public hazard reports.',
            ),
          ),
          OutlinedButton.icon(
            onPressed: savingProfile ? null : _editProfile,
            icon: const Icon(Icons.edit_outlined),
            label: Text(savingProfile ? 'Saving…' : 'Edit name and email'),
          ),
          const SizedBox(height: 8),
          OutlinedButton.icon(
            onPressed: changingPhone ? null : _changePhone,
            icon: const Icon(Icons.phone_android_outlined),
            label: Text(changingPhone ? 'Updating…' : 'Change phone number'),
          ),
          const SizedBox(height: 8),
          OutlinedButton.icon(
            onPressed: widget.refresh,
            icon: const Icon(Icons.my_location),
            label: const Text('Refresh profile and location'),
          ),
          const SizedBox(height: 8),
          OutlinedButton.icon(
            onPressed: () async {
              await widget.api.clearToken();
              if (!context.mounted) return;
              Navigator.pushAndRemoveUntil(
                context,
                MaterialPageRoute(builder: (_) => const AuthPage()),
                (_) => false,
              );
            },
            icon: const Icon(Icons.logout),
            label: const Text('Sign out'),
          ),
        ],
      ),
    );
  }
}

class _ProfileRow extends StatelessWidget {
  const _ProfileRow({
    required this.icon,
    required this.label,
    required this.value,
  });
  final IconData icon;
  final String label;
  final String value;
  @override
  Widget build(BuildContext context) => ListTile(
        leading: Icon(icon, color: green),
        title: Text(
          label,
          style: const TextStyle(fontSize: 12, color: Colors.black54),
        ),
        subtitle: Text(value, style: const TextStyle(fontSize: 15, color: ink)),
      );
}

class NotificationsPage extends StatefulWidget {
  const NotificationsPage({
    super.key,
    required this.api,
    required this.items,
    required this.refresh,
  });
  final RoadGuardApi api;
  final List<dynamic> items;
  final Future<void> Function() refresh;
  @override
  State<NotificationsPage> createState() => _NotificationsPageState();
}

class _NotificationsPageState extends State<NotificationsPage> {
  late List<dynamic> items;
  bool busy = false;
  @override
  void initState() {
    super.initState();
    items = widget.items
        .map((item) => Map<String, dynamic>.from(item as Map))
        .toList();
  }

  Future<void> _reload() async {
    try {
      final latest = await widget.api.notifications();
      if (mounted)
        setState(
          () => items = latest
              .map((item) => Map<String, dynamic>.from(item as Map))
              .toList(),
        );
      await widget.refresh();
    } catch (e) {
      if (mounted)
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Could not refresh notifications: $e')),
        );
    }
  }

  Future<void> _markRead(Map<String, dynamic> item) async {
    final id = item['notificationId'];
    if (id is! num || item['isRead'] == true) return;
    setState(() => busy = true);
    try {
      await widget.api.markNotificationRead(id.toInt());
      if (mounted) setState(() => item['isRead'] = true);
      await _reload();
    } catch (e) {
      if (mounted)
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Could not mark notification as read: $e')),
        );
    } finally {
      if (mounted) setState(() => busy = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(
          title: const Text('Notifications'),
          actions: [
            if (busy)
              const Padding(
                padding: EdgeInsets.all(16),
                child: SizedBox(
                  width: 18,
                  height: 18,
                  child: CircularProgressIndicator(strokeWidth: 2),
                ),
              ),
          ],
        ),
        body: RefreshIndicator(
          onRefresh: _reload,
          child: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              if (items.isEmpty)
                const Padding(
                  padding: EdgeInsets.all(20),
                  child: Text('No notifications yet.'),
                )
              else
                ...items.map(
                  (item) => Card(
                    child: ListTile(
                      onTap: () => _markRead(item),
                      leading: Icon(
                        item['isRead'] == true
                            ? Icons.notifications_none
                            : Icons.notifications_active_outlined,
                        color: green,
                      ),
                      title: Text(
                          (item['title'] ?? 'RoadGuard update').toString()),
                      subtitle: Text((item['message'] ?? '').toString()),
                      trailing: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(_formatDate(item['createdAt'])),
                          if (item['isRead'] != true)
                            const Text(
                              'NEW',
                              style: TextStyle(
                                color: green,
                                fontWeight: FontWeight.bold,
                                fontSize: 10,
                              ),
                            ),
                        ],
                      ),
                      isThreeLine: true,
                    ),
                  ),
                ),
            ],
          ),
        ),
      );
}

class PublicMapPage extends StatefulWidget {
  const PublicMapPage({
    super.key,
    required this.api,
    required this.reports,
    required this.currentLocation,
    required this.fallbackLocation,
    required this.refresh,
    required this.openReport,
  });
  final RoadGuardApi api;
  final List<dynamic> reports;
  final LatLng? currentLocation;
  final LatLng? fallbackLocation;
  final Future<void> Function() refresh;
  final void Function(Map<String, dynamic>) openReport;
  @override
  State<PublicMapPage> createState() => _PublicMapPageState();
}

class _PublicMapPageState extends State<PublicMapPage> {
  bool satellite = true;
  final MapController _mapController = MapController();
  double _mapZoom = 15;

  Future<void> _showPinReports(_ReportPinGroup group) async {
    if (group.reports.length == 1) {
      widget.openReport(group.reports.first);
      return;
    }
    _mapController.move(group.center, math.min(_mapZoom + 2, 19));
    final selected = await showModalBottomSheet<Map<String, dynamic>>(
      context: context,
      showDragHandle: true,
      builder: (context) => SafeArea(
        child: ListView(
          shrinkWrap: true,
          padding: const EdgeInsets.fromLTRB(16, 0, 16, 18),
          children: [
            Text(
              '${group.reports.length} reports at this location',
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            ...group.reports.map(
              (report) => ListTile(
                leading: Icon(Icons.location_on,
                    color: reportStatusColor(report['reportStatus'])),
                title: Text(_category(report)),
                subtitle: Text(
                  '${_label(report['reportStatus'])} · ${_reportLocationText(report)}',
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
                onTap: () => Navigator.pop(context, report),
              ),
            ),
          ],
        ),
      ),
    );
    if (selected != null && mounted) widget.openReport(selected);
  }

  @override
  Widget build(BuildContext context) {
    final currentLocation = widget.currentLocation;
    final valid = widget.reports
        .where((r) => r is Map && r['latitude'] is num && r['longitude'] is num)
        .toList();
    final groups = _groupReportPins(valid, _mapZoom);
    final reportPoints = valid
        .map((report) => LatLng(
              (report['latitude'] as num).toDouble(),
              (report['longitude'] as num).toDouble(),
            ))
        .toList();
    final fallbackCenter = widget.fallbackLocation ??
        (reportPoints.isNotEmpty
            ? reportPoints.first
            : const LatLng(20.2961, 85.8245));
    final fitReportArea = currentLocation == null &&
        widget.fallbackLocation == null &&
        reportPoints.isNotEmpty;
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(14, 12, 8, 8),
          child: Row(
            children: [
              const Expanded(
                child: Text(
                  'Nearby hazards',
                  style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
                ),
              ),
              SegmentedButton<bool>(
                segments: const [
                  ButtonSegment(
                    value: true,
                    label: Text('Satellite'),
                    icon: Icon(Icons.satellite_alt),
                  ),
                  ButtonSegment(
                    value: false,
                    label: Text('Street'),
                    icon: Icon(Icons.map_outlined),
                  ),
                ],
                selected: {satellite},
                onSelectionChanged: (value) =>
                    setState(() => satellite = value.first),
              ),
              IconButton(
                onPressed: widget.refresh,
                icon: const Icon(Icons.refresh),
              ),
            ],
          ),
        ),
        Expanded(
          child: FlutterMap(
            mapController: _mapController,
            key: ValueKey(
                '$currentLocation|${widget.fallbackLocation}|${valid.length}'),
            options: MapOptions(
              initialCameraFit: fitReportArea
                  ? CameraFit.coordinates(
                      coordinates: reportPoints,
                      padding: const EdgeInsets.all(48),
                      minZoom: 12,
                      maxZoom: 15,
                    )
                  : null,
              initialCenter: currentLocation ?? fallbackCenter,
              initialZoom: currentLocation != null ||
                      widget.fallbackLocation != null
                  ? 15
                  : 12,
              onPositionChanged: (camera, _) {
                if ((camera.zoom - _mapZoom).abs() >= 0.5 && mounted) {
                  setState(() => _mapZoom = camera.zoom);
                }
              },
            ),
            children: [
              TileLayer(
                urlTemplate: satellite
                    ? 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}'
                    : 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                userAgentPackageName: 'com.roadguard.roadguard_citizen',
                maxZoom: 19,
                tileProvider: NetworkTileProvider(),
              ),
              RichAttributionWidget(
                attributions: [
                  TextSourceAttribution(
                    satellite ? 'Tiles © Esri' : '© OpenStreetMap contributors',
                  ),
                ],
              ),
              if (currentLocation != null)
                MarkerLayer(
                  markers: [
                    Marker(
                      point: currentLocation,
                      width: 44,
                      height: 44,
                      child: const Tooltip(
                        message: 'Your current GPS location',
                        child: Icon(Icons.my_location,
                            color: Colors.blue, size: 34),
                      ),
                    ),
                  ],
                ),
              MarkerLayer(
                markers: groups.map((group) {
                  final first = group.reports.first;
                  return Marker(
                    point: group.center,
                    width: group.reports.length > 1 ? 48 : 42,
                    height: group.reports.length > 1 ? 48 : 44,
                    child: GestureDetector(
                      onTap: () => _showPinReports(group),
                      child: group.reports.length > 1
                          ? Container(
                              decoration: BoxDecoration(
                                color: ink,
                                shape: BoxShape.circle,
                                border:
                                    Border.all(color: Colors.white, width: 3),
                                boxShadow: const [
                                  BoxShadow(
                                      color: Colors.black26, blurRadius: 8),
                                ],
                              ),
                              alignment: Alignment.center,
                              child: Text(
                                '${group.reports.length}',
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            )
                          : Icon(
                              Icons.location_on,
                              color: reportStatusColor(first['reportStatus']),
                              size: 40,
                            ),
                    ),
                  );
                }).toList(),
              ),
            ],
          ),
        ),
        if (valid.isEmpty)
          const Padding(
            padding: EdgeInsets.all(8),
            child: Text('No nearby reports with map coordinates yet.'),
          ),
      ],
    );
  }
}

class _ReportPinGroup {
  _ReportPinGroup(this.reports, this.center);
  final List<Map<String, dynamic>> reports;
  LatLng center;
}

List<_ReportPinGroup> _groupReportPins(List<dynamic> rawReports, double zoom) {
  final thresholdMeters = 100 * math.pow(2, 15 - zoom);
  final groups = <_ReportPinGroup>[];
  for (final raw in rawReports) {
    final report = Map<String, dynamic>.from(raw as Map);
    final point = LatLng(
      (report['latitude'] as num).toDouble(),
      (report['longitude'] as num).toDouble(),
    );
    _ReportPinGroup? matching;
    for (final group in groups) {
      final meanLat = (point.latitude + group.center.latitude) / 2;
      final deltaLat = (point.latitude - group.center.latitude) * 111320;
      final deltaLon = (point.longitude - group.center.longitude) *
          111320 *
          math.cos(meanLat * math.pi / 180);
      if (math.sqrt(deltaLat * deltaLat + deltaLon * deltaLon) <=
          thresholdMeters) {
        matching = group;
        break;
      }
    }
    if (matching == null) {
      groups.add(_ReportPinGroup([report], point));
    } else {
      matching.reports.add(report);
      final count = matching.reports.length;
      matching.center = LatLng(
        matching.center.latitude +
            (point.latitude - matching.center.latitude) / count,
        matching.center.longitude +
            (point.longitude - matching.center.longitude) / count,
      );
    }
  }
  return groups;
}

Color reportStatusColor(dynamic status) =>
    switch ((status ?? '').toString().toUpperCase()) {
      'SUBMITTED' || 'RESOLVED' => const Color(0xFF24733B),
      'PENDING_REVIEW' => const Color(0xFFB67A00),
      'VERIFIED' => const Color(0xFF087C69),
      'ASSIGNED' => const Color(0xFF1766A8),
      'IN_PROGRESS' => const Color(0xFFB85C00),
      'REJECTED' || 'AI_REJECTED' => const Color(0xFFB63831),
      'DUPLICATE' => const Color(0xFF697783),
      _ => green,
    };

class ReportCard extends StatelessWidget {
  const ReportCard({super.key, required this.report, required this.onTap});
  final Map<String, dynamic> report;
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) {
    return Card(
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ListTile(
              leading: const Icon(Icons.warning_amber_rounded, color: green),
              title: Text(
                _category(report),
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
              subtitle: Padding(
                padding: const EdgeInsets.only(top: 7),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    ReportStatusBadge(status: report['reportStatus']),
                    const SizedBox(height: 6),
                    Text(
                      '${report['road'] ?? report['fullAddress'] ?? report['address'] ?? report['ward'] ?? report['city'] ?? 'Location recorded'}',
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                    Text(_coordinates(report)),
                  ],
                ),
              ),
              trailing: const Icon(Icons.chevron_right),
            ),
            if ((report['aiExplanation'] ?? '').toString().isNotEmpty)
              Padding(
                padding: const EdgeInsets.fromLTRB(16, 0, 16, 14),
                child: Text(
                  'AI assessment: ${report['aiExplanation']}',
                  maxLines: 3,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(color: Colors.black87),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class ReportStatusBadge extends StatelessWidget {
  const ReportStatusBadge({super.key, required this.status});
  final dynamic status;

  @override
  Widget build(BuildContext context) {
    final value = (status ?? '').toString().toUpperCase();
    final (background, foreground) = switch (value) {
      'SUBMITTED' => (const Color(0xFFE4F6E9), const Color(0xFF24733B)),
      'PENDING_REVIEW' => (const Color(0xFFFFF2D8), const Color(0xFF986300)),
      'VERIFIED' => (const Color(0xFFE4F5F1), const Color(0xFF087C69)),
      'ASSIGNED' => (const Color(0xFFE5F1FF), const Color(0xFF1766A8)),
      'IN_PROGRESS' => (const Color(0xFFFFEBD6), const Color(0xFF9B4F00)),
      'RESOLVED' => (const Color(0xFFDDF1E2), const Color(0xFF1F6333)),
      'REJECTED' => (const Color(0xFFFFE9E7), const Color(0xFFB63831)),
      'AI_REJECTED' => (const Color(0xFFFFE9E7), const Color(0xFFB63831)),
      'DUPLICATE' => (const Color(0xFFEDF0F3), const Color(0xFF586675)),
      _ => (const Color(0xFFEDF0F3), const Color(0xFF586675)),
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 4),
      decoration: BoxDecoration(
        color: background,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        _label(value),
        style: TextStyle(
          color: foreground,
          fontSize: 11,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class ReportSeverityBadge extends StatelessWidget {
  const ReportSeverityBadge({super.key, required this.severity});
  final dynamic severity;

  @override
  Widget build(BuildContext context) {
    final value = (severity ?? '').toString().toUpperCase();
    final color = switch (value) {
      'CRITICAL' || 'HIGH' => const Color(0xFFB63831),
      'MEDIUM' => const Color(0xFF986300),
      'LOW' => const Color(0xFF087C69),
      _ => const Color(0xFF586675),
    };
    return Text(
      'Severity: ${_label(value)}',
      style: TextStyle(color: color, fontWeight: FontWeight.w700),
    );
  }
}

String _reportLocationText(Map<String, dynamic> report) {
  final values = [
    report['fullAddress'],
    report['road'],
    report['address'],
    report['ward'],
    report['city'],
    report['district'],
    report['state'],
  ]
      .where((v) => v != null && v.toString().trim().isNotEmpty)
      .map((v) => v.toString())
      .toSet();
  return values.isEmpty ? 'Location recorded' : values.join(', ');
}

class _RoadInformationPanel extends StatelessWidget {
  const _RoadInformationPanel(
      {required this.api, required this.latitude, required this.longitude});
  final RoadGuardApi api;
  final double latitude;
  final double longitude;

  @override
  Widget build(BuildContext context) => FutureBuilder<Map<String, dynamic>>(
        future: api.roadInformation(latitude, longitude),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const ListTile(
              contentPadding: EdgeInsets.zero,
              leading: Icon(Icons.route_outlined, color: green),
              title: Text('Nearby road record'),
              subtitle: Text('Loading road and contractor details...'),
            );
          }
          if (!snapshot.hasData || snapshot.hasError) {
            return const ListTile(
              contentPadding: EdgeInsets.zero,
              leading: Icon(Icons.route_outlined, color: green),
              title: Text('Nearby road record'),
              subtitle:
                  Text('No registered road record was found near this report.'),
            );
          }
          final data = snapshot.data!;
          final name = data['name']?.toString();
          if (name == null || name.trim().isEmpty)
            return const SizedBox.shrink();
          final contractor = data['contractorName']?.toString();
          final details = <String>[
            if (contractor != null && contractor.trim().isNotEmpty)
              'Maintained by: ' + contractor,
            if (data['lastMaintenance'] != null)
              'Last maintenance: ' + _formatDate(data['lastMaintenance']),
            if (data['warrantyTill'] != null)
              'Warranty until: ' + _formatDate(data['warrantyTill']),
            if (data['budget'] != null)
              'Recorded budget: ' + data['budget'].toString(),
          ];
          return Card(
            color: pale,
            child: ListTile(
              contentPadding: const EdgeInsets.all(12),
              leading: const Icon(Icons.route_outlined, color: green),
              title: Text(name,
                  style: const TextStyle(fontWeight: FontWeight.bold)),
              subtitle: Text(details.isEmpty
                  ? 'Registered road near this report'
                  : details.join(' | ')),
            ),
          );
        },
      );
}

void showReportDetails(
    BuildContext context, Map<String, dynamic> report, RoadGuardApi api) {
  showModalBottomSheet<void>(
    context: context,
    isScrollControlled: true,
    builder: (context) => SafeArea(
      child: DraggableScrollableSheet(
        expand: false,
        initialChildSize: 0.72,
        maxChildSize: 0.95,
        builder: (_, controller) => ListView(
          controller: controller,
          padding: const EdgeInsets.all(18),
          children: [
            Text(
              _category(report),
              style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
            ),
            Wrap(
              spacing: 8,
              crossAxisAlignment: WrapCrossAlignment.center,
              children: [
                ReportStatusBadge(status: report['reportStatus']),
                ReportSeverityBadge(
                  severity: report['finalSeverity'] ?? report['aiSeverity'],
                ),
              ],
            ),
            const SizedBox(height: 12),
            if ((report['imageUrl'] ?? '').toString().isNotEmpty)
              ClipRRect(
                borderRadius: BorderRadius.circular(14),
                child: Image.network(
                  report['imageUrl'].toString(),
                  height: 230,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => const SizedBox(
                    height: 100,
                    child: Center(child: Icon(Icons.broken_image_outlined)),
                  ),
                ),
              ),
            const SizedBox(height: 12),
            Text(_reportLocationText(report)),
            if (report['latitude'] is num && report['longitude'] is num) ...[
              const SizedBox(height: 8),
              ListTile(
                contentPadding: EdgeInsets.zero,
                leading: const Icon(Icons.my_location, color: green),
                title: const Text('Coordinates'),
                subtitle: Text(
                  '${(report['latitude'] as num).toStringAsFixed(6)}, ${(report['longitude'] as num).toStringAsFixed(6)}',
                ),
              ),
            ],
            if (report['latitude'] is num && report['longitude'] is num)
              _RoadInformationPanel(
                api: api,
                latitude: (report['latitude'] as num).toDouble(),
                longitude: (report['longitude'] as num).toDouble(),
              ),
            if ((report['description'] ?? '').toString().isNotEmpty) ...[
              const SizedBox(height: 10),
              const Text(
                'Description',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              Text(report['description'].toString()),
            ],
            if ((report['aiExplanation'] ?? '').toString().isNotEmpty) ...[
              const SizedBox(height: 14),
              const Text(
                'Why the AI classified this hazard',
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
              Text(report['aiExplanation'].toString()),
            ],
            const SizedBox(height: 12),
            Text(
              'Reported ${_formatDate(report['createdAt'])}',
              style: const TextStyle(color: Colors.black54),
            ),
          ],
        ),
      ),
    ),
  );
}

class _ErrorCard extends StatelessWidget {
  const _ErrorCard({required this.message});
  final String message;
  @override
  Widget build(BuildContext context) => Card(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Text('Could not load reports: $message'),
        ),
      );
}

class CreateReportPage extends StatefulWidget {
  const CreateReportPage({super.key, required this.api});
  final RoadGuardApi api;
  @override
  State<CreateReportPage> createState() => _CreateReportPageState();
}

class _CreateReportPageState extends State<CreateReportPage> {
  final description = TextEditingController();
  File? image;
  Position? position;
  String? category;
  bool busy = false;
  static const categories = {
    'Pothole': 'POTHOLE',
    'Open manhole': 'OPEN_MANHOLE',
    'Waterlogging': 'WATERLOGGING',
    'Fallen tree': 'FALLEN_TREE',
    'Broken streetlight': 'BROKEN_STREETLIGHT',
    'Damaged road': 'DAMAGED_ROAD',
    'Broken traffic signal': 'BROKEN_TRAFFIC_SIGNAL',
    'Other': 'OTHER',
  };

  Future<void> capture() async {
    final file = await ImagePicker().pickImage(
      source: ImageSource.camera,
      imageQuality: 85,
    );
    if (file == null || !mounted) return;

    // Bind a fresh GPS fix to this camera capture immediately. The report must
    // not silently use a later location obtained after the user has moved.
    setState(() {
      image = File(file.path);
      position = null;
      busy = true;
    });
    try {
      final capturedPosition = await _getCurrentPosition();
      if (mounted) setState(() => position = capturedPosition);
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              '${e.toString().replaceFirst('Exception: ', '')} Retake the photo after GPS is available.',
            ),
          ),
        );
      }
    } finally {
      if (mounted) setState(() => busy = false);
    }
  }

  Future<Position> _getCurrentPosition() async {
    if (!await Geolocator.isLocationServiceEnabled()) {
      await Geolocator.openLocationSettings();
      throw Exception('Turn on device location, then retake the photo.');
    }
    var permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }
    if (permission == LocationPermission.denied ||
        permission == LocationPermission.deniedForever) {
      await Geolocator.openAppSettings();
      throw Exception('Allow location permission, then retake the photo.');
    }
    return Geolocator.getCurrentPosition(
      locationSettings: const LocationSettings(
        accuracy: LocationAccuracy.high,
      ),
    );
  }

  Future<void> submit() async {
    if (image == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Take a photo with the camera first.')),
      );
      return;
    }
    if (position == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Location was not captured. Turn on GPS and retake the photo.'),
        ),
      );
      return;
    }
    setState(() => busy = true);
    try {
      final result = await widget.api.submitReport(
        photo: image!,
        lat: position!.latitude,
        lon: position!.longitude,
        accuracy: position!.accuracy,
        description: description.text.trim(),
        category: category,
      );
      if (!mounted) return;
      await showDialog<void>(
        context: context,
        builder: (_) => AlertDialog(
          title: const Text('Report submitted'),
          content: Text(
            'Report #${result['reportId'] ?? 'created'}\nStatus: ${_label(result['reportStatus'] ?? 'SUBMITTED')}',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Done'),
            ),
          ],
        ),
      );
      if (mounted) Navigator.pop(context);
    } catch (e) {
      if (mounted)
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.toString().replaceFirst('Exception: ', ''))),
        );
    } finally {
      if (mounted) setState(() => busy = false);
    }
  }

  @override
  void dispose() {
    description.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Report a hazard')),
        body: ListView(
          padding: const EdgeInsets.all(18),
          children: [
            const Text(
              '1. Take a photo',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            if (image != null)
              ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: Image.file(image!, height: 210, fit: BoxFit.cover),
              )
            else
              Container(
                height: 150,
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Icon(
                  Icons.camera_alt_outlined,
                  size: 48,
                  color: green,
                ),
              ),
            const SizedBox(height: 8),
            OutlinedButton.icon(
              onPressed: busy ? null : capture,
              icon: const Icon(Icons.camera_alt),
              label: Text(image == null ? 'Open camera' : 'Retake photo'),
            ),
            const Text(
              'Only camera capture is offered; the gallery is not available here.',
              style: TextStyle(color: Colors.black54),
            ),
            const SizedBox(height: 20),
            const Text(
              '2. Confirm current location',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            ListTile(
              contentPadding: EdgeInsets.zero,
              leading: const Icon(Icons.my_location, color: green),
              title: Text(
                position != null
                    ? 'Location captured with photo'
                    : image == null
                        ? 'Location will be captured automatically'
                        : 'Location unavailable',
              ),
              subtitle: Text(
                position != null
                    ? '${position!.latitude.toStringAsFixed(5)}, ${position!.longitude.toStringAsFixed(5)} · ±${position!.accuracy.toStringAsFixed(0)} m'
                    : image == null
                        ? 'Take the photo and RoadGuard will fetch its location.'
                        : 'Turn on GPS and retake the photo to capture its location.',
              ),
            ),
            const SizedBox(height: 12),
            const Text(
              '3. Optional details',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            DropdownButtonFormField<String>(
              initialValue: category,
              decoration: const InputDecoration(
                labelText: 'Hazard category (optional)',
                border: OutlineInputBorder(),
              ),
              items: categories.entries
                  .map((e) =>
                      DropdownMenuItem(value: e.value, child: Text(e.key)))
                  .toList(),
              onChanged: (v) => setState(() => category = v),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: description,
              maxLines: 3,
              maxLength: 500,
              decoration: const InputDecoration(
                labelText: 'Description (optional)',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            FilledButton.icon(
              onPressed: busy ? null : submit,
              icon: const Icon(Icons.send),
              label: Text(busy ? 'Submitting…' : 'Submit report'),
            ),
          ],
        ),
      );
}

String _category(Map<String, dynamic> report) => _label(
      report['finalCategory'] ?? report['reportedCategory'] ?? 'Road hazard',
    );
String _coordinates(Map<String, dynamic> report) => report['latitude'] is num &&
        report['longitude'] is num
    ? '${(report['latitude'] as num).toStringAsFixed(5)}, ${(report['longitude'] as num).toStringAsFixed(5)}'
    : 'Coordinates unavailable';
String _label(dynamic value) => (value ?? 'Unknown')
    .toString()
    .replaceAll('_', ' ')
    .toLowerCase()
    .split(' ')
    .map(
      (word) =>
          word.isEmpty ? word : '${word[0].toUpperCase()}${word.substring(1)}',
    )
    .join(' ');
String _formatDate(dynamic value) {
  if (value == null) return '';
  final date = DateTime.tryParse(value.toString());
  return date == null
      ? value.toString()
      : '${date.day}/${date.month}/${date.year}';
}
