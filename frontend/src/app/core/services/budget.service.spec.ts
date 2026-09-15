import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BudgetService } from './budget.service';

describe('BudgetService', () => {
  let service: BudgetService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(BudgetService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lists budgets with two sort params and default paging', () => {
    service.list().subscribe();

    const req = http.expectOne((r) => r.url.endsWith('/api/budgets'));
    expect(req.request.params.getAll('sort')).toEqual(['year,desc', 'month,desc']);
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');

    req.flush({
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 10,
      number: 0,
      first: true,
      last: true,
      numberOfElements: 0,
      empty: true,
    });
  });

  it('passes filters and paging when provided', () => {
    service.list({ categoryId: 'c1', year: 2026, month: 9 }, 1, 25).subscribe();

    const req = http.expectOne((r) => r.url.endsWith('/api/budgets'));
    expect(req.request.params.get('categoryId')).toBe('c1');
    expect(req.request.params.get('year')).toBe('2026');
    expect(req.request.params.get('month')).toBe('9');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('25');

    req.flush({
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 25,
      number: 1,
      first: false,
      last: true,
      numberOfElements: 0,
      empty: true,
    });
  });

  it('creates, updates and deletes a budget', () => {
    const body = { categoryId: 'c1', year: 2026, month: 9, amount: 500 };
    service.create(body).subscribe();
    http.expectOne((r) => r.method === 'POST' && r.url.endsWith('/api/budgets')).flush({ id: 'b1', ...body });

    service.update('b1', body).subscribe();
    http.expectOne((r) => r.method === 'PUT' && r.url.includes('/api/budgets/b1')).flush({});

    service.delete('b1').subscribe();
    http.expectOne((r) => r.method === 'DELETE' && r.url.includes('/api/budgets/b1')).flush(null);
  });
});