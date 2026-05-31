import SwiftUI

struct ChatMessageBubble: View {

    let message: ChatMessageRowResponse

    private var isMine: Bool {
        message.senderId == TokenStorage.shared.memberId
    }

    var body: some View {
        HStack(alignment: .bottom, spacing: 4) {
            if isMine {
                Spacer()

                timeText

                bubbleText(background: Color.blue, foreground: .white)
            } else {
                bubbleText(background: Color(.systemGray5), foreground: .primary)

                timeText

                Spacer()
            }
        }
    }

    private func bubbleText(background: some ShapeStyle, foreground: Color) -> some View {
        Text(attributedContent(foreground: foreground))
            .font(.subheadline)
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(background)
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            .onLongPressGesture {
                UIPasteboard.general.string = message.content
                UINotificationFeedbackGenerator().notificationOccurred(.success)

                ToastManager.shared.show("내용이 복사되었습니다.", style: .success)
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
}

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
