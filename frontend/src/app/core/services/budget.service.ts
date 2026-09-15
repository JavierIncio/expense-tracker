import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Page } from '@core/models/common.models';
import { BudgetFilter, BudgetRequest, BudgetResponse } from '@core/models/budget.models';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class BudgetService {
  private readonly http = inject(HttpClient);

  list(filter: BudgetFilter = {}, page = 0, size = 10): Observable<Page<BudgetResponse>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .append('sort', 'year,desc')
      .append('sort', 'month,desc');

    if (filter.categoryId) params = params.set('categoryId', filter.categoryId);
    if (filter.year != null) params = params.set('year', filter.year);
    if (filter.month != null) params = params.set('month', filter.month);

    return this.http.get<Page<BudgetResponse>>(`${environment.apiUrl}/budgets`, { params });
  }

  get(id: string): Observable<BudgetResponse> {
    return this.http.get<BudgetResponse>(`${environment.apiUrl}/budgets/${id}`);
  }

  create(request: BudgetRequest): Observable<BudgetResponse> {
    return this.http.post<BudgetResponse>(`${environment.apiUrl}/budgets`, request);
  }

  update(id: string, request: BudgetRequest): Observable<BudgetResponse> {
    return this.http.put<BudgetResponse>(`${environment.apiUrl}/budgets/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/budgets/${id}`);
  }
}