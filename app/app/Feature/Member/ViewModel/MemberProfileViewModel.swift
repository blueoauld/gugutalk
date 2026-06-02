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
        (2...15).contains(nickname.trimmingCharacters(in: .whitespaces).count) && birthYear.count == 4 && region != nil
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
        guard (2...15).contains(trimmedNickname.count) else {
            ToastManager.shared.show("닉네임은 2자 이상 15자 이하여야 합니다.", style: .error)
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
        let images = try await compressImages(pickedImages)
        
        // 업로드 URL 생성
        let requests = UploadUrlRequests(
            urls: images.map { _ in UploadUrlRequest(contentType: "image/jpeg") }
        )
        let responses = try await createUploadUrls(requests)
        
        // 응답 개수 검증
        guard responses.urls.count == images.count else {
            throw APIError.server(
                code: "INTERNAL_SERVER_ERROR",
                message: "이미지 개수가 올바르지 않습니다.",
                statusCode: 500
            )
        }
        
        // R2 업로드
        try await uploadToR2(images: images, urls: responses.urls)
        
        return responses.urls.map { response in
            MemberImageCreateRequest(url: response.url, key: response.key)
        }
    }
    
    private func compressImages(_ pickedImages: [PickedImage]) async throws -> [Data] {
        try await withThrowingTaskGroup(of: (Int, Data).self) { group in
            for (index, picked) in pickedImages.enumerated() {
                group.addTask(priority: .userInitiated) {
                    let resized = picked.image.resized(toMaxDimension: 1024)
                    
                    guard let data = resized.jpegData(compressionQuality: 0.8) else {
                        throw APIError.server(
                            code: "INTERNAL_SERVER_ERROR",
                            message: "이미지 압축에 실패했습니다.",
                            statusCode: 500
                        )
                    }
                    return (index, data)
                }
            }
            
            var results = Array<Data?>(repeating: nil, count: pickedImages.count)
            for try await (index, data) in group {
                results[index] = data
            }
            return results.compactMap { $0 }
        }
    }
    
    private func uploadToR2(
        images: [Data],
        urls: [UploadUrlResponse],
        maxConcurrent: Int = 4
    ) async throws {
        try await withThrowingTaskGroup(of: Void.self) { group in
            var nextIndex = 0
            let total = images.count
            
            while nextIndex < min(maxConcurrent, total) {
                let image = images[nextIndex]
                let response = urls[nextIndex]
                
                group.addTask {
                    try await self.r2Service.upload(
                        data: image,
                        url: response.url,
                        contentType: "image/jpeg"
                    )
                }
                nextIndex += 1
            }
            
            while try await group.next() != nil {
                guard nextIndex < total else { continue }
                
                let image = images[nextIndex]
                let response = urls[nextIndex]
                
                group.addTask {
                    try await self.r2Service.upload(
                        data: image,
                        url: response.url,
                        contentType: "image/jpeg"
                    )
                }
                nextIndex += 1
            }
        }
    }
    
    private func resolveImages(
        _ images: [ProfileImage],
        createUploadUrls: (UploadUrlRequests) async throws -> UploadUrlResponses
    ) async throws -> [MemberImageCreateRequest] {
        // 새로 고른 것만 추출
        let picks = images.compactMap { image -> PickedImage? in
            if case .picked(let picked) = image {
                return picked
            }
            return nil
        }
        
        // 업로드
        let uploaded = try await uploadImages(picks, createUploadUrls: createUploadUrls)
        
        // 원래 순서대로 재조립
        var uploadedIterator = uploaded.makeIterator()
        return images.map { image in
            switch image {
            case .existing(let existing):
                return MemberImageCreateRequest(url: existing.url, key: existing.key)
            case .picked:
                return uploadedIterator.next()!
            }
        }
    }
}
