import SwiftUI

struct ChatDateSeparator: View {

    let date: String

    var body: some View {
        if let date = date.toISO8601Date() {
            Text(date.formatted(.dateTime.year().month().day().weekday(.wide)))
                .font(.caption2)
                .foregroundStyle(.secondary)
                .padding(.horizontal)
                .padding(.vertical, 4)
                .background(Color(.systemGray6), in: Capsule())
                .frame(maxWidth: .infinity)
        }
    }
}
