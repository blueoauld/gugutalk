import SwiftUI

struct RankViewPicker: View {

    @Binding var selectedRank: RankFilter

    var body: some View {
        Picker("랭킹", selection: $selectedRank) {
            ForEach(RankFilter.allCases, id: \.self) { it in
                Text(it.label).tag(it)
            }
        }
        .pickerStyle(.segmented)
        .padding(.horizontal, 12)
        .sensoryFeedback(.selection, trigger: selectedRank)
    }
}
