import SwiftUI

struct LocationView: View {

    @Environment(AppRouter.self) private var router

    @State private var gender = "ALL"
    @State private var showComment = false
    @State private var comment = ""

    var body: some View {
        VStack {
            GenderPicker(selectedGender: $gender)

            MemberList()
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
                        .foregroundColor(.primary)
                }
            }
        }
        .alert("코멘트", isPresented: $showComment) {
            TextField("내용", text: $comment)

            Button("작성") {
            }
            Button("취소", role: .cancel) { }
        }
    }
}
