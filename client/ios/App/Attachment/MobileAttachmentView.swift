import SwiftUI

public struct MobileAttachmentView: View {
    private let vendorId: String?
    private let attachmentFeatureBridge: AttachmentFeatureBridge?

    @State private var attachmentList: AttachmentListPayload?
    @State private var errorMessage: String?
    @State private var isLoading: Bool = false
    @State private var category: AttachmentCategory = .all
    @State private var selectedItem: AttachmentItemPayload?

    private let columns = [GridItem(.adaptive(minimum: 150), spacing: 12)]

    public init(vendorId: String?, attachmentFeatureBridge: AttachmentFeatureBridge?) {
        self.vendorId = vendorId
        self.attachmentFeatureBridge = attachmentFeatureBridge
    }

    public var body: some View {
        List {
            if isLoading {
                Section {
                    Text("Loading attachment...")
                }
            }

            if let errorMessage {
                Section {
                    Text(errorMessage)
                }
            } else if let attachmentList {
                Section {
                    Text("\(attachmentList.count) attachments")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(AttachmentCategory.allCases) { option in
                                Button(option.label) { category = option }
                                    .buttonStyle(.bordered)
                                    .tint(category == option ? .accentColor : .secondary)
                            }
                        }
                    }
                }

                let visibleItems = attachmentList.items.filter(category.matches)
                if visibleItems.isEmpty {
                    Section {
                        ContentUnavailableView("No \(category.label.lowercased()) attachments", systemImage: "paperclip")
                    }
                } else {
                    Section("Files") {
                        LazyVGrid(columns: columns, spacing: 12) {
                            ForEach(visibleItems) { item in
                                attachmentCard(item)
                                    .onTapGesture { selectedItem = item }
                            }
                        }
                    }
                }
            } else {
                Section {
                    Text("Attachment bridge is ready.")
                }
            }

            Section {
                Button("Refresh") {
                    Task { await load() }
                }
            }
        }
        .navigationTitle("Attachment")
        .task(id: vendorId ?? "") {
            await load()
        }
        .sheet(item: $selectedItem) { item in
            NavigationStack {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        if item.type == "media", item.mediaKind == "image", let url = item.downloadUrl.flatMap(URL.init(string:)) {
                            AsyncImage(url: url) { phase in
                                switch phase {
                                case .success(let image): image.resizable().scaledToFit()
                                case .failure: ContentUnavailableView("Preview unavailable", systemImage: "photo")
                                default: ProgressView()
                                }
                            }
                            .frame(maxWidth: .infinity, minHeight: 260)
                        }
                        Text(item.displayName).font(.title2.bold())
                        Text(item.categoryLabel).foregroundStyle(.secondary)
                        if let mimeType = item.mimeType { Text(mimeType).font(.caption) }
                        if item.size > 0 { Text(ByteCountFormatter.string(fromByteCount: item.size, countStyle: .file)).font(.caption) }
                    }
                    .padding(16)
                }
                .navigationTitle("Attachment")
                .toolbar {
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Close") { selectedItem = nil }
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func attachmentCard(_ item: AttachmentItemPayload) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            if item.type == "media", item.mediaKind == "image", let url = item.downloadUrl.flatMap(URL.init(string:)) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image): image.resizable().scaledToFill()
                    case .failure: Image(systemName: "photo").font(.largeTitle)
                    default: ProgressView()
                    }
                }
                .frame(maxWidth: .infinity, minHeight: 120, maxHeight: 120)
                .clipped()
                .clipShape(RoundedRectangle(cornerRadius: 12))
            } else {
                ZStack {
                    RoundedRectangle(cornerRadius: 12).fill(.quaternary)
                    Image(systemName: item.type == "document" ? "doc.fill" : "paperclip").font(.largeTitle)
                }
                .frame(height: 120)
            }
            Text(item.displayName).font(.subheadline.weight(.semibold)).lineLimit(2)
            Text(item.categoryLabel).font(.caption).foregroundStyle(.secondary)
            if item.isPrimary { Text("Primary").font(.caption2.weight(.semibold)) }
        }
        .padding(10)
        .background(.background, in: RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(.quaternary))
    }

    private func load() async {
        attachmentList = nil
        errorMessage = nil

        guard let activeVendorId = vendorId?.trimmingCharacters(in: .whitespacesAndNewlines), !activeVendorId.isEmpty else {
            errorMessage = "Attachment require an active vendor session."
            return
        }

        guard let attachmentFeatureBridge else {
            errorMessage = "Attachment bridge is not available."
            return
        }

        isLoading = true
        defer { isLoading = false }

        do {
            attachmentList = try await attachmentFeatureBridge.list(ownerType: "vendor", ownerId: activeVendorId)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

}

private enum AttachmentCategory: String, CaseIterable, Identifiable {
    case all, media, documents, images, avatars, covers, otherImages, pdf, spreadsheets

    var id: String { rawValue }

    var label: String {
        switch self {
        case .all: "All"
        case .media: "Media"
        case .documents: "Documents"
        case .images: "Images"
        case .avatars: "Avatars"
        case .covers: "Covers"
        case .otherImages: "Other images"
        case .pdf: "PDF"
        case .spreadsheets: "Spreadsheets"
        }
    }

    func matches(_ item: AttachmentItemPayload) -> Bool {
        switch self {
        case .all: true
        case .media: item.type == "media"
        case .documents: item.type == "document"
        case .images: item.type == "media" && item.mediaKind == "image"
        case .avatars: item.type == "media" && item.mediaKind == "image" && item.context == "profile" && item.slot == "avatar"
        case .covers: item.type == "media" && item.mediaKind == "image" && item.context == "profile" && item.slot == "cover"
        case .otherImages: item.type == "media" && item.mediaKind == "image" && !(item.context == "profile" && ["avatar", "cover"].contains(item.slot ?? ""))
        case .pdf: item.type == "document" && item.documentKind == "pdf"
        case .spreadsheets: item.type == "document" && item.documentKind == "spreadsheet"
        }
    }
}

private extension AttachmentItemPayload {
    var displayName: String {
        if let title, !title.isEmpty { return title }
        if let originalName, !originalName.isEmpty { return originalName }
        return "Attachment \(attachmentId)"
    }

    var categoryLabel: String {
        if context == "profile", slot == "avatar" { return "Profile avatar" }
        if context == "profile", slot == "cover" { return "Profile cover" }
        if type == "document" { return documentKind?.replacingOccurrences(of: "_", with: " ").capitalized ?? "Document" }
        if type == "media" { return mediaKind?.capitalized ?? "Media" }
        return "Attachment"
    }
}
