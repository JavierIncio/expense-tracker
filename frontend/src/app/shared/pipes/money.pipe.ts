import { Pipe, type PipeTransform } from '@angular/core';
import { environment } from '@env/environment';

@Pipe({
  name: 'money',
})
export class MoneyPipe implements PipeTransform {
  private readonly formatter = new Intl.NumberFormat(environment.locale, {
    style: 'currency',
    currency: environment.currency,
  });

  transform(value: number | null | undefined): string {
    return value != null ? this.formatter.format(value) : '—';
  }
}
