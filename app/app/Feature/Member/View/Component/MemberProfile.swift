import SwiftUI
import Kingfisher

struct MemberProfile: View {

    let member: MemberGetResponse

    @State private var currentPage = 0
    @State private var showImageFullScreen = false

    var body: some View {
        VStack {
            TabView(selection: $currentPage) {
                if member.images.isEmpty {
                    Image(systemName: "person.fill")
                        .font(.largeTitle)
                        .padding()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .foregroundStyle(Color(.systemGray4))
                        .background(Color(.systemGray6))
                        .tag(0)
                } else {
                    ForEach(Array(member.images.enumerated()), id: \.element.imageId) { index, image in
                        Color.clear
                            .overlay {
                                KFImage(URL(string: image.url))
                                    .placeholder {
                                        ProgressView()
                                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                                            .background(Color(.systemGray6))
                                    }
                                    .retry(maxCount: 3, interval: .seconds(2))
                                    .fade(duration: 0.25)
                                    .resizable()
                                    .scaledToFill()
                            }
                            .clipped()
                            .tag(index)
                            .onTapGesture {
                                showImageFullScreen = true
                            }
                    }
                }
            }
            .tabViewStyle(.page)
            .aspectRatio(4/3, contentMode: .fit)
            .fullScreenCover(isPresented: $showImageFullScreen) {
                ImagesFullScreenView(
                    images: member.images,
                    currentPage: $currentPage
                )
            }

            VStack(alignment: .leading) {
                VStack {
                    HStack {
                        Text(member.nickname)
                            .font(.title3.bold())

                        Spacer()

                        if let date = member.updatedAt.toISO8601Date() {
                            Text(date.formatted(.relative(presentation: .named)))
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }

                    HStack {
                        Text(member.gender.label)
                        Text("·")
                        Text("\(member.age)살")
                        Text("·")
                        Text(member.region.label)

                        Spacer()
                    }
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                    HStack {
                        HStack(spacing: 3) {
                            Image(systemName: "heart.fill")
                                .foregroundStyle(.red)

                            Text("\(member.likes)")
                        }

                        Text("·")

                        HStack(spacing: 3) {
                            Image(systemName: "heart.slash.fill")
                                .foregroundStyle(.blue)

                            Text("\(member.unlikes)")
                        }

                        Text("·")

                        HStack(spacing: 3) {
                            Image(systemName: "star.fill")
                                .foregroundStyle(.yellow)

                            Text("\(member.reviews)")
                        }

                        Spacer()
                    }
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                }
                .padding(.bottom)

                VStack {
                    Text(member.bio.byCharWrapping)
                        .font(.subheadline)
                        .foregroundColor(.primary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding()
                        .background(
                            Color(.systemGray6),
                            in: RoundedRectangle(cornerRadius: 16)
                        )
                }
            }
            .padding()
        }
    }
}
