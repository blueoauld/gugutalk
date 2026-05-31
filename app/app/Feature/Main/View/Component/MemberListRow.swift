import SwiftUI
import Kingfisher

struct MemberListRow: View {

    let profileUrl: String?
    let nickname: String
    let updatedAt: String
    let comment: String
    let gender: Gender
    let age: Int
    let likes: Int64
    let unlikes: Int64
    let reviews: Int64
    let region: Region

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

            VStack(alignment: .leading, spacing: 1) {
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

                Text(comment.byCharWrapping)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)

                HStack {
                    VStack(alignment: .leading, spacing: 1) {
                        HStack {
                            Text(gender.label)

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

                    Text(region.label)
                        .font(.caption2)
                        .foregroundStyle(.primary)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(Color(.systemGray6), in: Capsule())
                }
            }
        }
        .padding(.vertical, 4)
        .padding(.horizontal)
    }
}
