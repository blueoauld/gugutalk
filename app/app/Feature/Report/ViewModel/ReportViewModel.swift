import SwiftUI

@MainActor
@Observable
final class ReportViewModel {
    
    private let reportService = ReportService.shared
    private let reportImageService = ReportImageService.shared
    private let r2Service = R2Service.shared
    
    var reportType: ReportType? = nil
    var selectImages: [PickedImage] = []
    var reason = ""
    
    private(set) var isLoading = false
    
    func create(memberId: Int64) async -> Result<Void, Error>? {
        guard !isLoading else { return nil }
        guard let reportType else {
            return .failure(
                APIError.server(
                    code: "INTERNAL_CLIENT_ERROR",
                    message: "신고 타입을 선택해주시길 바랍니다.",
                    statusCode: 400
                )
            )
        }
        
        let trimmed = reason.trimmingCharacters(in: .whitespaces)
        guard trimmed.count <= 500 else {
            return .failure(
                APIError.server(
                    code: "INTERNAL_CLIENT_ERROR",
                    message: "신고 사유는 500자 이하여야 합니다.",
                    statusCode: 400
                )
            )
        }
        
        isLoading = true
        defer { isLoading = false }
        
        do {
            var reportImages: [ReportImageCreateRequest] = []
            
            if !selectImages.isEmpty {
                // 이미지 압축
                let pickedImages = selectImages
                let images: [Data] = try await Task.detached(priority: .userInitiated) {
                    try pickedImages.map { it in
                        let resized = it.image.resized(toMaxDimension: 1024)
                        
                        guard let data = resized.jpegData(compressionQuality: 0.8) else {
                            throw APIError.server(
                                code: "INTERNAL_CLIENT_ERROR",
                                message: "이미지 압축에 실패했습니다.",
                                statusCode: 400
                            )
                        }
                        return data
                    }
                }.value
                
                // 신고 이미지 업로드 URL 생성
                let requests = UploadUrlRequests(
                    urls: images.map { image in
                        UploadUrlRequest(
                            contentType: "image/jpeg",
                            contentLength: Int64(image.count)
                        )
                    }
                )
                let responses = try await reportImageService.createUploadUrls(urls: requests)
                
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
                
                reportImages = responses.urls.map { it in
                    ReportImageCreateRequest(url: it.url, key: it.key)
                }
            }
            
            // 신고 접수
            let request = ReportCreateRequest(type: reportType, reason: trimmed, images: reportImages)
            try await reportService.create(memberId: memberId, request: request)
            return .success(())
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return nil
            }
            return .failure(error)
        }
    }
}
