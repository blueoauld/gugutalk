import SwiftUI

enum WatermarkStyle {

    case single
    case diagonal
    case tiled
}

enum WatermarkColorMode {

    case fixed(Color)
    case adaptive
    case contrast
}

struct WatermarkModifier: ViewModifier {

    let text: String
    let style: WatermarkStyle
    var opacity: Double = 0.15
    var colorMode: WatermarkColorMode = .contrast
    var fontSize: CGFloat = 24
    var rotation: Angle = .degrees(-30)

    func body(content: Content) -> some View {
        content.overlay(watermarkOverlay)
    }

    private var renderColor: Color {
        switch colorMode {
        case .fixed(let c): return c
        case .adaptive:     return .primary
        case .contrast:     return .white   // difference 합성용
        }
    }

    private var blend: BlendMode {
        if case .contrast = colorMode { return .difference }
        return .normal
    }

    @ViewBuilder
    private var watermarkOverlay: some View {
        Group {
            switch style {
            case .single:
                Text(text)
                    .font(.system(size: fontSize, weight: .bold))
                    .foregroundColor(renderColor)
                    .rotationEffect(rotation)

            case .diagonal:
                GeometryReader { geo in
                    Text(text)
                        .font(.system(size: min(geo.size.width, geo.size.height) * 0.15,
                                      weight: .heavy))
                        .foregroundColor(renderColor)
                        .rotationEffect(rotation)
                        .frame(width: geo.size.width, height: geo.size.height)
                }

            case .tiled:
                TiledWatermark(
                    text: text,
                    color: renderColor,
                    fontSize: fontSize,
                    rotation: rotation
                )
            }
        }
        .opacity(opacity)        // 블렌드 결과에 대한 투명도
        .blendMode(blend)
        .allowsHitTesting(false)
    }
}

private struct TiledWatermark: View {

    let text: String
    let color: Color
    let fontSize: CGFloat
    let rotation: Angle

    var body: some View {
        Canvas { context, size in
            let resolved = context.resolve(
                Text(text)
                    .font(.system(size: fontSize, weight: .semibold))
                    .foregroundColor(color)
            )
            let textSize = resolved.measure(in: size)
            let stepX = textSize.width + 40
            let stepY = textSize.height + 60

            context.rotate(by: rotation)

            let extra = max(size.width, size.height)
            for y in stride(from: -extra, through: size.height + extra, by: stepY) {
                for x in stride(from: -extra, through: size.width + extra, by: stepX) {
                    context.draw(resolved, at: CGPoint(x: x, y: y))
                }
            }
        }
    }
}

extension View {

    func watermark(
        _ text: String,
        style: WatermarkStyle = .diagonal,
        opacity: Double = 0.15,
        colorMode: WatermarkColorMode = .contrast,
        fontSize: CGFloat = 16,
        rotation: Angle = .degrees(-30)
    ) -> some View {
        modifier(WatermarkModifier(
            text: text,
            style: style,
            opacity: opacity,
            colorMode: colorMode,
            fontSize: fontSize,
            rotation: rotation
        ))
    }
}
