import 'package:flutter_test/flutter_test.dart';
import 'package:roadguard_citizen/main.dart';

void main() {
  testWidgets('RoadGuard shows its location access gate at startup', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(const RoadGuardApp());
    await tester.pump();

    expect(
      find.text('Turn on location to see hazards near you and submit reports.'),
      findsOneWidget,
    );
  });
}
