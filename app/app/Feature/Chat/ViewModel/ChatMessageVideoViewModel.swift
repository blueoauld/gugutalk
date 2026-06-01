import SwiftUI
import AVKit

@MainActor
@Observable
final class ChatMessageVideoViewModel {

    private let chatMessageService = ChatMessageService.shared

    private(set) var player: AVPlayer? = nil
    private(set) var localURL: URL? = nil
    private(set) var isLoading = false

    func getVideo(chatMessageId: Int64) async {
        guard !isLoading, player == nil else { return }

        let url = await loadVideoURL(chatMessageId: chatMessageId)

        if let url {
            await downloadForSharing(url: url)
        }
    }

    private func loadVideoURL(chatMessageId: Int64) async -> URL? {
        isLoading = true
        defer { isLoading = false }

        do {
            let response = try await chatMessageService.getVideo(chatMessageId: chatMessageId)

            guard let url = URL(string: response.url) else { return nil }

            player = AVPlayer(url: url)
            return url
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
            return nil
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
            return nil
        }
    }

    private func downloadForSharing(url: URL) async {
        if url.isFileURL {
            localURL = url
            return
        }

        do {
            let (tempURL, _) = try await URLSession.shared.download(from: url)

            let ext = url.pathExtension.isEmpty ? "mp4" : url.pathExtension
            let destination = FileManager.default.temporaryDirectory.appendingPathComponent("\(UUID().uuidString).\(ext)")

            try? FileManager.default.removeItem(at: destination)
            try FileManager.default.moveItem(at: tempURL, to: destination)

            localURL = destination
        } catch {
            print("공유용 파일 준비에 실패했습니다.")
        }
    }
}
