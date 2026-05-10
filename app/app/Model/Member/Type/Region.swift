enum Region: String, CaseIterable, Identifiable {

    case seoul = "서울"
    case incheon = "인천"
    case gyeonggi = "경기"
    case gangwon = "강원"
    case daejeon = "대전"
    case sejong = "세종"
    case chungbuk = "충북"
    case chungnam = "충남"
    case gwangju = "광주"
    case jeonbuk = "전북"
    case jeonnam = "전남"
    case busan = "부산"
    case daegu = "대구"
    case ulsan = "울산"
    case gyeongbuk = "경북"
    case gyeongnam = "경남"
    case jeju = "제주"
    case overseas = "해외"

    var id: String { self.rawValue }
}
