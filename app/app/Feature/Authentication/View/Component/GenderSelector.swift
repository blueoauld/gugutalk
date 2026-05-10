import SwiftUI

struct GenderSelector: View {

    @Binding var gender: Gender

    var body: some View {
        HStack {
            ForEach(Gender.allCases) { it in
                Button {
                    gender = it
                } label: {
                    Text(it.rawValue)
                        .font(.default.bold())
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(
                            gender == it ? .blue : .gray,
                            in: RoundedRectangle(cornerRadius: 16)
                        )
                }
                .buttonStyle(.plain)
            }
        }
        .sensoryFeedback(.selection, trigger: gender)
    }
}
