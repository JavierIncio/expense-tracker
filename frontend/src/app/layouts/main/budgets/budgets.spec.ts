import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Page } from '@core/models/common.models';
import { CategoryResponse } from '@core/models/category.models';
import { BudgetResponse } from '@core/models/budget.models';
import { BudgetService } from '@core/services/budget.service';
import { CategoryService } from '@core/services/category.service';
import { currentMonth, shiftMonth } from '@shared/utils/month';
import { of, throwError } from 'rxjs';
import { Budgets } from './budgets';

const categories: CategoryResponse[] = [
  { id: 'c1', name: 'Alimentación', type: 'EXPENSE', createdAt: 'x', updatedAt: 'x' },
  { id: 'c2', name: 'Nómina', type: 'INCOME', createdAt: 'x', updatedAt: 'x' },
];

const page: Page<BudgetResponse> = {
  content: [{ id: 'b1', categoryId: 'c1', year: 2026, month: 9, amount: 500 }],
  totalElements: 1,
  totalPages: 1,
  size: 10,
  number: 0,
  first: true,
  last: true,
  numberOfElements: 1,
  empty: false,
};

describe('Budgets', () => {
  let fixture: ComponentFixture<Budgets>;
  let budgetMock: { list: () => unknown };
  let categoryMock: { list: () => unknown };

  beforeEach(async () => {
    budgetMock = { list: () => of(page) };
    categoryMock = { list: () => of(categories) };

    await TestBed.configureTestingModule({
      imports: [Budgets],
      providers: [
        { provide: BudgetService, useValue: budgetMock },
        { provide: CategoryService, useValue: categoryMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Budgets);
  });

  it('creates the component and exposes only expense categories', () => {
    fixture.detectChanges();

    expect(fixture.componentInstance.expenseCategories().length).toBe(1);
    expect(fixture.componentInstance.expenseCategories()[0].type).toBe('EXPENSE');
  });

  it('loads budgets and renders them', () => {
    fixture.detectChanges();

    expect(fixture.componentInstance.budgets()?.content.length).toBe(1);
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(1);
    expect(fixture.nativeElement.textContent).toContain('Alimentación');
  });

  it('sets an error when the list fails', () => {
    budgetMock.list = () => throwError(() => new HttpErrorResponse({ status: 500 }));
    fixture.detectChanges();

    expect(fixture.componentInstance.error()).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.alert-error')).toBeTruthy();
  });

  it('navigates back and forward between months', () => {
    const original = currentMonth();
    fixture.componentInstance.prev();
    expect(fixture.componentInstance.month()).toEqual(shiftMonth(original, -1));

    fixture.componentInstance.next();
    expect(fixture.componentInstance.month()).toEqual(original);
  });
});