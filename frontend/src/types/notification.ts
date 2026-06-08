export interface Notification {
    notificationId: string;
    type: string;
    title: string;
    message: string;
    status: 'UNREAD' | 'READ';
    createdAt: string;
    readAt: string | null;
}