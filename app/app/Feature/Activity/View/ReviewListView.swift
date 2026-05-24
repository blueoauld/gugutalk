import SwiftUI

struct ReviewListView: View {

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

                        VStack(alignment: .leading, spacing: 1) {
                            HStack {
                                Text("닉네임")
                                    .font(.subheadline.bold())

                                Spacer()

                                Text("2025.01.01")
                                    .font(.caption2)
                                    .foregroundStyle(.secondary)
                            }

                            Text("리뷰")
                                .font(.footnote)
                                .foregroundStyle(.secondary)
                                .lineLimit(1)

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
                    }
                    .padding(.vertical, 4)
                    .padding(.horizontal)
                    .listRowSeparator(.hidden)
                    .listRowInsets(EdgeInsets())
                }
            }
            .listStyle(.plain)
        }
        .navigationTitle("내가 쓴 리뷰 목록")
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview {
    NavigationStack {
        ReviewListView()
    }
}
