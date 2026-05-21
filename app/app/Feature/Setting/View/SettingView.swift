import SwiftUI

struct SettingView: View {

    @Environment(AppRouter.self) private var router
    @Environment(SessionStore.self) private var session

    @State private var vm = SettingViewModel()

    @State private var showMenu = false

    var body: some View {
        VStack {
            Form {
                Section("계정") {
                    Label("내 프로필", systemImage: "person.fill")
                        .labelStyle(.settings(color: .blue))
                }

                Section("활동") {
                    Label("좋아요 목록", systemImage: "heart.fill")
                        .labelStyle(.settings(color: .red))
                    Label("싫어요 목록", systemImage: "heart.slash.fill")
                        .labelStyle(.settings(color: .blue))
                    Label("비밀 사진 목록", systemImage: "lock.fill")
                        .labelStyle(.settings(color: .green))
                    Label("차단 목록", systemImage: "nosign")
                        .labelStyle(.settings(color: .gray))
                }

                Section("포인트") {
                    Label("출석 체크", systemImage: "calendar.badge.checkmark")
                        .labelStyle(.settings(color: .indigo))
                    Label("광고 보상", systemImage: "play.rectangle.fill")
                        .labelStyle(.settings(color: .pink))
                }

                Section("기타") {
                    Label("문의하기", systemImage: "ellipsis.message.fill")
                        .labelStyle(.settings(color: .green))
                    Label("버그제보", systemImage: "lightbulb.fill")
                        .labelStyle(.settings(color: .yellow))
                    Label("서비스 이용약관", systemImage: "doc.text.fill")
                        .labelStyle(.settings(color: .orange))
                    Label("개인정보 취급방침", systemImage: "hand.raised.fill")
                        .labelStyle(.settings(color: .brown))
                }
            }
        }
        .navigationTitle("설정")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    showMenu = true
                } label: {
                    Image(systemName: "ellipsis")
                        .font(.default)
                        .foregroundStyle(.primary)
                }
                .confirmationDialog("메뉴", isPresented: $showMenu) {
                    Button("로그아웃") {
                        Task {
                            TokenStorage.shared.clearAll()
                            session.isLoggedIn = false

                            await vm.logout()
                        }
                    }

                    Button("탈퇴", role: .destructive) {

                    }
                }
            }
        }
    }
}

#Preview {
    NavigationStack {
        SettingView()
            .environment(AppRouter())
    }
}
