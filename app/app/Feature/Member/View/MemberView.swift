import SwiftUI

struct MemberView: View {

    let memberId: Int64

    @State private var vm = MemberViewModel()

    @State private var currentPage = 0
    @State private var message = ""
    @State private var showMessage = false
    @State private var showBlock = false
    @State private var showMenu = false

    var body: some View {
        VStack {
            switch vm.state {
            case .idle:
                EmptyView()
            case .loading:
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .data:
                if let member = vm.member {
                    ScrollView {
                        TabView(selection: $currentPage) {
                            Image(systemName: "person.fill")
                                .font(.largeTitle)
                                .padding()
                                .frame(maxWidth: .infinity, maxHeight: .infinity)
                                .foregroundStyle(Color(.systemGray4))
                                .background(Color(.systemGray6))
                                .tag(0)

                            Image(systemName: "person.fill")
                                .font(.largeTitle)
                                .padding()
                                .frame(maxWidth: .infinity, maxHeight: .infinity)
                                .foregroundStyle(Color(.systemGray4))
                                .background(Color(.systemGray6))
                                .tag(1)
                        }
                        .tabViewStyle(.page)
                        .aspectRatio(4/3, contentMode: .fit)

                        VStack(alignment: .leading) {
                            VStack {
                                HStack {
                                    Text(member.nickname)
                                        .font(.title3.bold())

                                    Spacer()

                                    if let date = member.updatedAt.toISO8601Date() {
                                        Text(date.formatted(.relative(presentation: .named)))
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                    }
                                }

                                HStack {
                                    Text(member.gender.label)
                                    Text("·")
                                    Text("\(member.age)살")
                                    Text("·")
                                    Text(member.region.label)

                                    Spacer()
                                }
                                .font(.subheadline)
                                .foregroundStyle(.secondary)

                                HStack {
                                    HStack(spacing: 3) {
                                        Image(systemName: "heart.fill")
                                            .foregroundStyle(.red)

                                        Text("\(member.likes)")
                                    }

                                    Text("·")

                                    HStack(spacing: 3) {
                                        Image(systemName: "heart.slash.fill")
                                            .foregroundStyle(.blue)

                                        Text("\(member.unlikes)")
                                    }

                                    Text("·")

                                    HStack(spacing: 3) {
                                        Image(systemName: "star.fill")
                                            .foregroundStyle(.yellow)

                                        Text("\(member.reviews)")
                                    }

                                    Spacer()
                                }
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                            }
                            .padding(.bottom)

                            VStack {
                                Text(member.bio)
                                    .font(.body)
                                    .foregroundColor(.primary)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .padding()
                                    .background(
                                        Color(.systemGray6),
                                        in: RoundedRectangle(cornerRadius: 16)
                                    )
                            }
                        }
                        .padding()
                    }
                    .safeAreaBar(edge: .bottom) {
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
                    .ignoresSafeArea(.keyboard, edges: .bottom)
                }
            case .error(let message):
                ErrorRetryView(message: message, retry: { await vm.get(memberId: memberId) })
            }
        }
        .navigationTitle("프로필")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if let member = vm.member {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showMenu = true
                    } label: {
                        Image(systemName: "ellipsis")
                            .font(.default)
                            .foregroundStyle(.primary)
                    }
                    .confirmationDialog("메뉴", isPresented: $showMenu) {
                        Button(member.isPrivateImageGrant ? "비밀 사진 닫기" : "비밀 사진 공개") {
                            Task {
                                if member.isPrivateImageGrant {
                                    await vm.deletePrivateImageGrant(memberId: memberId)
                                } else {
                                    await vm.createPrivateImageGrant(memberId: memberId)
                                }
                            }
                        }

                        Button("신고", role: .destructive) {

                        }
                    }
                }
            }
        }
        .task {
            await vm.get(memberId: memberId)
        }
        .alert("쪽지", isPresented: $showMessage) {
            TextField("내용", text: $message)
            Button("전송") { }
            Button("취소", role: .cancel) { }
        }
        .alert("차단", isPresented: $showBlock) {
            if let member = vm.member {
                Button(member.isBlock ? "해제" : "차단") {
                    Task {
                        if member.isBlock {
                            await vm.deleteBlock(memberId: memberId)
                        } else {
                            await vm.createBlock(memberId: memberId)
                        }
                    }
                }
            }
            Button("취소", role: .cancel) { }
        } message: {
            if let member = vm.member {
                Text(member.isBlock ? "차단을 해제하시겠습니까?" : "대화 내역이 모두 삭제되며\n서로의 목록에서도 표시되지 않습니다.")
            }
        }
    }
}
