import KeychainSwift

final class TokenStorage {

    static let shared = TokenStorage()

    private let keychain = KeychainSwift()

    private let deviceIdKey = "device_id"
    private let memberIdKey = "member_id"
    private let accessTokenKey = "access_token"
    private let refreshTokenKey = "refresh_token"

    private var cachedDeviceId: String?
    private var cachedMemberId: Int64?
    private var cachedAccessToken: String?
    private var cachedRefreshToken: String?

    private init() {}

    var deviceId: String? {
        get {
            read(key: deviceIdKey, cache: &cachedDeviceId)
        }
        set {
            write(newValue, key: deviceIdKey, cache: &cachedDeviceId)
        }
    }

    var memberId: Int64? {
        get {
            if let cached = cachedMemberId {
                return cached
            }

            guard let str = keychain.get(memberIdKey), let value = Int64(str) else {
                return nil
            }

            cachedMemberId = value
            return value
        }
        set {
            cachedMemberId = newValue

            if let value = newValue {
                keychain.set(String(value), forKey: memberIdKey)
            } else {
                keychain.delete(memberIdKey)
            }
        }
    }

    var accessToken: String? {
        get {
            read(key: accessTokenKey, cache: &cachedAccessToken)
        }
        set {
            write(newValue, key: accessTokenKey, cache: &cachedAccessToken)
        }
    }

    var refreshToken: String? {
        get {
            read(key: refreshTokenKey, cache: &cachedRefreshToken)
        }
        set {
            write(newValue, key: refreshTokenKey, cache: &cachedRefreshToken)
        }
    }

    func clearAll() {
        cachedDeviceId = nil
        cachedMemberId = nil
        cachedAccessToken = nil
        cachedRefreshToken = nil

        [deviceIdKey, memberIdKey, accessTokenKey, refreshTokenKey].forEach {
            keychain.delete($0)
        }
    }

    private func read(key: String, cache: inout String?) -> String? {
        if let cached = cache {
            return cached
        }

        let value = keychain.get(key)
        cache = value
        return value
    }

    private func write(_ newValue: String?, key: String, cache: inout String?) {
        cache = newValue

        if let value = newValue {
            keychain.set(value, forKey: key)
        } else {
            keychain.delete(key)
        }
    }
}
