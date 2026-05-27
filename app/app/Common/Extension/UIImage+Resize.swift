import SwiftUI

extension UIImage {

    nonisolated func resized(toMaxDimension maxDimension: CGFloat) -> UIImage {
        let width = size.width
        let height = size.height

        guard width > maxDimension || height > maxDimension else { return self }

        let scale = maxDimension / max(width, height)
        let newSize = CGSize(width: width * scale, height: height * scale)

        let format = UIGraphicsImageRendererFormat()
        format.scale = 1.0
        format.opaque = true

        let renderer = UIGraphicsImageRenderer(size: newSize, format: format)
        return renderer.image { _ in
            self.draw(in: CGRect(origin: .zero, size: newSize))
        }
    }
}
