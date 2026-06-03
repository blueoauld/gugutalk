import SwiftUI

struct PointHeader: View {

    let balance: Int64

    var body: some View {
        VStack(alignment: .leading) {
            Text("보유 포인트")
                .font(.subheadline)
                .foregroundStyle(.secondary)

            HStack(alignment: .firstTextBaseline, spacing: 4) {
                Text("\(balance)")
                    .font(.largeTitle)
                    .fontWeight(.bold)

                Text("P")
                    .font(.title3)
                    .foregroundStyle(.secondary)
            }
        }
        .padding()
    }
}
