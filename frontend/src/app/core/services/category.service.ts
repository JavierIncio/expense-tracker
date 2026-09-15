import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { CategoryRequest, CategoryResponse } from '@core/models/category.models';
import { TransactionType } from '@core/models/common.models';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private readonly http = inject(HttpClient);

  list(type?: TransactionType): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(`${environment.apiUrl}/categories`, {
      params: type ? { type } : {},
    });
  }

  get(id: string): Observable<CategoryResponse> {
    return this.http.get<CategoryResponse>(`${environment.apiUrl}/categories/${id}`);
  }

  create(request: CategoryRequest): Observable<CategoryResponse> {
    return this.http.post<CategoryResponse>(`${environment.apiUrl}/categories`, request);
  }

  update(id: string, request: CategoryRequest): Observable<CategoryResponse> {
    return this.http.put<CategoryResponse>(`${environment.apiUrl}/categories/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/categories/${id}`);
  }
}