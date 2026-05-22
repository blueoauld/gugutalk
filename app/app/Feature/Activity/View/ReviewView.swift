import SwiftUI

struct ReviewView: View {

    let memberId: Int64

    @Environment(AppRouter.self) private var router

    @State private var showAlert = false
    @State private var review = ""

    var body: some View {
        VStack {
            List(1...100, id: \.self) { index in
                VStack(alignment: .leading) {
                    HStack {
                        Text("잠자는 고양이")
                            .font(.subheadline.bold())
                            .foregroundStyle(.primary)

                        Spacer()

                        Text("2025.05.\(String(format: "%02d", index))")
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }

                    Text("안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                .padding(.vertical, 4)
                .padding(.horizontal)
                .listRowSeparator(.hidden)
                .listRowInsets(EdgeInsets())
                .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                    Button {

                    } label: {
                        Image(systemName: "trash.fill")
                        Text("삭제 (10P)")
                    }
                    .tint(.red)
                }
            }
            .listStyle(.plain)
            .onTapGesture {
                hideKeyboard()
            }
        }
        .safeAreaBar(edge: .bottom) {
            TextField("리뷰 입력 (5P)", text: $review, axis: .vertical)
                .font(.subheadline)
                .lineLimit(1...5)
                .multilineTextAlignment(.leading)
                .padding(.leading)
                .padding(.trailing, 50)
                .padding(.vertical, 8)
                .frame(minHeight: 44)
                .overlay(alignment: .bottomTrailing) {
                    HStack {
                        Spacer()

                        Button {
                            Task {
                            }
                        } label: {
                            Image(systemName: "paperplane.fill")
                                .foregroundColor(.white)
                                .frame(width: 36, height: 36)
                                .background(review.isEmpty ? Color(.systemGray3) : .blue)
                                .clipShape(Circle())
                        }
                        .padding(.trailing, 4)
                        .padding(.bottom, 4)
                        .disabled(review.isEmpty)
                    }
                }
                .glassEffect(
                    .regular.tint(.clear).interactive(),
                    in: .rect(cornerRadius: 20)
                )
                .autocorrectionDisabled(true)
                .textInputAutocapitalization(.never)
                .padding()
        }
        .navigationTitle("리뷰 (홍길동)")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            showAlert = true
        }
        .alert("경고", isPresented: $showAlert) {
            Button("확인", role: .cancel) {}
        } message: {
            Text("신상 공개, 음란, 비방 등 부적절한 내용이 포함된 리뷰는 서비스 이용이 제한될 수 있습니다. 작성하신 리뷰는 삭제가 불가능하니 신중히 작성해 주세요.")
        }
    }
}
