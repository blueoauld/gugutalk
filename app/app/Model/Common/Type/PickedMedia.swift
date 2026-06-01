import SwiftUI

struct PickedMedia: Identifiable, Equatable {

    let id = UUID()
    let kind: Kind

    enum Kind: Equatable {
        case image(UIImage)
        case video(url: URL, thumbnail: UIImage?)
    }

    var previewImage: UIImage? {
        switch kind {
        case .image(let image): return image
        case .video(_, let thumbnail): return thumbnail
        }
    }

    var isVideo: Bool {
        if case .video = kind { return true }

        return false
    }

    static func == (lhs: PickedMedia, rhs: PickedMedia) -> Bool {
        lhs.id == rhs.id
    }
}
