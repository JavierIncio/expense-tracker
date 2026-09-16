export interface NotificationResponse {
  id: string;
  message: string;
  read: boolean;
  createdAt: string;
}

export interface UnreadCountResponse {
  count: number;
}
