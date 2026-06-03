import SwiftUI
import PhotosUI

struct ChatMessageInputBar: View {

    @Binding var message: String
    var onSend: () async -> Void
    var onSendMedia: (_ media: [PickedMedia]) async -> Void
    var isLoading: Bool
    var isUploading: Bool

    @State private var maxCount: Int = 5
    @State private var pickerItems: [PhotosPickerItem] = []
    @State private var isPreparing = false

    private var enabled: Bool {
        !message.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && !isLoading
    }

    var body: some View {
        HStack {
            PhotosPicker(
                selection: $pickerItems,
                maxSelectionCount: maxCount,
                matching: .any(of: [.images, .videos])
            ) {
                Group {
                    if isUploading || isPreparing {
                        ProgressView()
                    } else {
                        Image(systemName: "paperclip")
                            .foregroundStyle(.foreground)
                    }
                }
                .frame(width: 44, height: 44)
                .glassEffect(.regular.tint(.clear))
            }
            .disabled(isUploading || isPreparing)
            .onChange(of: pickerItems) { _, newItems in
                guard !newItems.isEmpty else { return }

                Task {
                    isPreparing = true

                    defer {
                        isPreparing = false
                        pickerItems = []
                    }


                    var picked: [PickedMedia] = []

                    for item in newItems {
                        guard picked.count < maxCount else { break }

                        if let media = await load(item) {
                            picked.append(media)
                        }
                    }

                    if !picked.isEmpty {
                        await onSendMedia(picked)
                    }
                }
            }

            TextField("메세지 입력", text: $message, axis: .vertical)
                .font(.subheadline)
                .lineLimit(1...5)
                .multilineTextAlignment(.leading)
                .padding(.leading)
                .padding(.trailing, 50)
                .padding(.vertical, 8)
                .frame(minHeight: 44)
                .overlay(alignment: .bottomTrailing) {
                    Button {
                        Task {
                            await onSend()
                        }
                    } label: {
                        Group {
                            if isLoading {
                                ProgressView()
                                    .tint(.white)
                            } else {
                                Image(systemName: "paperplane.fill")
                                    .foregroundColor(.white)
                            }
                        }
                        .frame(width: 36, height: 36)
                        .background(enabled ? Color.blue : Color(.systemGray3))
                        .clipShape(Circle())
                    }
                    .padding(.trailing, 4)
                    .padding(.bottom, 4)
                    .disabled(!enabled)
                }
                .glassEffect(
                    .regular.tint(.clear),
                    in: .rect(cornerRadius: 20)
                )
                .autocorrectionDisabled(true)
                .textInputAutocapitalization(.never)
        }
        .padding()
    }

    private func load(_ item: PhotosPickerItem) async -> PickedMedia? {
        let isVideo = item.supportedContentTypes.contains { $0.conforms(to: .movie) }

        if isVideo {
            do {
                if let video = try await item.loadTransferable(type: PickedVideo.self) {
                    let thumbnail = await makeThumbnail(url: video.url)

                    return PickedMedia(kind: .video(url: video.url, thumbnail: thumbnail))
                }
            } catch {
                ToastManager.shared.show("비디오를 불러오지 못했습니다.", style: .error)
            }
        } else {
            do {
                if let data = try await item.loadTransferable(type: Data.self), let uiImage = UIImage(data: data) {
                    return PickedMedia(kind: .image(uiImage))
                }
            } catch {
                ToastManager.shared.show("이미지를 불러오지 못했습니다.", style: .error)
            }
        }
        return nil
    }

    private func makeThumbnail(url: URL) async -> UIImage? {
        let asset = AVURLAsset(url: url)
        let generator = AVAssetImageGenerator(asset: asset)

        generator.appliesPreferredTrackTransform = true

        let time = CMTime(seconds: 0, preferredTimescale: 600)

        do {
            let cgImage = try await generator.image(at: time).image
            return UIImage(cgImage: cgImage)
        } catch {
            return nil
        }
    }
}
