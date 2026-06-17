import SwiftUI

struct ChatReactionPicker: View {

    let onSelect: (String) -> Void
    var onCopy: (() -> Void)? = nil

    private let reactions = ["❤️", "👍", "✅", "😂", "😮", "😢"]

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 14) {
                ForEach(reactions, id: \.self) { emoji in
                    Button {
                        onSelect(emoji)
                    } label: {
                        Text(emoji)
                            .font(.system(size: 30))
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            if let onCopy {
                Divider()

                Button {
                    onCopy()
                } label: {
                    Label("복사", systemImage: "doc.on.doc")
                        .font(.subheadline)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                }
                .buttonStyle(.plain)
            }
        }
        .presentationCompactAdaptation(.popover)
    }
}
