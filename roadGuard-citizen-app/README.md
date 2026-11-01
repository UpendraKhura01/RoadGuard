# RoadGuard Citizen App

Flutter Android client scaffold for citizen sign-in, map browsing, camera-only photo capture, current-location report submission, and personal report tracking.

## Run

1. Install Flutter and Android Studio/Android SDK.
2. From this folder run `flutter create --platforms=android .` once to generate the standard Android runner, then merge the permissions in `android/app/src/main/AndroidManifest.xml` (camera, internet, fine/coarse location) and allow cleartext traffic for local HTTP development.
3. Run `flutter pub get`.
4. Start the Spring Boot backend and set `API_BASE_URL` in `lib/config.dart` to a URL reachable from the phone. Android emulator uses `http://10.0.2.2:8080`; a physical phone needs the computer's LAN IP and backend access on that interface.
5. Run `flutter run`.

The backend's demo OTP response is displayed in the app during development. Do not use that behavior for production phone verification.

## Backend integration notes

- Auth uses `/auth/sendOtp`, `/auth/signup/phoneNumber`, `/auth/login/phoneNumber`, and `/auth/login/password`.
- Report creation sends multipart parts named `data` (JSON) and `photo` to `/citizen/report/create`.
- Citizen lists use `/citizen/myReports`, `/citizen/nearbyReports`, and `/citizen/verifiedReports`.
- The app only invokes the camera picker, but client-reported camera/VPN/mock-location signals are not tamper-proof. The Spring backend remains responsible for validation.
- API paths and payloads follow the current controllers/DTOs. The current backend does not expose notification/profile update APIs in this client.
