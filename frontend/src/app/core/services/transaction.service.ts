import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Page } from '@core/models/common.models';
import { TransactionFilter, TransactionRequest, TransactionResponse } from '@core/models/transaction.models';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly http = inject(HttpClient);

  list(
    filter: TransactionFilter = {},
    page = 0,
    size = 10,
  ): Observable<Page<TransactionResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'date,desc');

    if (filter.type) params = params.set('type', filter.type);
    if (filter.categoryId) params = params.set('categoryId', filter.categoryId);
    if (filter.fromDate) params = params.set('fromDate', filter.fromDate);
    if (filter.toDate) params = params.set('toDate', filter.toDate);

    return this.http.get<Page<TransactionResponse>>(`${environment.apiUrl}/transactions`, { params });
  }

  get(id: string): Observable<TransactionResponse> {
    return this.http.get<TransactionResponse>(`${environment.apiUrl}/transactions/${id}`);
  }

  create(request: TransactionRequest): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(`${environment.apiUrl}/transactions`, request);
  }

  update(id: string, request: TransactionRequest): Observable<TransactionResponse> {
    return this.http.put<TransactionResponse>(`${environment.apiUrl}/transactions/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/transactions/${id}`);
  }
}