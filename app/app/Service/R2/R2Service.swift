import SwiftUI
import Alamofire

final class R2Service {
    
    static let shared = R2Service()
    
    func upload(data: Data, url: String, contentType: String) async throws {
        let request = try URLRequest(
            url: url,
            method: .put,
            headers: [
                "Content-Type": contentType
            ]
        )
        
        let response = await AF.upload(data, with: request)
            .validate()
            .serializingData(emptyResponseCodes: [200, 204])
            .response
        
        if case .failure(let error) = response.result {
            throw error
        }
    }
}
