import SwiftUI
import MessageUI

struct SettingView: View {

    @Environment(SessionStore.self) private var session

    @State private var vm = SettingViewModel()
    @State private var showMenu = false
    @State private var showQuestion = false
    @State private var showBug = false
    @State private var showService = false
    @State private var showPrivate = false

    var body: some View {
        VStack {
            Form {
                Section("계정") {
                    if let memberId = TokenStorage.shared.memberId {
                        NavigationLink(value: AppRoute.member(memberId)) {
                            Label("내 프로필", systemImage: "person.fill")
                                .labelStyle(.settings(color: .yellow))
                        }
                        .navigationLinkIndicatorVisibility(.hidden)
                    }
                }

                Section("활동") {
                    NavigationLink(value: AppRoute.likeList) {
                        Label("좋아요 목록", systemImage: "heart.fill")
                            .labelStyle(.settings(color: .red))
                    }
                    .navigationLinkIndicatorVisibility(.hidden)

                    NavigationLink(value: AppRoute.unlikeList) {
                        Label("싫어요 목록", systemImage: "heart.slash.fill")
                            .labelStyle(.settings(color: .blue))
                    }
                    .navigationLinkIndicatorVisibility(.hidden)

                    NavigationLink(value: AppRoute.privateImageGrantList) {
                        Label("비밀 사진 목록", systemImage: "lock.fill")
                            .labelStyle(.settings(color: .green))
                    }
                    .navigationLinkIndicatorVisibility(.hidden)

                    NavigationLink(value: AppRoute.blockList) {
                        Label("차단 목록", systemImage: "nosign")
                            .labelStyle(.settings(color: .gray))
                    }
                    .navigationLinkIndicatorVisibility(.hidden)
                }

                Section("포인트") {
                    NavigationLink(value: AppRoute.point) {
                        Label("포인트 내역", systemImage: "gift.fill")
                            .labelStyle(.settings(color: .cyan))
                    }
                    .navigationLinkIndicatorVisibility(.hidden)

                    Button {
                        Task {
                            guard let result = await vm.rewardAttendance() else { return }

                            switch result {
                            case .success():
                                ToastManager.shared.show("출석 체크가 완료되었습니다.", style: .info)
                            case .failure(let error):
                                ToastManager.shared.show(error.userMessage, style: .error)
                            }
                        }
                    } label: {
                        Label("출석 체크", systemImage: "calendar.badge.checkmark")
                            .labelStyle(.settings(color: .indigo))
                    }
                    .tint(.primary)

                    Button {
                        Task {
                            guard let result = await vm.rewardAdvertisement() else { return }

                            switch result {
                            case .success():
                                ToastManager.shared.show("광고 보상이 지급되었습니다.", style: .info)
                            case .failure(let error):
                                ToastManager.shared.show(error.userMessage, style: .error)
                            }
                        }
                    } label: {
                        Label("광고 보상", systemImage: "play.rectangle.fill")
                            .labelStyle(.settings(color: .pink))
                    }
                    .tint(.primary)
                }

                Section("기타") {
                    Button {
                        if MFMailComposeViewController.canSendMail() {
                            showQuestion = true
                        } else {
                            ToastManager.shared.show("메일 계정을 설정해주시길 바랍니다.", style: .error)
                        }
                    } label: {
                        Label("문의사항", systemImage: "ellipsis.message.fill")
                            .labelStyle(.settings(color: .green))
                    }
                    .tint(.primary)

                    Button {
                        if MFMailComposeViewController.canSendMail() {
                            showBug = true
                        } else {
                            ToastManager.shared.show("메일 계정을 설정해주시길 바랍니다.", style: .error)
                        }
                    } label: {
                        Label("버그제보", systemImage: "lightbulb.fill")
                            .labelStyle(.settings(color: .mint))
                    }
                    .tint(.primary)

                    Button {
                        showService = true
                    } label: {
                        Label("서비스 이용약관", systemImage: "doc.text.fill")
                            .labelStyle(.settings(color: .orange))
                    }
                    .sheet(isPresented: $showService) {
                        SafariView(url: URL(string: "https://pidulgi.com/service.html")!)
                    }
                    .tint(.primary)

                    Button {
                        showPrivate = true
                    } label: {
                        Label("개인정보 취급방침", systemImage: "hand.raised.fill")
                            .labelStyle(.settings(color: .brown))
                    }
                    .sheet(isPresented: $showPrivate) {
                        SafariView(url: URL(string: "https://pidulgi.com/private.html")!)
                    }
                    .tint(.primary)
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
                        .font(.body)
                        .foregroundStyle(.primary)
                }
                .confirmationDialog("메뉴", isPresented: $showMenu) {
                    Button("로그아웃") {
                        Task {
                            guard let result = await vm.logout() else { return }

                            switch result {
                            case .success():
                                ToastManager.shared.show("로그아웃이 완료되었습니다.", style: .info)
                            case .failure(let error):
                                ToastManager.shared.show(error.userMessage, style: .error)
                            }

                            session.logout()
                        }
                    }

                    Button("탈퇴", role: .destructive) {

                    }
                }
            }
        }
        .sheet(isPresented: $showQuestion) {
            let deviceId = TokenStorage.shared.deviceId ?? "알 수 없음"
            let deviceModel = machineIdentifier()
            let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "알 수 없음"

            MailView(
                recipients: ["gugutalk@proton.me"],
                subject: "문의사항",
                body: """
                    내용: 
                    
                    
                    아래 정보를 삭제하지 마세요.
                    ─────────────────
                    디바이스 ID: \(deviceId)
                    기기: \(deviceModel)
                    버전: \(appVersion)
                    ─────────────────
                """,
            )
        }
        .sheet(isPresented: $showBug) {
            let deviceId = TokenStorage.shared.deviceId ?? "알 수 없음"
            let deviceModel = machineIdentifier()
            let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "알 수 없음"

            MailView(
                recipients: ["gugutalk@proton.me"],
                subject: "버그제보",
                body: """
                    내용: 
                    
                    
                    아래 정보를 삭제하지 마세요.
                    ─────────────────
                    디바이스 ID: \(deviceId)
                    기기: \(deviceModel)
                    버전: \(appVersion)
                    ─────────────────
                """,
            )
        }
        .overlay {
            if vm.isLoading {
                LoadingOverlay()
            }
        }
    }

    private func machineIdentifier() -> String {
        var systemInfo = utsname()
        uname(&systemInfo)

        return withUnsafePointer(to: &systemInfo.machine) {
            $0.withMemoryRebound(to: CChar.self, capacity: 1) {
                String(validatingUTF8: $0) ?? "Unknown"
            }
        }
    }
}
