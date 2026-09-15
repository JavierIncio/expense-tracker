import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-error-alert',
  imports: [],
  templateUrl: './error-alert.html',
})
export class ErrorAlert {
  readonly message = input.required<string>();
  readonly showRetry = input<boolean>(false);
  readonly retry = output<void>();
}
