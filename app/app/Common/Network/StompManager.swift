import SwiftUI
import SwiftStomp

@Observable
class StompManager: SwiftStompDelegate {

    static let shared = StompManager()

    var stomp: SwiftStomp!

    func onConnect(swiftStomp : SwiftStomp, connectType : StompConnectType) {
        guard connectType == .toStomp else { return }
    }

    func onDisconnect(swiftStomp : SwiftStomp, disconnectType : StompDisconnectType) {
        guard disconnectType == .fromStomp else { return }
    }

    func onMessageReceived(swiftStomp: SwiftStomp, message: Any?, messageId: String, destination: String, headers : [String : String]) {

    }

    func onReceipt(swiftStomp : SwiftStomp, receiptId : String) {

    }

    func onError(swiftStomp : SwiftStomp, briefDescription : String, fullDescription : String?, receiptId : String?, type : StompErrorType) {

    }

    func connect() {
        if let stomp, stomp.connectionStatus != .socketDisconnected {
            return
        }

        guard let accessToken = TokenStorage.shared.accessToken else { return }

        let url = URL(string: APIEnvironment.baseWS)!
        let headers = ["Authorization": "Bearer \(accessToken)"]

        stomp = SwiftStomp(host: url, headers: headers)
        stomp.delegate = self
        stomp.connect(autoReconnect: true)
    }

    func disconnect() {
        stomp?.autoReconnect = false
        stomp?.disconnect()
        stomp = nil
    }
}
