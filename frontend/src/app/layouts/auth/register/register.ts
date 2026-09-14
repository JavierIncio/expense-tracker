import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { RegisterRequest } from '@core/models/auth.models';
import { ErrorResponse } from '@core/models/common.models';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    firstName: [''],
    lastName: [''],
  });

  readonly submitting = signal(false);
  readonly serverError = signal<string | null>(null);

  onSubmit(): void {
    if (this.form.invalid || this.submitting()) return;

    this.submitting.set(true);
    this.serverError.set(null);

    const { firstName, lastName, ...rest } = this.form.getRawValue();
    const payload: RegisterRequest = {
      ...rest,
      firstName: firstName || undefined,
      lastName: lastName || undefined,
    };

    this.auth.register(payload).subscribe({
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
    return body?.message ?? 'No se pudo completar el registro. Inténtalo de nuevo.';
  }
}