import SwiftUI

public struct NotificationMobileScreen: View {
    private let bridge: NotificationFeatureBridge?
    @State private var items: [NotificationInboxItem] = []
    @State private var unreadCount = 0
    @State private var loading = true
    @State private var errorMessage: String?

    public init(notificationFeatureBridge: NotificationFeatureBridge?) {
        self.bridge = notificationFeatureBridge
    }

    public var body: some View {
        List {
            Section {
                Text(unreadCount == 1 ? "1 unread notification" : "\(unreadCount) unread notifications")
                    .font(.headline)
            }
            if loading {
                ProgressView("Loading notifications…")
            } else if let errorMessage {
                Text(errorMessage).foregroundStyle(.secondary)
            } else if items.isEmpty {
                Text("No notifications yet.").foregroundStyle(.secondary)
            } else {
                ForEach(items) { item in
                    Button {
                        guard item.status == "new" else { return }
                        Task { await markRead(item) }
                    } label: {
                        VStack(alignment: .leading, spacing: MobileDesignDefaults.Notification.rowGap) {
                            HStack {
                                Text(item.title).font(.headline).fontWeight(item.status == "new" ? .bold : .semibold)
                                Spacer()
                                if item.status == "new" { Text("Unread").font(.caption).foregroundStyle(.tint) }
                            }
                            if !item.body.isEmpty { Text(item.body).foregroundStyle(.primary) }
                            Text(item.createdAt).font(.caption2).foregroundStyle(.secondary)
                        }
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .navigationTitle("Notifications")
        .task { await loadInbox() }
    }

    @MainActor private func loadInbox() async {
        guard let bridge else {
            loading = false
            errorMessage = "Notification service is not available."
            return
        }
        do {
            let payload = try await bridge.inbox()
            items = payload.items
            unreadCount = payload.unreadCount
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
        loading = false
    }

    @MainActor private func markRead(_ item: NotificationInboxItem) async {
        guard let bridge else { return }
        let previousItems = items
        let previousUnreadCount = unreadCount
        items = items.map { current in
            guard current.id == item.id else { return current }
            return NotificationInboxItem(
                id: current.id,
                notificationId: current.notificationId,
                status: "read",
                title: current.title,
                body: current.body,
                priority: current.priority,
                actionUrl: current.actionUrl,
                createdAt: current.createdAt,
                readAt: current.readAt ?? "read"
            )
        }
        unreadCount = max(0, unreadCount - 1)

        do {
            unreadCount = try await bridge.markRead(ids: [item.id])
            errorMessage = nil
        } catch {
            items = previousItems
            unreadCount = previousUnreadCount
            errorMessage = error.localizedDescription
        }
    }
}
