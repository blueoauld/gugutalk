import SwiftUI
import SwiftStomp

@Observable
class StompManager: SwiftStompDelegate {

    static let shared = StompManager()

    var stomp: SwiftStomp!

    private var subscriptions: Set<String> = []
    private var handlers: [String: (String) -> Void] = [:]

    private var oldStomp: SwiftStomp?

    func onConnect(swiftStomp : SwiftStomp, connectType : StompConnectType) {
        guard connectType == .toStomp else { return }

        if swiftStomp === stomp, let old = oldStomp {
            old.disconnect()
            oldStomp = nil
        }

        for destination in subscriptions {
            swiftStomp.subscribe(to: destination)
        }
    }

    func onDisconnect(swiftStomp : SwiftStomp, disconnectType : StompDisconnectType) {
        guard disconnectType == .fromStomp else { return }
    }

    func onMessageReceived(swiftStomp: SwiftStomp, message: Any?, messageId: String, destination: String, headers : [String : String]) {
        guard let text = message as? String else { return }

        Task { @MainActor in
            self.handlers[destination]?(text)
        }
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

        subscriptions.removeAll()
        handlers.removeAll()
    }

    func reconnect() {
        guard let accessToken = TokenStorage.shared.accessToken else { return }

        oldStomp = stomp
        oldStomp?.autoReconnect = false

        let url = URL(string: APIEnvironment.baseWS)!
        let headers = ["Authorization": "Bearer \(accessToken)"]

        let newStomp = SwiftStomp(host: url, headers: headers)
        newStomp.delegate = self
        stomp = newStomp
        newStomp.connect(autoReconnect: true)
    }

    func subscribe(to destination: String, onMessage: ((String) -> Void)? = nil) {
        subscriptions.insert(destination)

        if let onMessage {
            handlers[destination] = onMessage
        }

        if let stomp, stomp.connectionStatus == .fullyConnected {
            stomp.subscribe(to: destination)
        }
    }

    func subscribe<T: Decodable>(to destination: String, as type: T.Type, onMessage: @escaping (T) -> Void) {
        subscribe(to: destination) { text in
            guard let data = text.data(using: .utf8), let decoded = try? JSONDecoder().decode(T.self, from: data) else { return }

            onMessage(decoded)
        }
    }

    func unsubscribe(from destination: String) {
        subscriptions.remove(destination)
        handlers[destination] = nil

        if let stomp, stomp.connectionStatus == .fullyConnected {
            stomp.unsubscribe(from: destination)
        }
    }
}
