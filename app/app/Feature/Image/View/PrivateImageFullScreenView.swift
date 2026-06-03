import SwiftUI
import Kingfisher
import LazyPager

struct PrivateImageFullScreenView: View {

    let memberId: Int64

    @Environment(\.dismiss) private var dismiss

    @State private var vm = PrivateImageFullScreenViewModel()
    @State private var currentPage = 0
    @State private var backgroundOpacity: CGFloat = 1

    var body: some View {
        VStack {
            content
        }
        .task {
            await vm.getPrivateImages(memberId: memberId)
        }
    }

    @ViewBuilder
    private var content: some View {
        switch vm.state {
        case .idle:
            Spacer()
        case .loading:
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        case .data:
            if let phone = vm.phone, let images = vm.images {
                if images.isEmpty {
                    ContentUnavailableView("내역 없음", systemImage: "tray")
                        .containerRelativeFrame([.horizontal, .vertical])
                        .overlay(alignment: .topLeading) {
                            Button {
                                dismiss()
                            } label: {
                                Image(systemName: "xmark")
                                    .font(.title2)
                                    .fontWeight(.semibold)
                                    .foregroundStyle(.foreground)
                                    .padding(11)
                                    .glassEffect()
                            }
                            .padding(.horizontal)
                        }
                } else {
                    ZStack(alignment: .topLeading) {
                        LazyPager(data: images, page: $currentPage) { image in
                            KFImage(URL(string: image.url))
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
                        .screenshotProtected()
                        .watermark("구구톡 - \(phone)", style: .tiled, opacity: 0.1)

                        Button {
                            dismiss()
                        } label: {
                            Image(systemName: "xmark")
                                .font(.title2)
                                .fontWeight(.semibold)
                                .foregroundStyle(.foreground)
                                .padding(11)
                                .glassEffect()
                        }
                        .padding(.horizontal)
                        .opacity(backgroundOpacity)

                        if images.count > 1 {
                            pageIndicator(count: images.count)
                                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
                                .opacity(backgroundOpacity)
                                .allowsHitTesting(false)
                        }
                    }
                }
            }
        case .error(let message):
            ContentUnavailableView {
                Label("오류", systemImage: "exclamationmark.triangle")
            } description: {
                Text(message)
            }
            .overlay(alignment: .topLeading) {
                Button {
                    dismiss()
                } label: {
                    Image(systemName: "xmark")
                        .font(.title2)
                        .fontWeight(.semibold)
                        .foregroundStyle(.foreground)
                        .padding(11)
                        .glassEffect()
                }
                .padding(.horizontal)
            }
        }
    }

    private func pageIndicator(count: Int) -> some View {
        HStack(spacing: 8) {
            ForEach(0..<count, id: \.self) { index in
                Circle()
                    .fill(.white)
                    .opacity(currentPage == index ? 1.0 : 0.4)
                    .frame(width: 7, height: 7)
                    .scaleEffect(currentPage == index ? 1.15 : 1.0)
                    .animation(.easeInOut(duration: 0.2), value: currentPage)
            }
        }
        .compositingGroup()
        .blendMode(.difference)
    }
}
