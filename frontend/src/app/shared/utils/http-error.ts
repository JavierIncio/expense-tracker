import { HttpErrorResponse } from '@angular/common/http';
import { ErrorResponse } from '@core/models/common.models';

export function httpErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof HttpErrorResponse) {
    const body = error.error as Partial<ErrorResponse> | undefined;
    if (body?.message) return body.message;
  }
  return fallback;
}
