import SwiftUI

struct MemberListRow: View {

    let nickname: String
    let updatedAt: String
    let comment: String
    let gender: Gender
    let age: Int
    let likes: Int
    let unlikes: Int
    let reviews: Int
    let region: Region

    @State private var showMessage = false
    @State private var message = ""

    var body: some View {
        HStack {
            Image(systemName: "person.fill")
                .font(.title)
                .padding()
                .foregroundStyle(Color(.systemGray4))
                .background(Color(.systemGray6))
                .clipShape(Circle())

            VStack(alignment: .leading, spacing: 1) {
                HStack {
                    Text(nickname)
                        .font(.subheadline.bold())

                    Spacer()

                    Text(updatedAt)
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                }

                Text(comment)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)

                HStack {
                    VStack(alignment: .leading) {
                        HStack {
                            Text(gender.rawValue)

                            Text("·")

                            Text("\(age)살")
                        }
                        .font(.caption2)
                        .foregroundStyle(.secondary)

                        HStack {
                            HStack(spacing: 3) {
                                Image(systemName: "heart.fill")
                                    .foregroundStyle(.red)

                                Text("\(likes)")
                            }

                            Text("·")

                            HStack(spacing: 3) {
                                Image(systemName: "heart.slash.fill")
                                    .foregroundStyle(.blue)

                                Text("\(unlikes)")
                            }

                            Text("·")

                            HStack(spacing: 3) {
                                Image(systemName: "star.fill")
                                    .foregroundStyle(.yellow)

                                Text("\(reviews)")
                            }
                        }
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                    }

                    Spacer()

                    Text(region.rawValue)
                        .font(.caption2)
                        .foregroundStyle(.primary)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(Color(.systemGray6), in: Capsule())
                }
            }
        }
        .listRowSeparator(.hidden)
        .listRowInsets(EdgeInsets(top: 4, leading: 12, bottom: 4, trailing: 12))
        .swipeActions(edge: .trailing, allowsFullSwipe: false) {
            Button {
                showMessage = true
            } label: {
                Image(systemName: "envelope.fill")
            }
            .tint(.blue)
        }
        .alert("쪽지", isPresented: $showMessage) {
            TextField("내용", text: $message)

            Button("전송") {
            }
            Button("취소", role: .cancel) { }
        }
    }
}
