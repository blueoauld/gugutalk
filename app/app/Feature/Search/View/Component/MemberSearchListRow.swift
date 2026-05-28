import SwiftUI
import Kingfisher

struct MemberSearchListRow: View {

    let memberId: Int64
    let nickname: String
    let profileUrl: String?
    let gender: Gender
    let age: Int
    let region: Region
    let updatedAt: String

    @Environment(AppRouter.self) private var router

    private let imageSize: CGFloat = 60

    var body: some View {
        VStack {
            Button {
                router.push(.member(memberId))
            } label: {
                HStack {
                    KFImage(profileUrl.flatMap { URL(string: $0) })
                        .placeholder {
                            Image(systemName: "person.fill")
                                .font(.title)
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
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
        }
        .padding(.vertical, 4)
        .padding(.horizontal)
    }
}
