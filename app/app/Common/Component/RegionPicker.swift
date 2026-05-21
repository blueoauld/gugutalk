import SwiftUI

struct RegionPicker: View {

    @Binding var region: Region?

    @State private var showSheet = false

    var body: some View {
        Button {
            showSheet = true
        } label: {
            Text(region?.label ?? "지역")
                .foregroundStyle(region == nil ? Color(.systemGray2) : .primary)
                .padding(.horizontal, 14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .frame(height: 50)
                .background(
                    Color(.systemGray6),
                    in: RoundedRectangle(cornerRadius: 16)
                )
        }
        .buttonStyle(.plain)
        .sensoryFeedback(.selection, trigger: region)
        .sheet(isPresented: $showSheet) {
            List(Region.allCases, id: \.self) { it in
                Button(it.label) {
                    region = it
                    showSheet = false
                }
            }
            .padding(.vertical)
            .listStyle(.plain)
            .presentationDetents([.medium])
        }
    }
}
