export interface Notification {
    id: number;
    userId: number;
    title: string;
    message: string;
    type: 'APPLICATION' | 'JOB' | 'SYSTEM' | 'MESSAGE';
    isRead: boolean;
    createdAt: string;
    link?: string;
    icon?: string;
}
