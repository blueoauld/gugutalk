struct ReportCreateRequest: Encodable {

    let type: ReportType
    let reason: String
    let images: [ReportImageCreateRequest]
}
