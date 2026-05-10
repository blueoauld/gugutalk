import SwiftUI

@MainActor
@Observable
final class SetupViewModel {

    var nickname = ""
    var birthYear = ""
    var region: Region? = nil
    var bio = ""

    var enabled: Bool {
        nickname.count >= 2 && birthYear.count == 4 && region != nil
    }
}
