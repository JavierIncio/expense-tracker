import { CategoryResponse } from '@core/models/category.models';
import { CategoryNamePipe } from './category-name.pipe';

describe('CategoryNamePipe', () => {
  const pipe = new CategoryNamePipe();

  const categories: CategoryResponse[] = [
    { id: 'c1', name: 'Alimentación', type: 'EXPENSE', createdAt: 'x', updatedAt: 'x' },
    { id: 'c2', name: 'Nómina', type: 'INCOME', createdAt: 'x', updatedAt: 'x' },
  ];

  it('resolves a category id to its name', () => {
    expect(pipe.transform('c1', categories)).toBe('Alimentación');
  });

  it('returns a dash when the category is not found', () => {
    expect(pipe.transform('nope', categories)).toBe('—');
  });

  it('returns a dash when there are no categories', () => {
    expect(pipe.transform('c1', [])).toBe('—');
  });
});