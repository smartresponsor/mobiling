import SwiftUI

public struct MobileAttachmentView: View {
    private let vendorId: String?
    private let attachmentFeatureBridge: AttachmentFeatureBridge?

    @State private var attachmentList: AttachmentListPayload?
    @State private var errorMessage: String?
    @State private var isLoading: Bool = false

    public init(vendorId: String?, attachmentFeatureBridge: AttachmentFeatureBridge?) {
        self.vendorId = vendorId
        self.attachmentFeatureBridge = attachmentFeatureBridge
    }

    public var body: some View {
        List {
            Section {
                Text("Attachment")
                    .font(.headline)
                Text("Vendor-owned attachment links from Attaching.")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }

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
                Section("Owner") {
                    field("Owner type", attachmentList.ownerType)
                    field("Owner ID", attachmentList.ownerId)
                    field("Count", String(attachmentList.count))
                    field("Payload", attachmentList.payloadText)
                }

                if attachmentList.items.isEmpty {
                    Section {
                        Text("No attachment yet.")
                    }
                } else {
                    Section("Files") {
                        ForEach(attachmentList.items) { item in
                            VStack(alignment: .leading, spacing: 4) {
                                Text("\(item.type ?? "attachment"): \(item.attachmentId)")
                                    .font(.subheadline)
                                    .fontWeight(.semibold)
                                field("MIME", item.mimeType)
                                field("Download URL", item.downloadUrl)
                                field("Payload", item.payloadText)
                            }
                            .padding(.vertical, 4)
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
