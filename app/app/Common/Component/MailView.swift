import SwiftUI
import MessageUI

struct MailView: UIViewControllerRepresentable {

    @Environment(\.dismiss) var dismiss

    let recipients: [String]
    let subject: String
    let body: String

    func makeUIViewController(context: Context) -> MFMailComposeViewController {
        let vc = MFMailComposeViewController()
        vc.mailComposeDelegate = context.coordinator
        vc.setToRecipients(recipients)
        vc.setSubject(subject)
        vc.setMessageBody(body, isHTML: false)
        return vc
    }

    func updateUIViewController(_ uiViewController: MFMailComposeViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(parent: self)
    }

    class Coordinator: NSObject, MFMailComposeViewControllerDelegate {
        let parent: MailView

        init(parent: MailView) {
            self.parent = parent
        }

        func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
            parent.dismiss()
        }
    }
}
