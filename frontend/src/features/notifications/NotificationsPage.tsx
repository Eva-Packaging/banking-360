import { useEffect, useState } from 'react';
import { getMyNotifications, markAsRead } from '../../api/notificationApi';
import { Notification } from '../../types/notification';


export default function NotificationsPage() {
    const [notifications, setNotifications] = useState<Notification[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        getMyNotifications()
            .then((data) => setNotifications(data))
            .catch(() => setError('Failed to load notifications'))
            .finally(() => setLoading(false));
    }, []);

    const handleMarkAsRead = (notificationId: string) => {
        markAsRead(notificationId).then((updated) => {
            setNotifications((prev) =>
                prev.map((n) => (n.notificationId === notificationId ? updated : n))
            );
        });
    };

    if (loading) return <p>Loading...</p>;
    if (error) return <p>{error}</p>;

    return (<div>
        <h1>My Notifications</h1>
        <ul>
            {notifications.map((n) => (
                <li
                    key={n.notificationId}
                    onClick={() => n.status === 'UNREAD' && handleMarkAsRead(n.notificationId)}
                    style={{ fontWeight: n.status === 'UNREAD' ? 'bold' : 'normal', cursor: 'pointer' }}
                    >
                        <p>{n.title}</p>
                        <p>{n.message}</p>
                        <p>{n.createdAt} </p>
                    </li>
            
            ))}
        </ul>
    </div>);


}
