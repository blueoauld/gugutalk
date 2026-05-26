import SwiftUI

struct ReportMenu: View {

    @Binding var reportType: ReportType?

    var body: some View {
        VStack(spacing: 10) {
            ForEach(ReportType.allCases, id: \.self) { it in
                Button {
                    reportType = it
                } label: {
                    HStack {
                        Text(it.label)
                            .foregroundStyle(.primary)
                            .font(.body)

                        Spacer()

                        Image(systemName: reportType == it ? "checkmark.circle.fill" : "circle")
                            .foregroundColor(.secondary)
                    }
                    .padding(.vertical, 7)
                    .padding(.horizontal, 7)
                }
                .buttonStyle(.bordered)
                .tint(.secondary)
            }
        }
        .sensoryFeedback(.selection, trigger: reportType)
    }
}
