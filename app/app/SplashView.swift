import SwiftUI

struct SplashView: View {

    @State private var scale = 0.8
    @State private var opacity = 0.0

    var body: some View {
        ZStack {
            Color(.systemBackground).ignoresSafeArea()

            Image(systemName: "bird.fill")
                .resizable()
                .scaledToFit()
                .frame(width: 120, height: 120)
                .scaleEffect(scale)
                .opacity(opacity)
                .foregroundStyle(.blue)
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.6)) {
                scale = 1.0
                opacity = 1.0
            }
        }
    }
}
