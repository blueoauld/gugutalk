import GoogleMobileAds
import SwiftUI

@MainActor
@Observable
final class AdMobViewModel: NSObject, FullScreenContentDelegate {

    var isAdReady = false

    @ObservationIgnored private var rewardedAd: RewardedAd?
    @ObservationIgnored private let memberId: Int64
    @ObservationIgnored private let adUnitID = "ca-app-pub-3940256099942544/1712485313"

    init(memberId: Int64) {
        self.memberId = memberId
        super.init()
    }

    func loadAd() async {
        do {
            let ad = try await RewardedAd.load(with: adUnitID, request: Request())

            let options = ServerSideVerificationOptions()
            options.userIdentifier = String(memberId)
            ad.serverSideVerificationOptions = options

            ad.fullScreenContentDelegate = self
            rewardedAd = ad
            isAdReady = true
        } catch {
            isAdReady = false
        }
    }

    func showAd(onRewardEarned: @escaping () -> Void) {
        guard let rewardedAd else {
            ToastManager.shared.show("광고가 준비되지 않았습니다.", style: .error)
            return
        }

        rewardedAd.present(from: nil) {
            onRewardEarned()
        }
    }

    // MARK: - FullScreenContentDelegate

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        rewardedAd = nil
        isAdReady = false

        Task { await loadAd() }
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        rewardedAd = nil
        isAdReady = false
    }
}
