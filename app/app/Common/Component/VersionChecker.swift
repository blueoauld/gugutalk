import Foundation

enum VersionChecker {

    static func isUpdateAvailable() async -> Bool {
        guard let info = Bundle.main.infoDictionary,
              let current = info["CFBundleShortVersionString"] as? String,
              let bundleId = info["CFBundleIdentifier"] as? String,
              let url = URL(string: "https://itunes.apple.com/lookup?bundleId=\(bundleId)")
        else { return false }

        do {
            let (data, _) = try await URLSession.shared.data(from: url)
            let result = try JSONDecoder().decode(LookupResult.self, from: data)

            guard let store = result.results.first?.version else { return false }

            return needsUpdate(current: current, store: store)
        } catch {
            return false
        }
    }

    static func needsUpdate(current: String, store: String) -> Bool {
        store.compare(current, options: .numeric) == .orderedDescending
    }

    private struct LookupResult: Decodable {

        let results: [AppInfo]

        struct AppInfo: Decodable { let version: String }
    }
}
