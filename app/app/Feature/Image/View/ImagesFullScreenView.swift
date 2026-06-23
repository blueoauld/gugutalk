import SwiftUI
import Kingfisher
import LazyPager

struct ImagesFullScreenView: View {

    let images: [MemberImageResponse]
    
    @Binding var currentPage: Int
    
    @Environment(\.dismiss) private var dismiss
    
    @State private var backgroundOpacity: CGFloat = 1
    @State private var showsControls = true

    var body: some View {
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
            .onTap {
                withAnimation(.easeInOut(duration: 0.2)) {
                    showsControls.toggle()
                }
            }
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
            .opacity(showsControls ? backgroundOpacity : 0)
            .allowsHitTesting(showsControls)

            if images.count > 1 {
                pageIndicator
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
                    .opacity(showsControls ? backgroundOpacity : 0)
                    .allowsHitTesting(false)
            }
        }
    }
    
    private var pageIndicator: some View {
        HStack(spacing: 8) {
            ForEach(0..<images.count, id: \.self) { index in
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
