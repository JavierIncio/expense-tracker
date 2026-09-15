import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LoginRequest } from '@core/models/auth.models';
import { ErrorResponse } from '@core/models/common.models';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly form = this.fb.nonNullable.group({
    login: ['', Validators.required],
    password: ['', Validators.required],
  });

  readonly submitting = signal(false);
  readonly serverError = signal<string | null>(null);

  onSubmit(): void {
    if (this.form.invalid || this.submitting()) return;

    this.submitting.set(true);
    this.serverError.set(null);

    const credentials: LoginRequest = this.form.getRawValue();

    this.auth.login(credentials).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (error: HttpErrorResponse) => {
        this.serverError.set(this.extractError(error));
        this.submitting.set(false);
      },
      complete: () => this.submitting.set(false),
    });
  }

  private extractError(error: HttpErrorResponse): string {
    const body = error.error as Partial<ErrorResponse> | undefined;
    return body?.message ?? 'No se pudo iniciar sesión. Inténtalo de nuevo.';
  }
}