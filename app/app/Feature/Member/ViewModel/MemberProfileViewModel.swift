import SwiftUI

enum MemberProfileViewState {

    case idle
    case loading
    case data
    case error(String)
}

@MainActor
@Observable
final class MemberProfileViewModel {

    private let memberService = MemberService.shared
    private let memberImageService = MemberImageService.shared
    private let r2Service = R2Service.shared

    var state: MemberProfileViewState = .idle
    var member: MemberGetMeResponse? = nil

    var publicImages: [ProfileImage] = []
    var privateImages: [ProfileImage] = []
    var nickname = ""
    var birthYear = ""
    var region: Region?
    var bio = ""

    private(set) var isLoading = false

    var enabled: Bool {
        (2...10).contains(nickname.count) && birthYear.count == 4 && region != nil
    }

    func getMe() async {
        state = .loading

        do {
            let member = try await memberService.getMe()

            let sorted = member.images.sorted { $0.sortOrder < $1.sortOrder }
            publicImages = sorted.filter { $0.type == "PUBLIC"  }.map { .existing($0) }
            privateImages = sorted.filter { $0.type == "PRIVATE" }.map { .existing($0) }

            nickname = member.nickname
            birthYear = String(member.birthYear)
            region = member.region
            bio = member.bio

            state = .data
        } catch let error as APIError {
            state = .error(error.message)
        } catch {
            state = .error(error.localizedDescription)
        }
    }

    func update() async -> Bool {
        guard !isLoading else { return false }

        let trimmedNickname = nickname.trimmingCharacters(in: .whitespaces)
        guard (2...10).contains(trimmedNickname.count) else {
            ToastManager.shared.show("닉네임은 2자 이상 10자 이하여야 합니다.", style: .error)
            return false
        }

        guard let region else {
            ToastManager.shared.show("지역을 선택해주시길 바랍니다.", style: .error)
            return false
        }

        guard bio.count < 500 else {
            ToastManager.shared.show("자기소개는 500자 이하여야 합니다.", style: .error)
            return false
        }

        isLoading = true
        defer { isLoading = false }

        do {
            // 이미지 업로드
            async let publicImageTask = resolveImages(publicImages, createUploadUrls: memberImageService.createPublicUploadUrls)
            async let privateImageTask = resolveImages(privateImages, createUploadUrls: memberImageService.createPrivateUploadUrls)
            let (publicImages, privateImages) = try await (publicImageTask, privateImageTask)

            // 프로필 편집
            let request = MemberUpdateProfileRequest(
                publicImages: publicImages,
                privateImages: privateImages,
                nickname: trimmedNickname,
                birthYear: Int(birthYear) ?? 2000,
                region: region,
                bio: bio
            )

            try await memberService.updateProfile(request: request)

            ToastManager.shared.show("프로필이 편집되었습니다.", style: .info)
            return true
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
            return false
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
            return false
        }
    }

    private func uploadImages(
        _ pickedImages: [PickedImage],
        createUploadUrls: (UploadUrlRequests) async throws -> UploadUrlResponses
    ) async throws -> [MemberImageCreateRequest] {
        guard !pickedImages.isEmpty else { return [] }

        // 이미지 압축
        let images: [Data] = try await Task.detached(priority: .userInitiated) {
            try pickedImages.map { it in
                let resized = it.image.resized(toMaxDimension: 1024)

                guard let data = resized.jpegData(compressionQuality: 0.8) else {
                    throw APIError.server(
                        code: "INTERNAL_SERVER_ERROR",
                        message: "이미지 압축에 실패했습니다.",
                        statusCode: 500
                    )
                }
                return data
            }
        }.value

        // 업로드 URL 생성
        let requests = UploadUrlRequests(
            urls: images.map { _ in
                UploadUrlRequest(contentType: "image/jpeg")
            }
        )
        let responses = try await createUploadUrls(requests)

        // R2 업로드
        try await withThrowingTaskGroup(of: Void.self) { group in
            for (image, response) in zip(images, responses.urls) {
                group.addTask {
                    try await self.r2Service.upload(
                        data: image,
                        url: response.url,
                        contentType: "image/jpeg"
                    )
                }
            }
            try await group.waitForAll()
        }

        return responses.urls.map { it in
            MemberImageCreateRequest(url: it.url, key: it.key)
        }
    }

    private func resolveImages(
        _ images: [ProfileImage],
        createUploadUrls: (UploadUrlRequests) async throws -> UploadUrlResponses
    ) async throws -> [MemberImageCreateRequest] {
        // 새로 고른 것만 추출
        let picks = images.compactMap { image -> PickedImage? in
            if case .picked(let p) = image {
                return p
            } else {
                return nil
            }
        }

        // 기존 uploadImages 로직 그대로 사용해서 업로드
        let uploaded = try await uploadImages(picks, createUploadUrls: createUploadUrls)

        // 원래 순서대로 재조립
        var result: [MemberImageCreateRequest] = []
        var i = 0

        for image in images {
            switch image {
            case .existing(let e):
                result.append(MemberImageCreateRequest(url: e.url, key: e.key))
            case .picked:
                result.append(uploaded[i])
                i += 1
            }
        }
        return result
    }
}
