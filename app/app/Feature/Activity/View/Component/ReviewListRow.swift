import SwiftUI

struct ReviewListRow: View {

    let nickname: String
    let content: String
    let createdAt: String

    var body: some View {
        VStack(alignment: .leading) {
            HStack {
                Text(nickname)
                    .font(.subheadline.bold())
                    .foregroundStyle(.primary)

                Spacer()

                if let date = createdAt.toISO8601Date() {
                    Text(date.formatted(date: .complete, time: .omitted))
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                }
            }

            Text(content)
                .font(.footnote)
                .foregroundStyle(.secondary)
        }
        .padding(.vertical, 12)
        .padding(.horizontal)
    }
}
