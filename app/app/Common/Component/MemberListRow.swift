import SwiftUI

struct MemberListRow: View {

    let nickname: String
    let updatedAt: String
    let comment: String
    let gender: Gender
    let age: Int
    let likes: Int
    let unlikes: Int
    let region: Region

    @State private var showMessage = false
    @State private var message = ""

    var body: some View {
        HStack {
            Image(systemName: "person.fill")
                .font(.title)
                .padding()
                .foregroundColor(Color(.systemGray4))
                .background(Color(.systemGray6))
                .clipShape(Circle())

            VStack(alignment: .leading) {
                HStack {
                    Text(nickname)
                        .font(.headline)

                    Spacer()

                    Text(updatedAt)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }

                Text(comment)
                    .font(.subheadline)
                    .foregroundColor(.secondary)

                HStack {
                    Text((gender.rawValue))

                    Text("·")

                    Text("\(age)살")

                    Text("·")

                    HStack(spacing: 5) {
                        Image(systemName: "heart.fill")
                            .foregroundColor(.red)

                        Text("\(likes)")
                    }

                    Text("·")

                    HStack(spacing: 5) {
                        Image(systemName: "heart.slash.fill")
                            .foregroundColor(.blue)

                        Text("\(unlikes)")
                    }

                    Spacer()

                    Text(region.rawValue)
                }
                .font(.caption)
                .foregroundColor(.secondary)
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
