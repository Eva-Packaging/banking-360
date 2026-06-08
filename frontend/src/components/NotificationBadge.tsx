import {useEffect, useState} from 'react';
import {getMyNotifications} from '../api/notificationApi';
export default function NotificationBadge() {
    const [unreadCount, setUnreadCount] = useState(0);

    useEffect(() => {
        getMyNotifications(true)
        .then((data) => setUnreadCount(data.length))
        .catch(() => setUnreadCount(0));
    }, []);

    if (unreadCount === 0) return null;

    return (
        <span style={{
            backgroundColor: 'red',
            color: 'white',
            borderRadius: '50%',
            padding: '2px 7px',
            fontSize: '12px',
            fontWeight: 'bold'
        }}>
            {unreadCount}   
        </span>
    );
}