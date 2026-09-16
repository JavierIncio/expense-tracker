import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Page } from '@core/models/common.models';
import { NotificationResponse, UnreadCountResponse } from '@core/models/notification.models';
import { environment } from '@env/environment';
import { catchError, Observable, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private static readonly LATEST_SIZE = 5;

  private readonly http = inject(HttpClient);

  readonly unreadCount = signal(0);
  readonly latest = signal<NotificationResponse[]>([]);

  list(page = 0, size = 10): Observable<Page<NotificationResponse>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'createdAt,desc');
    return this.http.get<Page<NotificationResponse>>(`${environment.apiUrl}/notifications`, {
      params,
    });
  }

  markAsRead(id: string): Observable<void> {
    return this.http.patch<void>(`${environment.apiUrl}/notifications/${id}/read`, null);
  }

  refresh(): void {
    this.http
      .get<UnreadCountResponse>(`${environment.apiUrl}/notifications/unread-count`)
      .pipe(catchError(() => of({ count: 0 })))
      .subscribe((response) => this.unreadCount.set(response.count));

    const params = new HttpParams()
      .set('page', 0)
      .set('size', NotificationService.LATEST_SIZE)
      .set('sort', 'createdAt,desc');
    this.http
      .get<Page<NotificationResponse>>(`${environment.apiUrl}/notifications`, { params })
      .pipe(
        catchError(() =>
          of({ content: [] as NotificationResponse[] } as Page<NotificationResponse>),
        ),
      )
      .subscribe((page) => this.latest.set(page.content));
  }

  reset(): void {
    this.unreadCount.set(0);
    this.latest.set([]);
  }
}
