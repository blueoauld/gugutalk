import SwiftUI

struct MemberActionBar: View {
    
    let memberId: Int64
    let member: MemberGetResponse
    let vm: MemberViewModel
    
    @State private var showMessage = false
    @State private var showBlock = false
    @State private var message = ""
    
    var body: some View {
        VStack {
            HStack(spacing: 22) {
                Button {
                    Task {
                        if member.isLike {
                            await vm.deleteLike(memberId: memberId)
                        } else {
                            await vm.createLike(memberId: memberId)
                        }
                    }
                } label: {
                    Image(systemName: "heart.fill")
                        .font(.title2)
                        .foregroundStyle(member.isLike ? .red : .gray)
                }
                .sensoryFeedback(.selection, trigger: member.isLike)
                
                Button {
                    Task {
                        if member.isUnlike {
                            await vm.deleteUnlike(memberId: memberId)
                        } else {
                            await vm.createUnlike(memberId: memberId)
                        }
                    }
                } label: {
                    Image(systemName: "heart.slash.fill")
                        .font(.title2)
                        .foregroundStyle(member.isUnlike ? .blue : .gray)
                }
                .sensoryFeedback(.selection, trigger: member.isUnlike)
                
                Button {
                    
                } label: {
                    Image(systemName: "star.fill")
                        .font(.title2)
                        .foregroundStyle(.yellow)
                }
                
                Button {
                    showMessage = true
                } label: {
                    Image(systemName: "envelope.fill")
                        .font(.title2)
                        .foregroundStyle(member.isChat ? Color.primary : Color.gray)
                }
                .disabled(!member.isChat)
                
                Button {
                    
                } label: {
                    Image(systemName: "photo.fill")
                        .font(.title2)
                        .foregroundStyle(member.hasPrivateImageGrant ? .green : .gray)
                }
                .disabled(!member.hasPrivateImageGrant)
                
                Button {
                    showBlock = true
                } label: {
                    Image(systemName: "nosign")
                        .font(.title2.bold())
                        .foregroundStyle(member.isBlock ? .orange : .gray)
                }
            }
            .padding()
            .glassEffect(.regular, in: .capsule)
        }
        .alert("쪽지", isPresented: $showMessage) {
            TextField("내용", text: $message)
            Button("전송") { }
            Button("취소", role: .cancel) { }
        }
        .alert("차단", isPresented: $showBlock) {
            Button(member.isBlock ? "해제" : "차단") {
                Task {
                    if member.isBlock {
                        await vm.deleteBlock(memberId: memberId)
                    } else {
                        await vm.createBlock(memberId: memberId)
                    }
                }
            }
            Button("취소", role: .cancel) { }
        } message: {
            Text(member.isBlock ? "차단을 해제하시겠습니까?" : "대화 내역이 모두 삭제되며\n서로의 목록에서도 표시되지 않습니다.")
        }
    }
}
