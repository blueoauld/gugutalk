import SwiftUI

enum ProfileImage: Identifiable, Equatable {

    case existing(MemberImageResponse)
    case picked(PickedImage)

    var id: String {
        switch self {
        case .existing(let image): return "e-\(image.imageId)"
        case .picked(let image): return "p-\(image.id)"
        }
    }
}
