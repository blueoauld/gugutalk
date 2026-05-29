import SwiftUI

struct ChatStatusPicker: View {

    @Binding var selectedStatus: ChatRoomStatusFilter

    var body: some View {
        Picker("상태", selection: $selectedStatus) {
            ForEach(ChatRoomStatusFilter.allCases, id: \.self) { it in
                Text(it.label).tag(it)
            }
        }
        .pickerStyle(.segmented)
        .padding(.horizontal, 12)
        .sensoryFeedback(.selection, trigger: selectedStatus)
    }
}
