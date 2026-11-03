import 'dart:convert';
import 'dart:io';

import 'package:http/http.dart' as http;
import 'package:http_parser/http_parser.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'config.dart';

class RoadGuardApi {
  static const FlutterSecureStorage _secureStorage = FlutterSecureStorage();
  String? token;

  Future<void> restoreToken() async {
    token = await _secureStorage.read(key: 'roadguard_jwt');
  }

  Uri _uri(String path) => Uri.parse('${AppConfig.apiBaseUrl}$path');


  Future<bool> checkServiceHealth() async {
    try {
      final response = await http
          .get(_uri('/actuator/health'))
          .timeout(const Duration(seconds: 6));
      if (response.statusCode < 200 || response.statusCode >= 300) return false;
      final body = jsonDecode(response.body);
      return body is Map && body['status']?.toString().toUpperCase() == 'UP';
    } catch (_) {
      return false;
    }
  }

  Uri get mobileOAuthStartUri => _uri('/auth/oAuth2/mobileStart');

  Map<String, String> get _headers => {
        'Content-Type': 'application/json',
        if (token != null) 'Authorization': 'Bearer $token',
      };

  dynamic _decode(http.Response response) {
    dynamic body;
    try {
      body = response.body.isEmpty ? null : jsonDecode(response.body);
    } catch (_) {
      body = response.body;
    }
    if (response.statusCode < 200 || response.statusCode >= 300) {
      final detail = body is Map
          ? (body['exception'] ??
                  body['message'] ??
                  body['error'] ??
                  response.body)
              .toString()
          : body.toString();
      if (response.statusCode == 401) {
        throw Exception(
          'Sign-in failed. Check your phone/email, password, or OTP and try again.',
        );
      }
      if (response.statusCode == 403) {
        throw Exception(
          'This account is not allowed to use the citizen app. Please use the admin website.',
        );
      }
      if (detail.toLowerCase().contains('only jpeg') ||
          detail.toLowerCase().contains('only .jpg')) {
        throw Exception(
          'The camera photo format was not accepted. Retake the photo and try again.',
        );
      }
      throw Exception(detail);
    }
    return body;
  }

  Future<Map<String, dynamic>> sendOtp(String phone) async =>
      Map<String, dynamic>.from(
        _decode(
          await http.post(
            _uri('/auth/sendOtp'),
            headers: _headers,
            body: jsonEncode({'phoneNumber': phone}),
          ),
        ) as Map,
      );

  Future<Map<String, dynamic>> loginOtp(String phone, String otp) async =>
      _saveAuth(
        _decode(
          await http.post(
            _uri('/auth/login/phoneNumber'),
            headers: _headers,
            body: jsonEncode({'phoneNumber': phone, 'otp': otp}),
          ),
        ),
      );

  Future<Map<String, dynamic>> loginPassword(
    String phoneNumber,
    String password,
  ) async =>
      _saveAuth(
        _decode(
          await http.post(
            _uri('/auth/login/password'),
            headers: _headers,
            body: jsonEncode({
              'phoneNumber': phoneNumber.trim(),
              'password': password,
            }),
          ),
        ),
      );

  Future<Map<String, dynamic>> exchangeOAuthCode(String code) async =>
      _saveAuth(
        _decode(
          await http.post(
            _uri('/auth/oauth2/exchange'),
            headers: _headers,
            body: jsonEncode({'code': code}),
          ),
        ),
      );

  Future<Map<String, dynamic>> signup(
    String name,
    String phone,
    String password,
    String otp, {
    String? email,
  }) async =>
      _saveAuth(
        _decode(
          await http.post(
            _uri('/auth/signup/phoneNumber'),
            headers: _headers,
            body: jsonEncode({
              'name': name,
              'phoneNumber': phone,
              'password': password,
              'otp': otp,
              if (email != null && email.trim().isNotEmpty)
                'email': email.trim(),
            }),
          ),
        ),
      );

  Future<Map<String, dynamic>> _saveAuth(dynamic response) async {
    final data = Map<String, dynamic>.from(response as Map);
    final receivedToken = data['token']?.toString();
    if (receivedToken == null || receivedToken.isEmpty) {
      throw Exception('The backend response did not include a token.');
    }
    token = receivedToken;
    await _secureStorage.write(key: 'roadguard_jwt', value: receivedToken);
    return data;
  }

  Future<void> clearToken() async {
    token = null;
    await _secureStorage.delete(key: 'roadguard_jwt');
  }

  Future<List<dynamic>> reports(String path) async {
    final response = _decode(await http.get(_uri(path), headers: _headers));
    if (response is List) return response;
    if (response is Map && response['content'] is List) {
      return List<dynamic>.from(response['content'] as List);
    }
    return <dynamic>[];
  }

  Future<Map<String, dynamic>> profile() async => Map<String, dynamic>.from(
        _decode(await http.get(_uri('/user/getProfile'), headers: _headers))
            as Map,
      );

  Future<void> updateProfile({
    required String name,
    required String email,
  }) async {
    _decode(
      await http.patch(
        _uri('/user/updateProfile'),
        headers: _headers,
        body: jsonEncode({'name': name.trim(), 'email': email.trim()}),
      ),
    );
  }

  Future<void> updatePhoneNumber({
    required String phoneNumber,
    required String oldPhoneNumberOtp,
    required String newPhoneNumberOtp,
  }) async {
    final response = _decode(
      await http.post(
        _uri('/user/updatePhoneNumber'),
        headers: _headers,
        body: jsonEncode({
          'phoneNumber': phoneNumber.trim(),
          'oldPhoneNumberOtp': oldPhoneNumberOtp.trim(),
          'newPhoneNumberOtp': newPhoneNumberOtp.trim(),
        }),
      ),
    );
    await _saveAuth(response);
  }

  Future<void> submitFeedback(int reportId, String feedback, int rating) async {
    _decode(
      await http.post(
        _uri('/feedback/report/feedback/$reportId'),
        headers: _headers,
        body: jsonEncode({'feedback': feedback, 'rating': rating}),
      ),
    );
  }

  Future<List<dynamic>> notifications() async {
    final response = _decode(
      await http.get(
        _uri('/notification/getMyNotifications'),
        headers: _headers,
      ),
    );
    if (response is List) return response;
    if (response is Map && response['content'] is List) {
      return List<dynamic>.from(response['content'] as List);
    }
    return <dynamic>[];
  }

  Future<void> markNotificationRead(int notificationId) async {
    _decode(
      await http.post(
        _uri('/notification/markAsRead/$notificationId'),
        headers: _headers,
      ),
    );
  }

  Future<Map<String, dynamic>> roadInformation(
          double latitude, double longitude) async =>
      Map<String, dynamic>.from(
        _decode(await http.get(
          _uri('/roadInfo/getRoadInfo/$latitude/$longitude'),
          headers: _headers,
        )) as Map,
      );
  Future<void> updateLocation(double latitude, double longitude) async {
    final uri = _uri('/user/update_location').replace(
      queryParameters: {
        'latitude': latitude.toString(),
        'longitude': longitude.toString(),
      },
    );
    _decode(await http.post(uri, headers: _headers));
  }

  Future<Map<String, dynamic>> submitReport({
    required File photo,
    required double lat,
    required double lon,
    required double accuracy,
    required String description,
    String? category,
  }) async {
    final request = http.MultipartRequest(
      'POST',
      _uri('/citizen/report/create'),
    );
    if (token != null) request.headers['Authorization'] = 'Bearer $token';
    request.files.add(
      await http.MultipartFile.fromPath(
        'photo',
        photo.path,
        filename: 'roadguard_capture.jpg',
        contentType: MediaType('image', 'jpeg'),
      ),
    );
    request.files.add(
      http.MultipartFile.fromString(
        'data',
        jsonEncode({
          'description': description,
          'reportedCategory': category,
          'latitude': lat,
          'longitude': lon,
          'gpsAccuracy': accuracy,
          'cameraOnly': true,
          'vpnDetected': false,
          'fakeLocation': false,
          'capturedAt': DateTime.now().toIso8601String(),
        }),
        contentType: MediaType('application', 'json'),
      ),
    );
    final response = await http.Response.fromStream(await request.send());
    return Map<String, dynamic>.from(_decode(response) as Map);
  }
}
