import SwiftUI

struct ReviewActionBar: View {

    @Binding var review: String

    var onSubmit: () async -> Void

    var body: some View {
        TextField("리뷰 입력 (5P)", text: $review, axis: .vertical)
            .font(.subheadline)
            .lineLimit(1...5)
            .multilineTextAlignment(.leading)
            .padding(.leading)
            .padding(.trailing, 50)
            .padding(.vertical, 8)
            .frame(minHeight: 44)
            .overlay(alignment: .bottomTrailing) {
                Button {
                    Task {
                        await onSubmit()
                    }
                } label: {
                    Image(systemName: "paperplane.fill")
                        .foregroundColor(.white)
                        .frame(width: 36, height: 36)
                        .background(review.isEmpty ? Color(.systemGray3) : .blue)
                        .clipShape(Circle())
                }
                .padding(.trailing, 4)
                .padding(.bottom, 4)
                .disabled(review.isEmpty)
            }
            .glassEffect(
                .regular.tint(.clear).interactive(),
                in: .rect(cornerRadius: 20)
            )
            .autocorrectionDisabled(true)
            .textInputAutocapitalization(.never)
            .padding()
    }
}
