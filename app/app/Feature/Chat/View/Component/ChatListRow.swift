import SwiftUI

struct ChatListRow: View {

    let nickname: String
    let updatedAt: String
    let message: String
    let unreads: Int

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        HStack {
            Image(systemName: "person.fill")
                .font(.title)
                .padding()
                .foregroundStyle(Color(.systemGray4))
                .background(Color(.systemGray6))
                .clipShape(Circle())

            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(nickname)
                        .font(.subheadline.bold())

                    Spacer()

                    Text(updatedAt)
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                }

                HStack(alignment: .center) {
                    Text(message)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .lineLimit(2)

                    Spacer()

                    if unreads > 0 {
                        let label = unreads > 99 ? "+99" : "\(unreads)"
                        Text(label)
                            .font(.caption2.bold())
                            .foregroundStyle(.white)
                            .padding(.horizontal, unreads < 10 ? 0 : 6)
                            .padding(.vertical, 3)
                            .frame(minWidth: 20, minHeight: 20)
                            .background(.red, in: Capsule())
                    }
                }
            }
        }
        .listRowSeparator(.hidden)
        .listRowInsets(EdgeInsets(top: 4, leading: 12, bottom: 4, trailing: 12))
        .swipeActions(edge: .leading, allowsFullSwipe: false) {
            Button {
            } label: {
                Image(systemName: colorScheme == .dark ? "eyes" : "eyes.inverse")
            }
            .tint(.gray)
        }
        .swipeActions(edge: .trailing, allowsFullSwipe: false) {
            Button {
            } label: {
                Image(systemName: "trash.fill")
            }
            .tint(.red)
        }
    }
}
