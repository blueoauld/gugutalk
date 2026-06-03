import SwiftUI

struct MemberActionBar: View {

    let memberId: Int64
    let member: MemberGetResponse
    let vm: MemberViewModel

    @AppStorage(StorageKey.message) private var savedMessage = ""

    @State private var showMessage = false
    @State private var showPrivateImageFullScreen = false
    @State private var showBlock = false
    @State private var message = ""

    var body: some View {
        VStack {
            HStack(spacing: 22) {
                Button {
                    Task {
                        if member.isLike {
                            if case .failure(let error) = await vm.deleteLike(memberId: memberId) {
                                ToastManager.shared.show(error.userMessage, style: .error)
                            }
                        } else {
                            if case .failure(let error) = await vm.createLike(memberId: memberId) {
                                ToastManager.shared.show(error.userMessage, style: .error)
                            }
                        }
                    }
                } label: {
                    Image(systemName: "heart.fill")
                        .font(.title2)
                        .foregroundStyle(member.isLike ? .red : .gray)
                        .symbolEffect(.bounce, value: member.isLike)
                }
                .sensoryFeedback(.selection, trigger: member.isLike)

                Button {
                    Task {
                        if member.isUnlike {
                            if case .failure(let error) = await vm.deleteUnlike(memberId: memberId) {
                                ToastManager.shared.show(error.userMessage, style: .error)
                            }
                        } else {
                            if case .failure(let error) = await vm.createUnlike(memberId: memberId) {
                                ToastManager.shared.show(error.userMessage, style: .error)
                            }
                        }
                    }
                } label: {
                    Image(systemName: "heart.slash.fill")
                        .font(.title2)
                        .foregroundStyle(member.isUnlike ? .blue : .gray)
                        .symbolEffect(.bounce, value: member.isUnlike)
                }
                .sensoryFeedback(.selection, trigger: member.isUnlike)

                NavigationLink(value: AppRoute.review(memberId, member.nickname)) {
                    Image(systemName: "star.fill")
                        .font(.title2)
                        .foregroundStyle(.yellow)
                }

                Button {
                    message = savedMessage
                    showMessage = true
                } label: {
                    Image(systemName: "envelope.fill")
                        .font(.title2)
                        .foregroundStyle(member.isChat ? Color.primary : Color.gray)
                }
                .disabled(!member.isChat)

                Button {
                    showPrivateImageFullScreen = true
                } label: {
                    Image(systemName: "photo.fill")
                        .font(.title2)
                        .foregroundStyle(member.hasPrivateImageGrant ? .green : .gray)
                        .overlay(alignment: .topTrailing) {
                            Text("\(member.privateImages)")
                                .font(.caption2)
                                .fontWeight(.bold)
                                .foregroundStyle(.white)
                                .padding(.horizontal, 5)
                                .padding(.vertical, 2)
                                .background(
                                    member.privateImages <= 0 ? Color(.systemGray2) : Color(.systemRed),
                                    in: Capsule()
                                )
                                .offset(x: 7, y: -7)
                        }
                }
                .disabled(!member.hasPrivateImageGrant)

                Button {
                    showBlock = true
                } label: {
                    Image(systemName: "nosign")
                        .font(.title2.bold())
                        .foregroundStyle(member.isBlock ? .orange : .gray)
                        .symbolEffect(.bounce, value: member.isBlock)
                }
            }
            .padding()
            .glassEffect(.regular, in: .capsule)
        }
        .fullScreenCover(isPresented: $showPrivateImageFullScreen) {
            PrivateImageFullScreenView(
                memberId: member.memberId
            )
        }
        .alert("쪽지", isPresented: $showMessage) {
            TextField("내용 입력 (15P)", text: $message)

            Button("전송") {
                Task {
                    guard let result = await vm.createChatRoom(memberId: memberId, message: message) else { return }

                    switch result {
                    case .success():
                        ToastManager.shared.show("쪽지를 보내셨습니다.", style: .info)
                        savedMessage = message
                    case .failure(let error):
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                }
            }

            Button("취소", role: .cancel) { }
        }
        .alert("차단", isPresented: $showBlock) {
            Button(member.isBlock ? "해제" : "차단") {
                Task {
                    if member.isBlock {
                        if case .failure(let error) = await vm.deleteBlock(memberId: memberId) {
                            ToastManager.shared.show(error.userMessage, style: .error)
                        }
                    } else {
                        if case .failure(let error) = await vm.createBlock(memberId: memberId) {
                            ToastManager.shared.show(error.userMessage, style: .error)
                        }
                    }
                }
            }
            Button("취소", role: .cancel) { }
        } message: {
            Text(member.isBlock ? "차단을 해제하시겠습니까?" : "대화 내역이 모두 삭제되며 서로의 목록에서도 표시되지 않습니다.")
        }
    }
}
