import SwiftUI

struct MemberView: View {
    
    let memberId: Int64
    
    var body: some View {
        VStack {
            Text("\(memberId)")
        }
        .navigationTitle("프로필")
        .navigationBarTitleDisplayMode(.inline)
    }
}
