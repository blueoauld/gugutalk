import SwiftUI

struct PointHistoryListRow: View {

    let description: String
    let type: PointType
    let point: Int64
    let createdAt: String
    
    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(description)
                    .font(.subheadline)

                if let date = createdAt.toISO8601Date() {
                    Text(date.formatted(date: .complete, time: .omitted))
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                }
            }

            Spacer()

            Text(type == .earn ? "+\(point)" : "-\(point)")
                .font(.subheadline)
                .fontWeight(.semibold)
                .foregroundStyle(type == .earn ? .red : .blue)
        }
        .padding(.vertical, 4)
        .padding(.horizontal)
    }
}
