import SwiftUI

public struct MessageMobileScreen: View {
    private let bridge: MessageFeatureBridge?
    @State private var threads: [MessageThreadSummary] = []
    @State private var selectedThread: MessageThreadSummary?
    @State private var messages: [MessageItemPayload] = []
    @State private var query = ""
    @State private var unreadOnly = false
    @State private var draft = ""
    @State private var loading = true
    @State private var errorMessage: String?
    @State private var sending = false

    public init(messageFeatureBridge: MessageFeatureBridge?) {
        self.bridge = messageFeatureBridge
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
    }

    private var inbox: some View {
        List {
            Section {
                TextField("Search by customer name", text: $query)
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
    }

    private func conversation(_ thread: MessageThreadSummary) -> some View {
        List {
            Section {
                Button("Back to conversations") { selectedThread = nil; messages = [] }
            }
            if let errorMessage { Text(errorMessage).foregroundStyle(.secondary) }
            ForEach(messages, id: \.messageId) { message in
                VStack(alignment: .leading, spacing: 4) {
                    Text(message.body)
                    Text(message.sentAtIso8601).font(.caption2).foregroundStyle(.secondary)
                }
                .padding(.vertical, 4)
            }
            Section {
                HStack {
                    TextField("Message", text: $draft, axis: .vertical)
                    Button { Task { await send(thread) } } label: { Image(systemName: "paperplane.fill") }
                        .disabled(draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || sending)
                }
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
            errorMessage = "Messaging gateway is not available."
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
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    @MainActor private func send(_ thread: MessageThreadSummary) async {
        guard let bridge else { return }
        let body = draft.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !body.isEmpty else { return }
        sending = true
        do {
            _ = try await bridge.send(request: SendMessageRequest(threadId: thread.threadId, body: body))
            draft = ""
            messages = try await bridge.listItems(threadId: thread.threadId)
            threads = try await bridge.listThreads()
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
        sending = false
    }
}
