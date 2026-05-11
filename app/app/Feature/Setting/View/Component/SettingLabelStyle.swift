import SwiftUI

struct SettingLabelStyle: LabelStyle {

    let color: Color

    func makeBody(configuration: Configuration) -> some View {
        HStack(spacing: 12) {
            configuration.icon
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(.white)
                .frame(width: 30, height: 30)
                .background(color, in: RoundedRectangle(cornerRadius: 7))

            configuration.title
        }
    }
}

extension LabelStyle where Self == SettingLabelStyle {

    static func settings(color: Color) -> SettingLabelStyle {
        SettingLabelStyle(color: color)
    }
}
