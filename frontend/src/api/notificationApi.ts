import axiosClient from "./axiosClient";

import { Notification } from '../types/notification';

export const getMyNotifications = (unreadOnly = false): Promise<Notification[]> => {
    return axiosClient
        .get('/notifications/my', {
            params: {
                unreadOnly}})
        .then(res => res.data);
    };

export const markAsRead = (notificationId: string): Promise<Notification> => {
    return axiosClient
        .patch(`/notifications/${notificationId}/read`)
        .then((res) => res.data);
};