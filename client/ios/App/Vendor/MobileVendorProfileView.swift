import SwiftUI
import PhotosUI
import UIKit

public struct MobileVendorProfileView: View {
    private let vendorId: String?
    private let vendorProfileGateway: VendorProfileGateway?
    private let attachmentFeatureBridge: AttachmentFeatureBridge?

    @State private var profile: MobileVendorProfileScreenContract?
    @State private var errorMessage: String?
    @State private var mediaSlot: String?
    @State private var mediaItems: [AttachmentItemPayload] = []
    @State private var mediaError: String?
    @State private var mediaLoading = false
    @State private var photoPickerPresented = false

    public init(vendorId: String?, vendorProfileGateway: VendorProfileGateway?, attachmentFeatureBridge: AttachmentFeatureBridge? = nil) {
        self.vendorId = vendorId
        self.vendorProfileGateway = vendorProfileGateway
        self.attachmentFeatureBridge = attachmentFeatureBridge
    }

    public var body: some View {
        List {
            if let errorMessage {
                Section {
                    Text(errorMessage)
                }
            } else if let profile {
                Section {
                    profileMediaHeader(profile)
                        .listRowInsets(EdgeInsets())
                        .listRowBackground(Color.clear)
                }

                Section {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(profile.title)
                            .font(.title2.bold())
                        if let brand = profile.brandName, !brand.isEmpty, brand != profile.title {
                            Text(brand)
                                .font(.body)
                                .foregroundStyle(.secondary)
                        }
                        Text(profile.publicationStatus.isEmpty ? profile.status : profile.publicationStatus)
                            .font(.caption.weight(.semibold))
                            .padding(.horizontal, 10)
                            .padding(.vertical, 5)
                            .background(.thinMaterial, in: Capsule())
                    }
                }

                if let about = profile.about, !about.isEmpty {
                    Section("About") {
                        Text(about)
                    }
                }

                if let website = profile.website, !website.isEmpty {
                    Section("Website") {
                        Text(website)
                    }
                }

                Section("Activity") {
                    Text("Posts and vendor updates will appear here.")
                        .foregroundStyle(.secondary)
                }
            } else {
                Section {
                    Text("Loading vendor profile...")
                }
            }
        }
        .navigationTitle("My Profile")
        .task(id: vendorId ?? "") {
            await load()
        }
        .sheet(
            isPresented: Binding(
                get: { mediaSlot != nil },
                set: { if !$0 { mediaSlot = nil } }
            )
        ) {
            mediaLibrarySheet
        }
        .sheet(isPresented: $photoPickerPresented) {
            ProfilePhotoPicker { image in
                photoPickerPresented = false
                guard let image else { return }
                Task { await uploadPickedImage(image) }
            }
        }
    }

    @ViewBuilder
    private func profileMediaHeader(_ profile: MobileVendorProfileScreenContract) -> some View {
        ZStack(alignment: .bottomLeading) {
            AsyncImage(url: profile.coverUrl.flatMap(URL.init(string:))) { image in
                image.resizable().scaledToFill()
            } placeholder: {
                Color(.secondarySystemBackground)
            }
            .frame(height: 190)
            .frame(maxWidth: .infinity)
            .clipped()
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))

            if profile.canEditProfileMedia {
                Button {
                    openMedia(slot: "cover")
                } label: {
                    Image(systemName: "pencil")
                        .font(.system(size: 18, weight: .semibold))
                        .frame(width: 38, height: 38)
                        .background(.ultraThinMaterial, in: Circle())
                }
                .buttonStyle(.plain)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomTrailing)
                .padding(14)
            }

            ZStack(alignment: .bottomTrailing) {
                AsyncImage(url: profile.avatarUrl.flatMap(URL.init(string:))) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Color(.secondarySystemBackground)
                }
                .frame(width: 112, height: 112)
                .clipShape(Circle())
                .overlay(Circle().stroke(Color(.systemBackground), lineWidth: 5))

                if profile.canEditProfileMedia {
                    Button {
                        openMedia(slot: "avatar")
                    } label: {
                        Image(systemName: "pencil")
                            .font(.system(size: 16, weight: .semibold))
                            .frame(width: 36, height: 36)
                            .background(.ultraThinMaterial, in: Circle())
                    }
                    .buttonStyle(.plain)
                    .offset(x: 8, y: 8)
                }
            }
            .offset(x: 20, y: 56)
        }
        .frame(height: 252)
        .padding(.horizontal, 16)
    }

    private var mediaLibrarySheet: some View {
        NavigationStack {
            Group {
                if mediaLoading {
                    ProgressView("Loading media library…")
                } else if let mediaError {
                    ContentUnavailableView("Media unavailable", systemImage: "photo.badge.exclamationmark", description: Text(mediaError))
                } else if mediaItems.isEmpty {
                    ContentUnavailableView("No images", systemImage: "photo.on.rectangle.angled", description: Text("Upload an image from the web or attachment screen first."))
                } else {
                    ScrollView {
                        LazyVGrid(columns: [GridItem(.adaptive(minimum: 150), spacing: 12)], spacing: 12) {
                            ForEach(mediaItems) { item in
                                Button {
                                    Task { await select(item) }
                                } label: {
                                    VStack(alignment: .leading, spacing: 8) {
                                        AsyncImage(url: item.downloadUrl.flatMap(URL.init(string:))) { image in
                                            image.resizable().scaledToFill()
                                        } placeholder: {
                                            Color(.secondarySystemBackground)
                                        }
                                        .frame(height: 130)
                                        .frame(maxWidth: .infinity)
                                        .clipped()
                                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                                        Text("Attachment \(item.attachmentId)")
                                            .font(.caption)
                                            .lineLimit(1)
                                    }
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle(mediaSlot == "avatar" ? "Choose Avatar" : "Choose Cover")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { mediaSlot = nil }
                }
                ToolbarItem(placement: .primaryAction) {
                    Button("Choose Photo") {
                        photoPickerPresented = true
                    }
                    .disabled(mediaLoading)
                }
            }
        }
    }

    private func openMedia(slot: String) {
        mediaSlot = slot
        mediaItems = []
        mediaError = nil
        mediaLoading = true
        Task {
            defer { mediaLoading = false }
            guard let vendorId, !vendorId.isEmpty, let attachmentFeatureBridge else {
                mediaError = "Profile media requires an active attachment bridge and vendor session."
                return
            }
            do {
                mediaItems = try await attachmentFeatureBridge.list(ownerType: "vendor", ownerId: vendorId, context: "profile", slot: nil).items
                    .filter { $0.mimeType?.hasPrefix("image/") == true && $0.downloadUrl?.isEmpty == false }
            } catch {
                mediaError = error.localizedDescription
            }
        }
    }

    private func select(_ item: AttachmentItemPayload) async {
        guard let slot = mediaSlot,
              let vendorId,
              let attachmentFeatureBridge,
              let attachmentId = Int64(item.attachmentId)
        else {
            mediaError = "This attachment cannot be selected."
            return
        }
        do {
            _ = try await attachmentFeatureBridge.attach(
                request: AttachmentLinkRequest(
                    attachmentId: attachmentId,
                    ownerType: "vendor",
                    ownerId: vendorId,
                    context: "profile",
                    slot: slot,
                    position: 0,
                    isPrimary: true
                )
            )
            await load()
            mediaSlot = nil
        } catch {
            mediaError = error.localizedDescription
        }
    }

    private func uploadPickedImage(_ image: UIImage) async {
        guard let slot = mediaSlot,
              let vendorId,
              let attachmentFeatureBridge,
              vendorProfileGateway != nil
        else {
            mediaError = "Profile upload requires an active vendor session."
            return
        }
        mediaLoading = true
        mediaError = nil
        defer { mediaLoading = false }
        let aspectRatio: CGFloat = slot == "avatar" ? 1 : 8 / 3
        guard let cropped = image.centerCropped(to: aspectRatio),
              let jpeg = cropped.jpegData(compressionQuality: 0.92)
        else {
            mediaError = "Unable to crop the selected image."
            return
        }
        do {
            _ = try await attachmentFeatureBridge.upload(
                request: AttachmentUploadHandoffRequest(
                    ownerType: "vendor",
                    ownerId: vendorId,
                    context: "profile",
                    slot: slot,
                    isPrimary: true,
                    title: slot == "avatar" ? "Profile avatar" : "Profile cover",
                    description: nil,
                    altText: slot == "avatar" ? "Vendor profile avatar" : "Vendor profile cover"
                ),
                fileName: "vendor-\(slot)-\(Int(Date().timeIntervalSince1970)).jpg",
                mimeType: "image/jpeg",
                data: jpeg
            )
            await load()
            mediaSlot = nil
        } catch {
            mediaError = error.localizedDescription
        }
    }

    @ViewBuilder
    private func field(_ label: String, _ value: String?) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
            Text(value?.isEmpty == false ? value! : "—")
        }
    }

    private func load() async {
        profile = nil
        errorMessage = nil

        guard let vendorId, !vendorId.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            errorMessage = "Profile requires an active vendor session."
            return
        }

        guard let vendorProfileGateway else {
            errorMessage = "Vendor profile service is not available."
            return
        }

        do {
            let payload = try await LoadVendorProfileUseCase(gateway: vendorProfileGateway)(vendorId: vendorId)
            profile = MobileVendorProfileScreenContract(payload: payload)
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}

private struct ProfilePhotoPicker: UIViewControllerRepresentable {
    let completion: (UIImage?) -> Void

    func makeCoordinator() -> Coordinator { Coordinator(completion: completion) }

    func makeUIViewController(context: Context) -> PHPickerViewController {
        var configuration = PHPickerConfiguration(photoLibrary: .shared())
        configuration.filter = .images
        configuration.selectionLimit = 1
        let picker = PHPickerViewController(configuration: configuration)
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: PHPickerViewController, context: Context) {}

    final class Coordinator: NSObject, PHPickerViewControllerDelegate {
        private let completion: (UIImage?) -> Void
        init(completion: @escaping (UIImage?) -> Void) { self.completion = completion }

        func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
            guard let provider = results.first?.itemProvider, provider.canLoadObject(ofClass: UIImage.self) else {
                completion(nil)
                return
            }
            provider.loadObject(ofClass: UIImage.self) { object, _ in
                DispatchQueue.main.async { self.completion(object as? UIImage) }
            }
        }
    }
}

private extension UIImage {
    func centerCropped(to aspectRatio: CGFloat) -> UIImage? {
        guard aspectRatio > 0 else { return nil }
        let normalizedSize = size
        let currentRatio = normalizedSize.width / normalizedSize.height
        let cropSize: CGSize
        if currentRatio > aspectRatio {
            cropSize = CGSize(width: normalizedSize.height * aspectRatio, height: normalizedSize.height)
        } else {
            cropSize = CGSize(width: normalizedSize.width, height: normalizedSize.width / aspectRatio)
        }
        let origin = CGPoint(x: (normalizedSize.width - cropSize.width) / 2, y: (normalizedSize.height - cropSize.height) / 2)
        let format = UIGraphicsImageRendererFormat.default()
        format.scale = scale
        let renderer = UIGraphicsImageRenderer(size: cropSize, format: format)
        return renderer.image { _ in
            draw(at: CGPoint(x: -origin.x, y: -origin.y))
        }
    }
}
