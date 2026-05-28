struct MemberGetPrivateImagesResponse: Decodable {
    
    let phone: String
    let images: [MemberImageResponse]
}
