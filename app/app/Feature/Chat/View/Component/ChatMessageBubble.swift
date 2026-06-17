import SwiftUI
import Kingfisher

struct ChatMessageBubble: View {

    let message: ChatMessageRowResponse
    var onReact: ((String) -> Void)? = nil

    @Environment(AppRouter.self) private var router

    @State private var showImageFullScreen = false
    @State private var showVideoFullScreen = false
    @State private var showReactions = false

    private let imageSize: CGFloat = 200

    private var isMine: Bool {
        message.senderId == TokenStorage.shared.memberId
    }

    private var reactionChips: [(type: ReactionType, count: Int, isMine: Bool)] {
        let myId = TokenStorage.shared.memberId
        let order = ReactionType.allCases

        return Dictionary(grouping: message.reactions, by: \.type)
            .compactMap { key, value -> (type: ReactionType, count: Int, isMine: Bool)? in
                guard let type = key else { return nil }

                let mine = value.contains { $0.memberId == myId }
                return (type: type, count: value.count, isMine: mine)
            }
            .sorted { order.firstIndex(of: $0.type)! < order.firstIndex(of: $1.type)! }
    }

    private var jumboEmojiSize: CGFloat? {
        let content = message.content
        guard content.isOnlyEmoji else { return nil }
        switch content.emojiCount {
        case 1: return 56
        case 2: return 44
        case 3: return 36
        default: return nil
        }
    }

    var body: some View {
        HStack(alignment: .bottom, spacing: 4) {
            if isMine {
                Spacer()
                timeText
                bubble
            } else {
                bubble
                timeText
                Spacer()
            }
        }
    }

    @ViewBuilder
    private func bubbleText(background: some ShapeStyle, foreground: Color) -> some View {
        if message.type == .image {
            KFImage(URL(string: message.content))
                .placeholder {
                    ProgressView()
                        .frame(width: imageSize, height: imageSize)
                        .background(Color(.systemGray6))
                }
                .retry(maxCount: 3, interval: .seconds(2))
                .fade(duration: 0.2)
                .resizable()
                .scaledToFill()
                .frame(width: imageSize, height: imageSize)
                .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                .onTapGesture {
                    showImageFullScreen = true
                }
                .fullScreenCover(isPresented: $showImageFullScreen) {
                    ImageFullScreenView(url: message.content)
                }
        } else if message.type == .video {
            KFImage(URL(string: message.content))
                .placeholder {
                    ProgressView()
                        .frame(width: imageSize, height: imageSize)
                        .background(Color(.systemGray6))
                }
                .retry(maxCount: 3, interval: .seconds(2))
                .fade(duration: 0.2)
                .resizable()
                .blur(radius: 6)
                .scaledToFill()
                .frame(width: imageSize, height: imageSize)
                .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                .overlay {
                    Image(systemName: "play.fill")
                        .font(.title2)
                        .padding()
                        .foregroundColor(.primary)
                        .background(.ultraThinMaterial, in: Circle())
                }
                .onTapGesture {
                    router.push(AppRoute.chatMessageVideo(message.chatMessageId, message.content))
                }
        } else {
            if let jumboSize = jumboEmojiSize {
                Text(message.content)
                    .font(.system(size: jumboSize))
            } else {
                Text(attributedContent(foreground: foreground))
                    .font(.subheadline)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 10)
                    .background(background)
                    .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            }
        }
    }

    @ViewBuilder
    private var timeText: some View {
        if let date = message.createdAt.toISO8601Date() {
            Text(date.formatted(.dateTime.hour().minute()))
                .font(.caption2)
                .foregroundStyle(.secondary)
        }
    }

    private var bubble: some View {
        let background: Color = isMine ? .blue : Color(.systemGray5)
        let foreground: Color = isMine ? .white : .primary

        return bubbleText(background: background, foreground: foreground)
            .padding(.bottom, message.reactions.isEmpty ? 0 : 12)
            .overlay(alignment: isMine ? .bottomLeading : .bottomTrailing) {
                if !reactionChips.isEmpty {
                    HStack(spacing: 3) {
                        ForEach(reactionChips, id: \.type) { chip in
                            HStack(spacing: 2) {
                                Text(chip.type.emoji)
                                    .font(.caption)

                                if chip.count > 1 {
                                    Text("\(chip.count)")
                                        .font(.caption2)
                                        .foregroundStyle(.secondary)
                                }
                            }
                            .padding(.horizontal, 5)
                            .padding(.vertical, 3)
                            .background(
                                Capsule()
                                    .fill(Color(.systemBackground))
                                    .shadow(color: .black.opacity(0.15), radius: 1, y: 1)
                            )
                            .overlay(
                                Capsule()
                                    .strokeBorder(
                                        chip.isMine ? Color.accentColor.opacity(0.45) : Color.clear,
                                        lineWidth: 1
                                    )
                            )
                        }
                    }
                    .offset(x: isMine ? -4 : 4)
                    .transition(.scale.combined(with: .opacity))
                }
            }
            .onLongPressGesture {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                showReactions = true
            }
            .popover(isPresented: $showReactions) {
                let isMedia = message.type == .image || message.type == .video

                ChatReactionPicker(
                    onSelect: { emoji in
                        showReactions = false
                        onReact?(emoji)
                    },
                    onCopy: isMedia ? nil : {
                        showReactions = false
                        copyContent()
                    }
                )
            }
    }

    private func attributedContent(foreground: Color) -> AttributedString {
        var attributed = AttributedString(message.content)
        attributed.foregroundColor = foreground

        let text = message.content

        if let detector = try? NSDataDetector(
            types: NSTextCheckingResult.CheckingType.link.rawValue
        ) {
            let nsRange = NSRange(text.startIndex..<text.endIndex, in: text)
            for match in detector.matches(in: text, range: nsRange) {
                guard let url = match.url, let range = Range(match.range, in: text) else { continue }

                let lower = text.distance(from: text.startIndex, to: range.lowerBound)
                let upper = text.distance(from: text.startIndex, to: range.upperBound)
                let attrLower = attributed.index(attributed.startIndex, offsetByCharacters: lower)
                let attrUpper = attributed.index(attributed.startIndex, offsetByCharacters: upper)

                attributed[attrLower..<attrUpper].link = url
                attributed[attrLower..<attrUpper].underlineStyle = .single
                attributed[attrLower..<attrUpper].foregroundColor = foreground
            }
        }
        return attributed.characterWrappable()
    }

    private func copyContent() {
        UIPasteboard.general.string = message.content
        UINotificationFeedbackGenerator().notificationOccurred(.success)
        ToastManager.shared.show("내용이 복사되었습니다.", style: .success)
    }
}

// MARK: - Extension

private extension AttributedString {

    func characterWrappable() -> AttributedString {
        var result = AttributedString()

        for run in runs {
            let attrs = run.attributes
            let chars = Array(characters[run.range])

            for (index, ch) in chars.enumerated() {
                result.append(AttributedString(String(ch), attributes: attrs))

                if index < chars.count - 1 {
                    result.append(AttributedString("\u{200B}", attributes: attrs))
                }
            }
        }
        return result
    }
}

private extension Character {

    var isRealEmoji: Bool {
        guard let scalar = unicodeScalars.first else { return false }

        return scalar.properties.isEmojiPresentation || (scalar.properties.isEmoji && unicodeScalars.count > 1)
    }
}

private extension String {

    var isOnlyEmoji: Bool {
        !isEmpty && allSatisfy { $0.isRealEmoji || $0.isWhitespace }
    }
    var emojiCount: Int {
        reduce(0) { $0 + ($1.isRealEmoji ? 1 : 0) }
    }
}
