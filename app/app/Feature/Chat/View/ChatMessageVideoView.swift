import SwiftUI
import AVKit
import Kingfisher

struct ChatMessageVideoView: View {

    let chatMessageId: Int64
    let thumbnailUrl: String

    @State private var vm = ChatMessageVideoViewModel()
    @State private var shareImage: Image?

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            VStack {
                if let player = vm.player {
                    VideoPlayer(player: player)
                }
            }
            .padding()
        }
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                if let localURL = vm.localURL, let shareImage = shareImage {
                    ShareLink(
                        item: localURL,
                        preview: SharePreview("동영상", image: shareImage)
                    ) {
                        Image(systemName: "square.and.arrow.up")
                    }
                }
            }
        }
        .task {
            await vm.getVideo(chatMessageId: chatMessageId)

            guard
                let imageURL = URL(string: thumbnailUrl),
                let result = try? await KingfisherManager.shared.retrieveImage(with: imageURL)
            else { return }

            shareImage = Image(uiImage: result.image)
        }
        .overlay {
            if vm.isLoading {
                ProgressView()
            }
        }
    }
}
