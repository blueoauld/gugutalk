import SwiftUI

struct LocationView: View {

    @Environment(AppRouter.self) private var router

    @State private var vm = LocationViewModel()

    @State private var gender = "ALL"
    @State private var showComment = false

    var body: some View {
        VStack {
            GenderPicker(selectedGender: $gender)

            MemberList(onRefresh: vm.bump)
        }
        .navigationTitle("위치")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    showComment = true
                } label: {
                    Image(systemName: "square.and.pencil")
                        .font(.default)
                        .foregroundStyle(.primary)
                }
            }
        }
        .alert("코멘트", isPresented: $showComment) {
            TextField("내용", text: $vm.comment)

            Button("작성") {
                Task {
                    await vm.updateComment()
                }
            }
            Button("취소", role: .cancel) { }
        }
    }
}
