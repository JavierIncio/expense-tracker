import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-main-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './main-shell.html',
})
export class MainShell {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly user = this.auth.currentUser;
  readonly loggingOut = signal(false);

  onLogout(): void {
    if (this.loggingOut()) return;

    this.loggingOut.set(true);
    this.auth.logout().subscribe(() => {
      this.loggingOut.set(false);
      this.router.navigate(['/login']);
    });
  }
}
