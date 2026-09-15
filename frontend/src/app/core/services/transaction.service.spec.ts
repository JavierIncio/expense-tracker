import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TransactionService } from './transaction.service';

describe('TransactionService', () => {
  let service: TransactionService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TransactionService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lists transactions with default paging and server-side sort', () => {
    service.list().subscribe((page) => expect(page.content.length).toBe(1));

    const req = http.expectOne((r) => r.url.endsWith('/api/transactions'));
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');
    expect(req.request.params.get('sort')).toBe('date,desc');
    expect(req.request.params.has('type')).toBe(false);

    req.flush({
      content: [{ id: 't1', type: 'EXPENSE', amount: 10, categoryId: 'c1', date: '2026-09-01', createdAt: 'x' }],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0,
      first: true,
      last: true,
      numberOfElements: 1,
      empty: false,
    });
  });

  it('passes filters and paging when provided', () => {
    service
      .list({ type: 'INCOME', categoryId: 'c1', fromDate: '2026-09-01', toDate: '2026-09-30' }, 2, 20)
      .subscribe();

    const req = http.expectOne((r) => r.url.endsWith('/api/transactions'));
    expect(req.request.params.get('type')).toBe('INCOME');
    expect(req.request.params.get('categoryId')).toBe('c1');
    expect(req.request.params.get('fromDate')).toBe('2026-09-01');
    expect(req.request.params.get('toDate')).toBe('2026-09-30');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('20');

    req.flush({
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 20,
      number: 2,
      first: false,
      last: true,
      numberOfElements: 0,
      empty: true,
    });
  });

  it('creates a transaction with the request body', () => {
    const body = { type: 'EXPENSE' as const, amount: 25, categoryId: 'c1', date: '2026-09-01' };
    service.create(body).subscribe();

    const req = http.expectOne((r) => r.method === 'POST' && r.url.endsWith('/api/transactions'));
    expect(req.request.body).toEqual(body);
    req.flush({ id: 't1', ...body, createdAt: 'x' });
  });

  it('updates and deletes a transaction', () => {
    service.update('t1', { type: 'EXPENSE', amount: 1, categoryId: 'c1', date: '2026-09-01' }).subscribe();
    http.expectOne((r) => r.method === 'PUT' && r.url.includes('/api/transactions/t1')).flush({});

    service.delete('t1').subscribe();
    http.expectOne((r) => r.method === 'DELETE' && r.url.includes('/api/transactions/t1')).flush(null);
  });
});