import SwiftUI

struct BanView: View {

    let banInfo: BanInfo

    @State private var didCopy = false

    var body: some View {
        ContentUnavailableView {
            Label("서비스 이용 제한", systemImage: "nosign")
        } description: {
            VStack(spacing: 4) {
                Text("사유: \(banInfo.reason)")
                    .foregroundStyle(.secondary)

                if let date = banInfo.expiredAt.toISO8601Date() {
                    Text("해제일: \(date.formatted(.dateTime.year().month().day()))")
                        .foregroundStyle(.secondary)
                }

                Text("문의: gugutalk@proton.me")
                    .foregroundStyle(.secondary)
                    .padding(.top, 8)
            }
        } actions: {
            Button {
                UIPasteboard.general.string = banInfo.uuid
                ToastManager.shared.show("복사되었습니다.", style: .info)
            } label: {
                Text("복사")
                    .padding(.horizontal)
            }
            .buttonStyle(.borderedProminent)
        }
    }
}
