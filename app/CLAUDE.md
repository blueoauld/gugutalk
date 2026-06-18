# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository scope

This directory is the **iOS client** (SwiftUI) for 구구톡 / pidulgi. It lives in a monorepo whose root (`../`) also contains `server/` (Kotlin / Spring Boot 4, the API this app talks to) and `web/`. The root `README.md` documents the full system and server architecture. Work here is Swift/iOS only unless told otherwise.

## Build & run

- No CocoaPods or `.xcworkspace`. Dependencies are Swift Package Manager packages resolved by the Xcode project. Open `app.xcodeproj` directly.
- Scheme: `app`. Bundle id `com.twobetta.app`. iOS deployment target **26.2**, Swift 5 language mode.
- **`app/Secrets.swift` is gitignored and required to compile.** It defines `Secrets.URL`, `Secrets.WS`, and `Secrets.AD_UNIT_ID`. A fresh checkout will fail to build until it exists. In `#if DEBUG` it points at a LAN dev server (`http://192.168.0.14:8080`); release points at `https://api.pidulgi.com`.
- Build from CLI: `xcodebuild -project app.xcodeproj -scheme app -destination 'generic/platform=iOS' build`
- There is **no test target** — do not look for or invent `xcodebuild test` workflows.

## Architecture

The app uses MVVM with observation via `@Observable` (not `ObservableObject`). Source is under `app/`, split into four layers:

- **`Feature/<Domain>/`** — UI per domain (Chat, Member, Activity, Authentication, Point, Rank, Report, Search, Setting, Main, AdMob, Image). Each feature contains `View/`, `ViewModel/`, and a `Router/` wrapper view (e.g. `ChatNavigationView`).
- **`Service/<Domain>/`** — API clients. Each is a `.shared` singleton with `async throws` methods that build a path + parameters and delegate to `PrivateNetworkManager.shared`. This is the only place that talks HTTP.
- **`Model/`** and feature models — Codable DTOs, suffixed `…Response` / `…Request`. Generic envelopes `CursorResponse<T>` and `CursorRequest` implement keyset/cursor pagination (page size 20; carries `cursorId` + `cursorDateAt`).
- **`Common/`** — cross-cutting infrastructure: `Network/`, `Router/`, `Helper/`, `Component/` (reusable views), `Extension/`.

### Networking (`Common/Network/`)
- Two Alamofire managers: `PrivateNetworkManager.shared` (authenticated) and `PublicNetworkManager.shared` (no auth). `APIEnvironment` wraps `Secrets` for `baseURL` / `baseWS`.
- `AuthorizationInterceptor` adapts every request: injects `X-Device-Id` and `Authorization: Bearer <accessToken>`. On a 401 it retries once after `TokenRefresher.refreshIfNeeded()`.
- All error handling funnels through `APIError`. HTTP failures are decoded into `.server`/`.unauthorized`/`.network`/`.decoding`/`.cancelled`. A **401 with code `UNAUTHORIZED_03` means the user is banned** → decoded into `.ban(...)` and routed to `SessionStore.handleBan`. When catching errors in ViewModels, treat `APIError.cancelled` as a no-op (don't surface it).

### Session & top-level routing
- `SessionStore.shared` (`@Observable`, injected via `.environment`) holds `isLoggedIn` and `banInfo`. `RootView` switches on it: `banInfo` → `BanView`, else `isLoggedIn` → `RootTabView`, else `AuthenticationNavigationView`.
- Tokens / `memberId` / `deviceId` persist in `TokenStorage` (Keychain via keychain-swift). `login`/`logout`/`handleBan` mutate `SessionStore` and notify `PushManager`.
- In-feature navigation uses `AppRouter` (`@Observable`, a `[AppRoute]` path stack with `push`/`pop`/`root`) plus per-feature `…NavigationView` wrappers and `AppDestination`.

### Realtime (WebSocket / STOMP)
- `StompManager.shared` wraps `SwiftStomp` over the `Secrets.WS` socket, authenticated by Bearer header. `RootView` calls `connect()` on login and `disconnect()` on logout/ban.
- It is a subscription registry: ViewModels register a destination + handler closure (e.g. `ChatRoomViewModel` listens on `/user/queue/chat-rooms/upsert|delete|read`). Incoming messages are dispatched to handlers on `@MainActor`.

### Conventions to follow
- ViewModels are `@MainActor @Observable final class`, depend on `.shared` services, and expose a domain `…ViewState` enum (`idle/loading/empty/data/error`). They own cursor state and `isPaging`/`isLoading` flags for pagination.
- New API call → add a method to the relevant `Service/<Domain>` singleton, never call `PrivateNetworkManager` from a ViewModel directly.
- Third-party libraries in use: Alamofire (HTTP), SwiftStomp (WS), Kingfisher (remote images), Firebase (analytics — see `View+Analytics.swift` / `screen_view` events), Google Mobile Ads + AdMob SSV (rewarded ads), SwiftUI-LazyPager (image gallery).
