import SwiftUI
import Kingfisher
import LazyPager

struct ImageFullScreenView: View {

    let url: String

    @Environment(\.dismiss) private var dismiss

    @State private var backgroundOpacity: CGFloat = 1
    @State private var shareImage: Image?

    var body: some View {
        ZStack(alignment: .topLeading) {
            LazyPager(data: [url]) { url in
                KFImage(URL(string: url))
                    .placeholder {
                        ProgressView()
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .tint(.white)
                    }
                    .retry(maxCount: 3, interval: .seconds(2))
                    .fade(duration: 0.25)
                    .resizable()
                    .scaledToFit()
            }
            .zoomable(min: 1, max: 5)
            .onDismiss(backgroundOpacity: $backgroundOpacity) {
                dismiss()
            }
            .settings { config in
                config.dismissVelocity = 0.3
                config.dismissTriggerOffset = 0.2
            }
            .background(.black.opacity(backgroundOpacity))
            .background(ClearFullScreenBackground())
            .ignoresSafeArea()

            HStack {
                Button {
                    dismiss()
                } label: {
                    Image(systemName: "xmark")
                        .font(.title2)
                        .fontWeight(.semibold)
                        .foregroundStyle(.foreground)
                        .padding(11)
                        .glassEffect(in: .circle)
                }
                .padding(.horizontal)
                .opacity(backgroundOpacity)

                Spacer()

                if let shareImage {
                    ShareLink(
                        item: shareImage,
                        preview: SharePreview("이미지", image: shareImage)
                    ) {
                        Image(systemName: "square.and.arrow.up")
                            .font(.title2)
                            .foregroundStyle(.foreground)
                            .padding(11)
                            .glassEffect(in: .circle)
                    }
                    .padding(.horizontal)
                    .opacity(backgroundOpacity)
                }
            }
        }
        .task {
            guard
                let imageURL = URL(string: url),
                let result = try? await KingfisherManager.shared.retrieveImage(with: imageURL)
            else { return }

            shareImage = Image(uiImage: result.image)
        }
    }
}
