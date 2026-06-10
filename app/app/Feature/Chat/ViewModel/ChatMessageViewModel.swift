import SwiftUI

enum ChatMessageViewState {

    case idle
    case loading
    case empty
    case data
    case error(String)
}

@MainActor
@Observable
final class ChatMessageViewModel {

    private struct ProcessedMedia {

        let type: MessageType
        let media: Data
        let mediaContentType: String
        let thumbnail: Data?
        let thumbnailContentType: String?
    }

    private let chatMessageService = ChatMessageService.shared
    private let chatRoomService = ChatRoomService.shared
    private let r2Service = R2Service.shared

    var state: ChatMessageViewState = .idle
    var chatMessages: [ChatMessageRowResponse] = []
    var message: String = ""

    private(set) var isPaging = false
    private(set) var isUploading = false

    private var hasLoad = false
    private var cursor = CursorRequest()
    var hasNext: Bool { cursor.hasNext }

    private func topic(_ chatRoomId: Int64) -> String {
        "/topic/chat-rooms/\(chatRoomId)"
    }

    func load(chatRoomId: Int64) async {
        guard !hasLoad else { return }
        hasLoad = true

        state = .loading
        await fetch(chatRoomId: chatRoomId)
    }

    func loadNext(chatRoomId: Int64) async -> Result<Void, Error>? {
        guard !isPaging, hasNext else { return nil }

        isPaging = true
        defer { isPaging = false }

        do {
            let response = try await chatMessageService.gets(
                chatRoomId: chatRoomId,
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            chatMessages.append(contentsOf: response.payload)
            return .success(())
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return nil
            }

            return .failure(error)
        }
    }

    func syncLatest(chatRoomId: Int64) async {
        guard hasLoad else {
            await load(chatRoomId: chatRoomId)
            return
        }

        let latest = CursorRequest()

        do {
            let response = try await chatMessageService.gets(
                chatRoomId: chatRoomId,
                cursorId: latest.cursorId,
                cursorDateAt: latest.cursorDateAt,
                size: 50
            )

            let existingIds = Set(chatMessages.map(\.chatMessageId))
            let missed = response.payload.filter { !existingIds.contains($0.chatMessageId) }

            guard !missed.isEmpty else { return }

            chatMessages.append(contentsOf: missed)
            chatMessages.sort { $0.chatMessageId > $1.chatMessageId }

            if case .empty = state { state = .data }
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return
            }

            print(error.userMessage)
        }
    }

    func send(chatRoomId: Int64) async -> Result<Void, Error>? {
        guard let memberId = TokenStorage.shared.memberId else {
            return .failure(
                APIError.server(
                    code: "INTERNAL_CLIENT_ERROR",
                    message: "회원 ID를 찾을 수 없습니다.",
                    statusCode: 400
                )
            )
        }

        let content = message.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !content.isEmpty else { return nil }

        let clientMessageId = UUID().uuidString

        let optimistic = ChatMessageRowResponse.optimistic(
            clientMessageId: clientMessageId,
            senderId: memberId,
            content: content
        )

        chatMessages.insert(optimistic, at: 0)
        if case .empty = state { state = .data }
        message = ""

        do {
            try await chatMessageService.send(
                chatRoomId: chatRoomId,
                content: content,
                clientMessageId: clientMessageId
            )
            updateStatus(clientMessageId: clientMessageId, to: .sent)
            return .success(())
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return nil
            }

            updateStatus(clientMessageId: clientMessageId, to: .failed)
            return .failure(error)
        }
    }

    func upload(chatRoomId: Int64, media: [PickedMedia]) async -> Result<Void, Error>? {
        guard !isUploading, !media.isEmpty else { return nil }

        isUploading = true
        defer { isUploading = false }

        do {
            // 1. 데이터 준비 (이미지 압축 / 비디오 데이터 + 썸네일 압축)
            let pickedMedia = media
            let processed: [ProcessedMedia] = try await Task.detached(priority: .userInitiated) {
                try pickedMedia.map { item in
                    switch item.kind {
                    case .image(let image):
                        let data = try Self.compressedJPEG(image)

                        return ProcessedMedia(
                            type: .image,
                            media: data,
                            mediaContentType: "image/jpeg",
                            thumbnail: nil,
                            thumbnailContentType: nil
                        )

                    case .video(let url, let thumbnail):
                        let videoData = try Data(contentsOf: url)

                        guard let thumbnail else {
                            throw APIError.server(
                                code: "INTERNAL_CLIENT_ERROR",
                                message: "비디오 썸네일이 없습니다.",
                                statusCode: 400
                            )
                        }

                        let thumbData = try Self.compressedJPEG(thumbnail)

                        return ProcessedMedia(
                            type: .video,
                            media: videoData,
                            mediaContentType: "video/mp4",
                            thumbnail: thumbData,
                            thumbnailContentType: "image/jpeg"
                        )
                    }
                }
            }.value

            // 2. 업로드할 파트들로 평탄화하면서, 항목별 슬롯(영상/썸네일 인덱스) 기록
            var parts: [(data: Data, contentType: String)] = []
            var slots: [(mediaIndex: Int, thumbnailIndex: Int?)] = []

            for item in processed {
                let mediaIndex = parts.count
                parts.append((item.media, item.mediaContentType))

                var thumbnailIndex: Int? = nil
                if let thumbnail = item.thumbnail, let ct = item.thumbnailContentType {
                    thumbnailIndex = parts.count
                    parts.append((thumbnail, ct))
                }
                slots.append((mediaIndex, thumbnailIndex))
            }

            // 3. 업로드 URL 생성
            let requests = UploadUrlRequests(
                urls: parts.map { UploadUrlRequest(contentType: $0.contentType) }
            )
            let responses = try await chatMessageService.createUploadUrls(chatRoomId: chatRoomId, urls: requests)

            // 4. R2 병렬 업로드
            try await withThrowingTaskGroup(of: Void.self) { group in
                for (part, response) in zip(parts, responses.urls) {
                    group.addTask {
                        try await self.r2Service.upload(
                            data: part.data,
                            url: response.url,
                            contentType: part.contentType
                        )
                    }
                }
                try await group.waitForAll()
            }

            // 5.슬롯으로 되짚어 항목별 요청 구성
            let media = zip(processed, slots).map { item, slot -> ChatMessageMediaCreateRequest in
                let main = responses.urls[slot.mediaIndex]
                let thumbnail = slot.thumbnailIndex.map { responses.urls[$0] }

                return ChatMessageMediaCreateRequest(
                    type: item.type,
                    url: main.url,
                    key: main.key,
                    thumbnailUrl: thumbnail?.url,
                    thumbnailKey: thumbnail?.key
                )
            }

            // 6. 요청
            let request = ChatMessageMediaUploadRequest(media: media)
            try await chatMessageService.upload(chatRoomId: chatRoomId, request: request)
            return .success(())
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return nil
            }

            return .failure(error)
        }
    }

    func read(chatRoomId: Int64) async {
        try? await chatRoomService.read(chatRoomId: chatRoomId)
    }

    private func fetch(chatRoomId: Int64) async {
        cursor.reset()
        chatMessages = []

        do {
            let response = try await chatMessageService.gets(
                chatRoomId: chatRoomId,
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 50
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            chatMessages = response.payload
            state = chatMessages.isEmpty ? .empty : .data
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return
            }

            state = .error(error.userMessage)
        }
    }

    private func updateStatus(clientMessageId: String, to status: SendStatus) {
        guard let idx = chatMessages.firstIndex(where: { $0.clientMessageId == clientMessageId }) else { return }

        chatMessages[idx].status = status
    }

    nonisolated private static func compressedJPEG(_ image: UIImage) throws -> Data {
        let resized = image.resized(toMaxDimension: 1024)

        guard let data = resized.jpegData(compressionQuality: 0.8) else {
            throw APIError.server(
                code: "INTERNAL_CLIENT_ERROR",
                message: "이미지 압축에 실패했습니다.",
                statusCode: 400
            )
        }
        return data
    }

    // MARK: - STOMP
    func subscribe(chatRoomId: Int64) {
        StompManager.shared.subscribe(
            to: topic(chatRoomId),
            as: ChatMessageRowResponse.self
        ) { [weak self] message in
            guard let self else { return }

            self.receive(message)
            Task {
                await self.read(chatRoomId: chatRoomId)
            }
        }
    }

    func unsubscribe(chatRoomId: Int64) {
        StompManager.shared.unsubscribe(from: topic(chatRoomId))
    }

    private func receive(_ message: ChatMessageRowResponse) {
        if let cid = message.clientMessageId, let idx = chatMessages.firstIndex(where: { $0.clientMessageId == cid }) {
            var updated = message
            updated.status = .sent
            chatMessages[idx] = updated
            return
        }

        guard !chatMessages.contains(where: { $0.chatMessageId == message.chatMessageId }) else { return }

        chatMessages.insert(message, at: 0)
        if case .empty = state { state = .data }
    }
}
