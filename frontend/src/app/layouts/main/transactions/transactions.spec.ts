import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Page } from '@core/models/common.models';
import { CategoryResponse } from '@core/models/category.models';
import { TransactionResponse } from '@core/models/transaction.models';
import { CategoryService } from '@core/services/category.service';
import { TransactionService } from '@core/services/transaction.service';
import { of, throwError } from 'rxjs';
import { Transactions } from './transactions';

const categories: CategoryResponse[] = [
  { id: 'c1', name: 'Alimentación', type: 'EXPENSE', createdAt: 'x', updatedAt: 'x' },
  { id: 'c2', name: 'Nómina', type: 'INCOME', createdAt: 'x', updatedAt: 'x' },
];

const page: Page<TransactionResponse> = {
  content: [
    { id: 't1', type: 'EXPENSE', amount: 12.5, categoryId: 'c1', date: '2026-09-01', createdAt: 'x' },
  ],
  totalElements: 1,
  totalPages: 1,
  size: 10,
  number: 0,
  first: true,
  last: true,
  numberOfElements: 1,
  empty: false,
};

describe('Transactions', () => {
  let fixture: ComponentFixture<Transactions>;
  let transactionMock: { list: () => unknown };
  let categoryMock: { list: () => unknown };

  beforeEach(async () => {
    transactionMock = { list: () => of(page) };
    categoryMock = { list: () => of(categories) };

    await TestBed.configureTestingModule({
      imports: [Transactions],
      providers: [
        { provide: TransactionService, useValue: transactionMock },
        { provide: CategoryService, useValue: categoryMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Transactions);
  });

  it('creates the component', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('loads transactions and renders them', () => {
    fixture.detectChanges();

    expect(fixture.componentInstance.transactions()?.content.length).toBe(1);
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(1);
    expect(fixture.nativeElement.textContent).toContain('Alimentación');
  });

  it('sets an error when the list fails', () => {
    transactionMock.list = () => throwError(() => new HttpErrorResponse({ status: 500 }));
    fixture.detectChanges();

    expect(fixture.componentInstance.error()).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.alert-error')).toBeTruthy();
  });

  it('filters expense categories for the form select', () => {
    fixture.detectChanges();
    fixture.componentInstance.onFormTypeChange({ target: { value: 'INCOME' } } as unknown as Event);

    expect(fixture.componentInstance.typeFilter()).toBe('INCOME');
    expect(fixture.componentInstance.filteredCategories().map((c) => c.id)).toEqual(['c2']);
  });
});