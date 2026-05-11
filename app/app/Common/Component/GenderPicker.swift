import SwiftUI

struct GenderPicker: View {

    @Binding var selectedGender: String

    var body: some View {
        Picker("성별", selection: $selectedGender) {
            Text("전체").tag("ALL")
            Text("여자").tag("FEMALE")
            Text("남자").tag("MALE")
        }
        .pickerStyle(.segmented)
        .padding(.horizontal, 12)
        .sensoryFeedback(.selection, trigger: selectedGender)
    }
}
