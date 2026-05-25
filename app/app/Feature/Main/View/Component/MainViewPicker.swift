import SwiftUI

struct MainViewPicker: View {

    @Binding var selectedView: ViewFilter

    var body: some View {
        Picker("화면", selection: $selectedView) {
            ForEach(ViewFilter.allCases, id: \.self) { it in
                Text(it.label).tag(it)
            }
        }
        .pickerStyle(.segmented)
        .padding(.horizontal, 12)
        .sensoryFeedback(.selection, trigger: selectedView)
    }
}
