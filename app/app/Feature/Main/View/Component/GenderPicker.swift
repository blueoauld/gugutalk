import SwiftUI

struct GenderPicker: View {

    @Binding var selectedGender: GenderFilter

    var body: some View {
        Picker("성별", selection: $selectedGender) {
            ForEach(GenderFilter.allCases, id: \.self) { it in
                Text(it.label).tag(it)
            }
        }
    }
}
