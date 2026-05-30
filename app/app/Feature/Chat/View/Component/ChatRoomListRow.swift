import SwiftUI
import Kingfisher

struct ChatRoomListRow: View {

    let nickname: String
    let profileUrl: String?
    let updatedAt: String
    let message: String
    let unreadCount: Int64

    private let imageSize: CGFloat = 60

    var body: some View {
        HStack {
            KFImage(profileUrl.flatMap { URL(string: $0) })
                .placeholder {
                    Image(systemName: "person.fill")
                        .font(.largeTitle)
                        .foregroundStyle(Color(.systemGray4))
                        .frame(width: imageSize, height: imageSize)
                        .background(Color(.systemGray6))
                        .clipShape(Circle())
                }
                .retry(maxCount: 3, interval: .seconds(2))
                .fade(duration: 0.25)
                .resizable()
                .scaledToFill()
                .frame(width: imageSize, height: imageSize)
                .clipShape(Circle())

            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(nickname)
                        .font(.subheadline.bold())

                    Spacer()

                    if let date = updatedAt.toISO8601Date() {
                        Text(display(for: date))
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }
                }

                HStack(alignment: .center) {
                    Text(message)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .lineLimit(2)

                    Spacer()

                    if unreadCount > 0 {
                        let label = unreadCount > 99 ? "+99" : "\(unreadCount)"

                        Text(label)
                            .font(.caption2.bold())
                            .foregroundStyle(.white)
                            .padding(.horizontal, unreadCount < 10 ? 0 : 6)
                            .padding(.vertical, 3)
                            .frame(minWidth: 20, minHeight: 20)
                            .background(.red, in: Capsule())
                    }
                }
            }
        }
        .padding(.vertical, 4)
        .padding(.horizontal)
    }

    private func display(for date: Date) -> String {
        let now = Calendar.current

        if now.isDateInToday(date) {
            return date.formatted(.dateTime.hour().minute())
        } else if now.isDate(date, equalTo: Date(), toGranularity: .year) {
            return date.formatted(.dateTime.month().day())
        } else {
            return date.formatted(.dateTime.year().month().day())
        }
    }
}
