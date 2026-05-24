import SwiftUI

struct LikeListView: View {

    var body: some View {
        VStack {
            List {
                ForEach(1...1000, id: \.self) { it in
                    HStack {
                        Image(systemName: "person.fill")
                            .font(.title)
                            .padding()
                            .foregroundStyle(Color(.systemGray4))
                            .background(Color(.systemGray6))
                            .clipShape(Circle())

                        VStack(alignment: .leading, spacing: 4) {
                            Text("닉네임")
                                .font(.subheadline.bold())

                            HStack {
                                Text("남자")

                                Text("·")

                                Text("20살")

                                Text("·")

                                Text("서울")
                            }
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                        }

                        Spacer()

                        Button {
                        } label: {
                            Image(systemName: "trash.fill")
                                .font(.title3)
                                .padding()
                                .foregroundColor(.white)
                                .background(
                                    .red,
                                    in: Circle()
                                )
                        }
                    }
                    .padding(.vertical, 4)
                    .padding(.horizontal)
                    .listRowSeparator(.hidden)
                    .listRowInsets(EdgeInsets())
                }
            }
            .listStyle(.plain)
        }
        .navigationTitle("좋아요 목록")
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview {
    NavigationStack {
        LikeListView()
    }
}
