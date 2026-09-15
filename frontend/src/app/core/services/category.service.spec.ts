import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CategoryService } from './category.service';

describe('CategoryService', () => {
  let service: CategoryService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CategoryService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lists all categories without a type filter', () => {
    service.list().subscribe((list) => expect(list.length).toBe(1));

    const req = http.expectOne((r) => r.url.endsWith('/api/categories'));
    expect(req.request.params.has('type')).toBe(false);
    req.flush([{ id: 'c1', name: 'Food', type: 'EXPENSE', createdAt: 'x', updatedAt: 'x' }]);
  });

  it('filters categories by type', () => {
    service.list('INCOME').subscribe();

    const req = http.expectOne((r) => r.url.endsWith('/api/categories'));
    expect(req.request.params.get('type')).toBe('INCOME');
    req.flush([]);
  });

  it('creates, updates and deletes a category', () => {
    service.create({ name: 'Food', type: 'EXPENSE' }).subscribe();
    http.expectOne((r) => r.method === 'POST' && r.url.endsWith('/api/categories')).flush({});

    service.update('c1', { name: 'Food', type: 'EXPENSE' }).subscribe();
    http.expectOne((r) => r.method === 'PUT' && r.url.includes('/api/categories/c1')).flush({});

    service.delete('c1').subscribe();
    http.expectOne((r) => r.method === 'DELETE' && r.url.includes('/api/categories/c1')).flush(null);
  });
});