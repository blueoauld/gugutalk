import SwiftUI

struct ActivityListRow: View {

    let memberId: Int64
    let nickname: String
    let gender: Gender
    let age: Int
    let region: Region
    var onDelete: () async -> Void

    @Environment(AppRouter.self) private var router

    @State private var deleteTrigger = false

    var body: some View {
        HStack {
            Button {
                router.push(.member(memberId))
            } label: {
                HStack {
                    Image(systemName: "person.fill")
                        .font(.title)
                        .padding()
                        .foregroundStyle(Color(.systemGray4))
                        .background(
                            Color(.systemGray6),
                            in: Circle()
                        )
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text(nickname)
                            .font(.subheadline.bold())
                        
                        HStack {
                            Text(gender.label)
                            
                            Text("·")
                            
                            Text("\(age)살")
                            
                            Text("·")
                            
                            Text(region.label)
                        }
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                    }

                    Spacer()
                }
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            
            Button {
                deleteTrigger.toggle()

                Task {
                    await onDelete()
                }
            } label: {
                Image(systemName: "trash.fill")
                    .font(.subheadline)
                    .padding()
                    .foregroundColor(.white)
                    .background(
                        .red,
                        in: Circle()
                    )
            }
            .buttonStyle(.plain)
            .sensoryFeedback(.selection, trigger: deleteTrigger)
        }
        .padding(.vertical, 4)
        .padding(.horizontal)
    }
}
