import SwiftUI
import Observation

struct Toast: Equatable, Identifiable {

    let id = UUID()
    let message: String
    var style: Style = .error
    var duration: TimeInterval = 2.0

    enum Style {
        case error, success, info

        var backgroundColor: Color {
            switch self {
            case .error: return Color.red.opacity(0.9)
            case .success: return Color.green.opacity(0.9)
            case .info: return Color.blue.opacity(0.9)
            }
        }

        var icon: String {
            switch self {
            case .error: return "xmark.circle.fill"
            case .success: return "checkmark.circle.fill"
            case .info: return "info.circle.fill"
            }
        }
    }
}

@MainActor
@Observable
final class ToastManager {

    static let shared = ToastManager()

    var current: Toast?

    private init() {}

    func show(_ message: String, style: Toast.Style = .error, duration: TimeInterval = 2.0) {
        current = Toast(message: message, style: style, duration: duration)
    }

    func dismiss() {
        current = nil
    }
}

struct ToastView: View {

    let toast: Toast
    let onTap: () -> Void

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: toast.style.icon)
                .font(.system(size: 18, weight: .semibold))

            Text(toast.message)
                .font(.system(size: 14, weight: .medium))
                .lineLimit(2)
                .multilineTextAlignment(.leading)

            Spacer(minLength: 0)
        }
        .foregroundColor(.white)
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(toast.style.backgroundColor)
        )
        .padding(.horizontal, 16)
        .onTapGesture {
            onTap()
        }
    }
}

struct ToastModifier: ViewModifier {

    private let manager = ToastManager.shared

    @State private var dismissTask: Task<Void, Never>?

    func body(content: Content) -> some View {
        content
            .overlay(alignment: .bottom) {
                if let toast = manager.current {
                    ToastView(toast: toast) {
                        dismiss()
                    }
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .padding(.bottom, 72)
                    .zIndex(10)
                }
            }
            .animation(.spring(response: 0.35, dampingFraction: 0.85), value: manager.current)
            .onChange(of: manager.current) { _, newValue in
                guard let newValue else { return }

                scheduleDismiss(after: newValue.duration)
            }
    }

    private func scheduleDismiss(after seconds: TimeInterval) {
        dismissTask?.cancel()
        dismissTask = Task { @MainActor in
            try? await Task.sleep(for: .seconds(seconds))

            guard !Task.isCancelled else { return }

            dismiss()
        }
    }

    private func dismiss() {
        dismissTask?.cancel()
        dismissTask = nil
        manager.dismiss()
    }
}

extension View {

    func toastHost() -> some View {
        modifier(ToastModifier())
    }
}
