import SwiftUI

struct ChatStatusPicker: View {

    @Binding var selectedStatus: String

    var body: some View {
        Picker("상태", selection: $selectedStatus) {
            Text("전체").tag("ALL")
            Text("안읽음").tag("UNREAD")
        }
        .pickerStyle(.segmented)
        .padding(.horizontal, 12)
        .sensoryFeedback(.selection, trigger: selectedStatus)
    }
}
