import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NotificationResponse } from '@core/models/notification.models';
import { AuthService } from '@core/services/auth.service';
import { NotificationService } from '@core/services/notification.service';

@Component({
  selector: 'app-main-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './main-shell.html',
})
export class MainShell implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);

  readonly user = this.auth.currentUser;
  readonly loggingOut = signal(false);

  readonly unreadCount = this.notificationService.unreadCount;
  readonly latest = this.notificationService.latest;
  readonly markingRead = signal<string | null>(null);

  ngOnInit(): void {
    this.notificationService.refresh();
  }

  markRead(notification: NotificationResponse): void {
    if (notification.read || this.markingRead()) return;

    this.markingRead.set(notification.id);
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        this.markingRead.set(null);
        this.notificationService.refresh();
      },
      error: () => this.markingRead.set(null),
    });
  }

  onLogout(): void {
    if (this.loggingOut()) return;

    this.loggingOut.set(true);
    this.auth.logout().subscribe(() => {
      this.loggingOut.set(false);
      this.notificationService.reset();
      this.router.navigate(['/login']);
    });
  }
}
