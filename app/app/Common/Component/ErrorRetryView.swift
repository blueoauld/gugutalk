import SwiftUI

struct ErrorRetryView: View {

    let message: String
    let retry: () async -> Void

    var body: some View {
        ContentUnavailableView {
            Label("오류", systemImage: "exclamationmark.triangle")
        } description: {
            Text(message)
        } actions: {
            Button {
                Task {
                    await retry()
                }
            } label: {
                Text("재시도")
                    .padding(.horizontal)
            }
            .buttonStyle(.borderedProminent)
        }
    }
}
