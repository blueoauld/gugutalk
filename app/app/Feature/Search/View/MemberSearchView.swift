import SwiftUI

struct MemberSearchView: View {

    @Environment(AppRouter.self) private var router

    @State private var nickname = ""

    var body: some View {
        VStack {
            List {
            }
            .listStyle(.plain)
        }
        .navigationTitle("회원 검색")
        .navigationBarTitleDisplayMode(.inline)
        .searchable(text: $nickname, prompt: "닉네임")
        .autocorrectionDisabled()
        .textInputAutocapitalization(.never)
    }
}

#Preview {
    NavigationStack {
        MemberSearchView()
            .environment(AppRouter())
    }
}
