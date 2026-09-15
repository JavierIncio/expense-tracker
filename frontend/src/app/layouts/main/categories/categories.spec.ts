import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CategoryResponse } from '@core/models/category.models';
import { CategoryService } from '@core/services/category.service';
import { of, throwError } from 'rxjs';
import { Categories } from './categories';

const categories: CategoryResponse[] = [
  { id: 'c1', name: 'Alimentación', type: 'EXPENSE', createdAt: 'x', updatedAt: 'x' },
  { id: 'c2', name: 'Nómina', type: 'INCOME', createdAt: 'x', updatedAt: 'x' },
];

describe('Categories', () => {
  let fixture: ComponentFixture<Categories>;
  let categoryMock: { list: () => unknown };

  beforeEach(async () => {
    categoryMock = { list: () => of(categories) };

    await TestBed.configureTestingModule({
      imports: [Categories],
      providers: [{ provide: CategoryService, useValue: categoryMock }],
    }).compileComponents();

    fixture = TestBed.createComponent(Categories);
  });

  it('creates the component', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('loads categories and renders them', () => {
    fixture.detectChanges();

    expect(fixture.componentInstance.categories().length).toBe(2);
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(2);
    expect(fixture.nativeElement.textContent).toContain('Alimentación');
  });

  it('sets an error when the list fails', () => {
    categoryMock.list = () => throwError(() => new HttpErrorResponse({ status: 500 }));
    fixture.detectChanges();

    expect(fixture.componentInstance.error()).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.alert-error')).toBeTruthy();
  });
});