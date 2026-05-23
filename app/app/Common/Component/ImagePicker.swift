import SwiftUI
import PhotosUI

struct ImagePicker: View {

    @Binding var selectImages: [PickedImage]
    var maxCount: Int = 5

    @State private var pickerItems: [PhotosPickerItem] = []

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 10) {
                ForEach(selectImages) { item in
                    ZStack(alignment: .topTrailing) {
                        Image(uiImage: item.image)
                            .resizable()
                            .scaledToFill()
                            .frame(width: 100, height: 100)
                            .clipShape(RoundedRectangle(cornerRadius: 20))

                        Button {
                            selectImages.removeAll { $0.id == item.id }

                            if selectImages.isEmpty {
                                pickerItems.removeAll()
                            }
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .font(.title3)
                                .symbolRenderingMode(.palette)
                                .foregroundStyle(.white, .red.opacity(0.9))
                                .padding(6)
                        }
                    }
                }

                if selectImages.count < 5 {
                    PhotosPicker(
                        selection: $pickerItems,
                        maxSelectionCount: 5 - selectImages.count,
                        matching: .images
                    ) {
                        Image(systemName: "plus")
                            .font(.title)
                            .foregroundStyle(Color(.systemGray3))
                            .frame(width: 100, height: 100)
                            .background(
                                Color(.systemGray6),
                                in: RoundedRectangle(cornerRadius: 20)
                            )
                    }
                    .onChange(of: pickerItems) { _, newItems in
                        guard !newItems.isEmpty else { return }

                        Task {
                            for item in newItems {
                                guard selectImages.count < 5 else { break }

                                do {
                                    if let data = try await item.loadTransferable(type: Data.self), let uiImage = UIImage(data: data) {
                                        selectImages.append(PickedImage(image: uiImage))
                                    }
                                } catch {
                                    ToastManager.shared.show("이미지를 불러오지 못했습니다.", style: .error)
                                }
                            }

                            pickerItems = []
                        }
                    }
                }
            }
        }
    }
}
