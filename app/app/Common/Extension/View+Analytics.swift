import SwiftUI
import FirebaseAnalytics

extension View {
    
    func trackScreen(_ name: String) -> some View {
        self.onAppear {
            Analytics.logEvent(AnalyticsEventScreenView, parameters: [
                AnalyticsParameterScreenName: name,
                AnalyticsParameterScreenClass: name
            ])
        }
    }
}
