import SwiftUI

struct ReportMenu: View {

    var body: some View {
        VStack(spacing: 10) {
            ForEach(ReportType.allCases, id: \.self) { it in
                Button {

                } label: {
                    HStack {
                        Text(it.label)
                            .foregroundStyle(.primary)
                            .font(.body)

                        Spacer()

                        Image(systemName: "circle")
                            .foregroundColor(.secondary)
                    }
                    .padding(.vertical, 7)
                    .padding(.horizontal, 7)
                }
                .buttonStyle(.bordered)
                .tint(.secondary)
            }
        }
    }
}
