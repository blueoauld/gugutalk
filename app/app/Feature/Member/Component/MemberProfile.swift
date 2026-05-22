import SwiftUI

struct MemberProfile: View {

    let member: MemberGetResponse

    @State private var currentPage = 0

    var body: some View {
        VStack {
            TabView(selection: $currentPage) {
                Image(systemName: "person.fill")
                    .font(.largeTitle)
                    .padding()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .foregroundStyle(Color(.systemGray4))
                    .background(Color(.systemGray6))
                    .tag(0)
            }
            .tabViewStyle(.page)
            .aspectRatio(4/3, contentMode: .fit)
            
            VStack(alignment: .leading) {
                VStack {
                    HStack {
                        Text(member.nickname)
                            .font(.title3.bold())
                        
                        Spacer()
                        
                        if let date = member.updatedAt.toISO8601Date() {
                            Text(date.formatted(.relative(presentation: .named)))
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                    
                    HStack {
                        Text(member.gender.label)
                        Text("·")
                        Text("\(member.age)살")
                        Text("·")
                        Text(member.region.label)
                        
                        Spacer()
                    }
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    
                    HStack {
                        HStack(spacing: 3) {
                            Image(systemName: "heart.fill")
                                .foregroundStyle(.red)
                            
                            Text("\(member.likes)")
                        }
                        
                        Text("·")
                        
                        HStack(spacing: 3) {
                            Image(systemName: "heart.slash.fill")
                                .foregroundStyle(.blue)
                            
                            Text("\(member.unlikes)")
                        }
                        
                        Text("·")
                        
                        HStack(spacing: 3) {
                            Image(systemName: "star.fill")
                                .foregroundStyle(.yellow)
                            
                            Text("\(member.reviews)")
                        }
                        
                        Spacer()
                    }
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                }
                .padding(.bottom)
                
                VStack {
                    Text(member.bio)
                        .font(.body)
                        .foregroundColor(.primary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding()
                        .background(
                            Color(.systemGray6),
                            in: RoundedRectangle(cornerRadius: 16)
                        )
                }
            }
            .padding()
        }
    }
}
