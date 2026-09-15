import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SummaryService } from './summary.service';

describe('SummaryService', () => {
  let service: SummaryService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SummaryService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('builds the monthly summary request with year and month', () => {
    service.getMonthlySummary(2026, 9).subscribe((summary) => expect(summary.totalIncome).toBe(100));

    const req = http.expectOne((r) => r.url.endsWith('/api/summary/monthly'));
    expect(req.request.params.get('year')).toBe('2026');
    expect(req.request.params.get('month')).toBe('9');

    req.flush({ year: 2026, month: 9, totalIncome: 100, totalExpense: 60, balance: 40, byCategory: [] });
  });
});