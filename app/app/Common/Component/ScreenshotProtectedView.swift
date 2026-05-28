import SwiftUI
import UIKit

struct ScreenshotProtectedView<Content: View>: UIViewRepresentable {

    private let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    func makeUIView(context: Context) -> UIView {
        let secureField = UITextField()
        secureField.isSecureTextEntry = true

        guard let secureCanvas = secureField.layer.sublayers?.first?.delegate as? UIView else {
            return UIView()
        }

        secureCanvas.subviews.forEach { $0.removeFromSuperview() }
        secureCanvas.isUserInteractionEnabled = true
        secureCanvas.backgroundColor = .clear

        let hosting = UIHostingController(rootView: content)
        hosting.view.backgroundColor = .clear
        hosting.view.translatesAutoresizingMaskIntoConstraints = false

        if #available(iOS 16.4, *) {
            hosting.safeAreaRegions = []
        }

        secureCanvas.addSubview(hosting.view)

        NSLayoutConstraint.activate([
            hosting.view.topAnchor.constraint(equalTo: secureCanvas.topAnchor),
            hosting.view.bottomAnchor.constraint(equalTo: secureCanvas.bottomAnchor),
            hosting.view.leadingAnchor.constraint(equalTo: secureCanvas.leadingAnchor),
            hosting.view.trailingAnchor.constraint(equalTo: secureCanvas.trailingAnchor)
        ])

        context.coordinator.hostingController = hosting
        return secureCanvas
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        context.coordinator.hostingController?.rootView = content
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    class Coordinator {
        var hostingController: UIHostingController<Content>?
    }
}

extension View {

    func screenshotProtected() -> some View {
        ScreenshotProtectedView { self }
            .ignoresSafeArea()
    }
}
