enum Region: String, CaseIterable, Identifiable, Codable {

    case seoul = "SEOUL"
    case incheon = "INCHEON"
    case gyeonggi = "GYEONGGI"
    case gangwon = "GANGWON"
    case daejeon = "DAEJEON"
    case sejong = "SEJONG"
    case chungbuk = "CHUNGBUK"
    case chungnam = "CHUNGNAM"
    case gwangju = "GWANGJU"
    case jeonbuk = "JEONBUK"
    case jeonnam = "JEONNAM"
    case busan = "BUSAN"
    case daegu = "DAEGU"
    case ulsan = "ULSAN"
    case gyeongbuk = "GYEONGBUK"
    case gyeongnam = "GYEONGNAM"
    case jeju = "JEJU"
    case overseas = "OVERSEAS"

    var id: String { self.rawValue }

    var label: String {
        switch self {
        case .seoul: return "서울"
        case .incheon: return "인천"
        case .gyeonggi: return "경기"
        case .gangwon: return "강원"
        case .daejeon: return "대전"
        case .sejong: return "세종"
        case .chungbuk: return "충북"
        case .chungnam: return "충남"
        case .gwangju: return "광주"
        case .jeonbuk: return "전북"
        case .jeonnam: return "전남"
        case .busan: return "부산"
        case .daegu: return "대구"
        case .ulsan: return "울산"
        case .gyeongbuk: return "경북"
        case .gyeongnam: return "경남"
        case .jeju: return "제주"
        case .overseas: return "해외"
        }
    }
}
