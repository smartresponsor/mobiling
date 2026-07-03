import SwiftUI

public struct MobileAttachmentView: View {
    private let vendorId: String?
    private let attachmentFeatureBridge: AttachmentFeatureBridge?

    @State private var attachmentList: AttachmentListPayload?
    @State private var errorMessage: String?
    @State private var isLoading: Bool = false
    @State private var handoffText: String?

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
                if let handoffText {
                    Text(handoffText)
                }
                Button("Refresh") {
                    Task { await load() }
                }
                Button("Prepare Upload") {
                    Task { await prepareUpload() }
                }
                Button("Prepare File") {
                    Task { await prepareFile() }
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

    private func prepareUpload() async {
        guard let activeVendorId = vendorId?.trimmingCharacters(in: .whitespacesAndNewlines), !activeVendorId.isEmpty,
              let attachmentFeatureBridge else {
            handoffText = "Upload handoff requires an active attachment bridge and vendor session."
            return
        }

        do {
            let handoff = try await attachmentFeatureBridge.uploadHandoff(
                request: AttachmentUploadHandoffRequest(ownerType: "vendor", ownerId: activeVendorId, context: nil, slot: nil, isPrimary: false, title: nil, description: nil, altText: nil)
            )
            handoffText = "Upload handoff: \(handoff.method) \(handoff.uploadUrl) field=\(handoff.fieldName) mode=\(handoff.handoffMode)"
        } catch {
            handoffText = error.localizedDescription
        }
    }

    private func prepareFile() async {
        guard let attachmentId = attachmentList?.items.first?.attachmentId, !attachmentId.isEmpty,
              let attachmentFeatureBridge else {
            handoffText = "File handoff requires at least one attachment."
            return
        }

        do {
            let handoff = try await attachmentFeatureBridge.fileHandoff(attachmentId: attachmentId)
            handoffText = "File handoff: \(handoff.downloadUrl) mode=\(handoff.handoffMode)"
        } catch {
            handoffText = error.localizedDescription
        }
    }
}
