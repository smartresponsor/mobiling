import SwiftUI

public struct MessageMobileScreen: View {
    private let bridge: MessageFeatureBridge?
    private let vendorId: String?
    private let attachmentFeatureBridge: AttachmentFeatureBridge?
    @State private var threads: [MessageThreadSummary] = []
    @State private var selectedThread: MessageThreadSummary?
    @State private var messages: [MessageItemPayload] = []
    @State private var query = ""
    @State private var unreadOnly = false
    @State private var draft = ""
    @State private var loading = true
    @State private var errorMessage: String?
    @State private var sending = false
    @State private var attachmentOpen = false

    public init(messageFeatureBridge: MessageFeatureBridge?, vendorId: String? = nil, attachmentFeatureBridge: AttachmentFeatureBridge? = nil) {
        self.bridge = messageFeatureBridge
        self.vendorId = vendorId
        self.attachmentFeatureBridge = attachmentFeatureBridge
    }

    public var body: some View {
        Group {
            if let thread = selectedThread {
                conversation(thread)
            } else {
                inbox
            }
        }
        .navigationTitle(selectedThread == nil ? "Messages" : "Conversation")
        .task { await loadThreads() }
        .sheet(isPresented: $attachmentOpen) {
            NavigationStack {
                MobileAttachmentView(vendorId: vendorId, attachmentFeatureBridge: attachmentFeatureBridge)
                    .toolbar {
                        ToolbarItem(placement: .navigationBarLeading) {
                            Button("Back") { attachmentOpen = false }
                        }
                    }
            }
        }
    }

    private var inbox: some View {
        List {
            Section {
                Toggle("Unread only", isOn: $unreadOnly)
            }
            if loading {
                ProgressView("Loading conversations…")
            } else if let errorMessage {
                Text(errorMessage).foregroundStyle(.secondary)
            } else if filteredThreads.isEmpty {
                Text("No conversations yet").foregroundStyle(.secondary)
            } else {
                ForEach(filteredThreads, id: \.threadId) { thread in
                    Button {
                        selectedThread = thread
                        Task { await loadMessages(thread.threadId) }
                    } label: {
                        VStack(alignment: .leading, spacing: 5) {
                            HStack {
                                Text(thread.subject ?? "Conversation \(thread.threadId.prefix(8))").font(.headline)
                                Spacer()
                                if thread.unreadCount > 0 { Text("\(thread.unreadCount)").font(.caption).bold() }
                            }
                            Text(thread.lastMessagePreview).lineLimit(2).foregroundStyle(.secondary)
                        }
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .searchable(text: $query, prompt: "Search by customer name")
    }

    private func conversation(_ thread: MessageThreadSummary) -> some View {
        List {
            Section {
                Button("Back to conversations") { selectedThread = nil; messages = [] }
            }
            if let errorMessage {
                CanonicalStateCard(title: "Conversation is temporarily unavailable", description: errorMessage)
            }
            ForEach(messages, id: \.messageId) { message in
                CanonicalMessageBubble(
                    body: message.body,
                    timestamp: message.sentAtIso8601,
                    ownMessage: message.senderId == "self"
                )
            }
            Section {
                CanonicalMessageComposer(
                    draft: $draft,
                    sending: sending,
                    onAttach: { attachmentOpen = true },
                    onSend: { Task { await send(thread) } }
                )
            }
        }
    }

    private var filteredThreads: [MessageThreadSummary] {
        threads.filter { thread in
            (!unreadOnly || thread.unreadCount > 0) &&
            (query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ||
             [thread.subject ?? "", thread.lastMessagePreview].contains { $0.localizedCaseInsensitiveContains(query) })
        }
    }

    @MainActor private func loadThreads() async {
        guard let bridge else {
            loading = false
            errorMessage = "Messaging service is not available."
            return
        }
        do {
            threads = try await bridge.listThreads()
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
        loading = false
    }

    @MainActor private func loadMessages(_ threadId: String) async {
        guard let bridge else { return }
        do {
            messages = try await bridge.listItems(threadId: threadId)
            if let latestMessage = messages.last {
                try? await bridge.markRead(threadId: threadId, messageId: latestMessage.messageId)
            }
            if let selectedThread, selectedThread.threadId == threadId, selectedThread.unreadCount > 0 {
                let cleared = MessageThreadSummary(
                    threadId: selectedThread.threadId,
                    subject: selectedThread.subject,
                    lastMessagePreview: selectedThread.lastMessagePreview,
                    unreadCount: 0,
                    updatedAtIso8601: selectedThread.updatedAtIso8601
                )
                self.selectedThread = cleared
                threads = threads.map { $0.threadId == threadId ? cleared : $0 }
            }
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    @MainActor private func send(_ thread: MessageThreadSummary) async {
        guard let bridge else { return }
        let body = draft.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !body.isEmpty, !sending else { return }

        sending = true
        errorMessage = nil
        let optimisticId = "local-\(UUID().uuidString)"
        let optimistic = MessageItemPayload(
            messageId: optimisticId,
            threadId: thread.threadId,
            body: body,
            senderId: "self",
            sentAtIso8601: ISO8601DateFormatter().string(from: Date())
        )
        messages.append(optimistic)
        messages.sort { $0.sentAtIso8601 < $1.sentAtIso8601 }
        draft = ""

        do {
            let sent = try await bridge.send(request: SendMessageRequest(threadId: thread.threadId, body: body))
            messages = messages.map { $0.messageId == optimisticId ? sent : $0 }
            messages.sort { $0.sentAtIso8601 < $1.sentAtIso8601 }
        } catch {
            messages.removeAll { $0.messageId == optimisticId }
            if draft.isEmpty { draft = body }
            errorMessage = error.localizedDescription
        }
        sending = false
    }
}
