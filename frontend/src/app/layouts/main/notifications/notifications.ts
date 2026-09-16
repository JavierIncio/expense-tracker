import { Component, inject, OnInit, signal } from '@angular/core';
import { Page } from '@core/models/common.models';
import { NotificationResponse } from '@core/models/notification.models';
import { NotificationService } from '@core/services/notification.service';
import { environment } from '@env/environment';
import { ErrorAlert } from '@shared/components/error-alert/error-alert';
import { httpErrorMessage } from '@shared/utils/http-error';

@Component({
  selector: 'app-notifications',
  imports: [ErrorAlert],
  templateUrl: './notifications.html',
})
export class Notifications implements OnInit {
  private static readonly PAGE_SIZE = 10;

  private readonly notificationService = inject(NotificationService);

  readonly page = signal<Page<NotificationResponse> | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly markingId = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.notificationService.list(this.currentPage(), Notifications.PAGE_SIZE).subscribe({
      next: (page) => this.page.set(page),
      error: (e) =>
        this.error.set(httpErrorMessage(e, 'No se pudieron cargar las notificaciones.')),
      complete: () => this.loading.set(false),
    });
  }

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.load();
  }

  formatDate(iso: string): string {
    return new Intl.DateTimeFormat(environment.locale, {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(iso));
  }

  markRead(notification: NotificationResponse): void {
    if (notification.read || this.markingId()) return;

    this.markingId.set(notification.id);
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        this.markingId.set(null);
        this.notificationService.refresh();
        this.load();
      },
      error: () => this.markingId.set(null),
    });
  }
}
