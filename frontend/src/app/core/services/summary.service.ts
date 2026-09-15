import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { MonthlySummaryResponse } from '@core/models/summary.models';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SummaryService {
  private readonly http = inject(HttpClient);

  getMonthlySummary(year: number, month: number): Observable<MonthlySummaryResponse> {
    return this.http.get<MonthlySummaryResponse>(`${environment.apiUrl}/summary/monthly`, {
      params: { year, month },
    });
  }
}
