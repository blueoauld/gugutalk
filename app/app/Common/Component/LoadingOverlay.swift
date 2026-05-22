import SwiftUI

struct LoadingOverlay: View {

    var body: some View {
        ZStack {
            Color(.systemBackground).opacity(0.4)
                .ignoresSafeArea()

            ProgressView()
        }
    }
}
