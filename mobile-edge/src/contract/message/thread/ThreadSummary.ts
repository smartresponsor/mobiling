// Marketing America Corp. Oleksandr Tishchenko
export interface ThreadSummary {
  threadId: string;
  subject: string | null;
  lastMessagePreview: string;
  unreadCount: number;
  updatedAtIso8601: string;
}
