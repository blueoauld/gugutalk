import SwiftUI

struct MemberView: View {

    let memberId: Int64

    @State private var currentPage = 0
    @State private var message = ""
    @State private var showMessage = false
    @State private var showMenu = false

    var body: some View {
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
                        Text("홍길동")
                            .font(.title3.bold())

                        Spacer()

                        Text("방금전")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }

                    HStack {
                        Text("남자")
                        Text("·")
                        Text("20살")
                        Text("·")
                        Text("서울")

                        Spacer()
                    }
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                    HStack {
                        HStack(spacing: 3) {
                            Image(systemName: "heart.fill")
                                .foregroundStyle(.red)

                            Text("100")
                        }

                        Text("·")

                        HStack(spacing: 3) {
                            Image(systemName: "heart.slash.fill")
                                .foregroundStyle(.blue)

                            Text("200")
                        }

                        Text("·")

                        HStack(spacing: 3) {
                            Image(systemName: "star.fill")
                                .foregroundStyle(.yellow)

                            Text("300")
                        }

                        Spacer()
                    }
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                }
                .padding(.bottom)

                VStack {
                    Text("자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개자기소개")
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

                } label: {
                    Image(systemName: "heart.fill")
                        .font(.title2)
                        .foregroundStyle(.red)
                }

                Button {

                } label: {
                    Image(systemName: "heart.slash.fill")
                        .font(.title2)
                        .foregroundStyle(.blue)
                }

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
                        .foregroundStyle(.foreground)
                }

                Button {

                } label: {
                    Image(systemName: "photo.fill")
                        .font(.title2)
                        .foregroundStyle(.green)
                }

                Button {

                } label: {
                    Image(systemName: "nosign")
                        .font(.title2.bold())
                        .foregroundStyle(.orange)
                }
            }
            .padding()
            .glassEffect(.regular, in: .capsule)
        }
        .ignoresSafeArea(.keyboard, edges: .bottom)
        .navigationTitle("프로필")
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
                    Button("비밀 사진 공개") {

                    }

                    Button("신고", role: .destructive) {

                    }
                }
            }
        }
        .alert("쪽지", isPresented: $showMessage) {
            TextField("내용", text: $message)
            Button("전송") { }
            Button("취소", role: .cancel) { }
        }
    }
}
