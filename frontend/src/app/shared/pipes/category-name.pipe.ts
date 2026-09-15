import { Pipe, PipeTransform } from '@angular/core';
import { CategoryResponse } from '@core/models/category.models';

@Pipe({ name: 'categoryName' })
export class CategoryNamePipe implements PipeTransform {
  transform(categoryId: string, categories: CategoryResponse[]): string {
    return categories.find((c) => c.id === categoryId)?.name ?? '—';
  }
}