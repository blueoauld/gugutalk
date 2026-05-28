import SwiftUI
import Kingfisher

struct ActivityListRow: View {

    let memberId: Int64
    let nickname: String
    let profileUrl: String?
    let gender: Gender
    let age: Int
    let region: Region
    var onDelete: () async -> Void

    @Environment(AppRouter.self) private var router

    @State private var deleteTrigger = false

    private let imageSize: CGFloat = 60
    
    var body: some View {
        HStack {
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
                        Text(nickname)
                            .font(.subheadline.bold())

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

                    Spacer()
                }
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)

            Button {
                deleteTrigger.toggle()

                Task {
                    await onDelete()
                }
            } label: {
                Image(systemName: "trash.fill")
                    .font(.subheadline)
                    .padding()
                    .foregroundColor(.white)
                    .background(
                        .red,
                        in: Circle()
                    )
            }
            .buttonStyle(.plain)
            .sensoryFeedback(.selection, trigger: deleteTrigger)
        }
        .padding(.vertical, 4)
        .padding(.horizontal)
    }
}
