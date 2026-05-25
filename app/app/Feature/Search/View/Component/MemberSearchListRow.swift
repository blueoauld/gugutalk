import SwiftUI

struct MemberSearchListRow: View {

    let memberId: Int64
    let nickname: String
    let gender: Gender
    let age: Int
    let region: Region
    let updatedAt: String

    @Environment(AppRouter.self) private var router

    var body: some View {
        VStack {
            Button {
                router.push(.member(memberId))
            } label: {
                HStack {
                    Image(systemName: "person.fill")
                        .font(.title)
                        .padding()
                        .foregroundStyle(Color(.systemGray4))
                        .background(
                            Color(.systemGray6),
                            in: Circle()
                        )
                    
                    VStack(alignment: .leading, spacing: 4) {
                        HStack {
                            Text(nickname)
                                .font(.subheadline.bold())

                            Spacer()

                            if let date = updatedAt.toISO8601Date() {
                                Text(date.formatted(.relative(presentation: .named)))
                                    .font(.caption2)
                                    .foregroundStyle(.secondary)
                            }
                        }

                        HStack {
                            Text(gender.label)
                            
                            Text("·")
                            
                            Text("\(age)살")
                            
                            Text("·")
                            
                            Text(region.label)
                        }
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                    }
                }
            }
            .buttonStyle(.plain)
        }
        .padding(.vertical, 4)
        .padding(.horizontal)
    }
}
