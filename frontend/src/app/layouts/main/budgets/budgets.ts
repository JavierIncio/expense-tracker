import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, ElementRef, inject, OnInit, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoryResponse } from '@core/models/category.models';
import { BudgetRequest, BudgetResponse } from '@core/models/budget.models';
import { BudgetService } from '@core/services/budget.service';
import { CategoryService } from '@core/services/category.service';
import { CategoryNamePipe } from '@shared/pipes/category-name.pipe';
import { MoneyPipe } from '@shared/pipes/money.pipe';
import { ErrorAlert } from '@shared/components/error-alert/error-alert';
import { httpErrorMessage } from '@shared/utils/http-error';
import { currentMonth, formatMonthLabel, Month, monthName, shiftMonth } from '@shared/utils/month';

@Component({
  selector: 'app-budgets',
  imports: [ReactiveFormsModule, MoneyPipe, CategoryNamePipe, ErrorAlert],
  templateUrl: './budgets.html',
})
export class Budgets implements OnInit {
  private readonly budgetService = inject(BudgetService);
  private readonly categoryService = inject(CategoryService);
  private readonly fb = inject(FormBuilder);

  readonly modal = viewChild<ElementRef<HTMLDialogElement>>('modal');

  readonly monthName = monthName;

  readonly month = signal<Month>(currentMonth());
  readonly monthLabel = computed(() => formatMonthLabel(this.month()));
  readonly budgets = signal<{ content: BudgetResponse[]; totalElements: number; totalPages: number; number: number; first: boolean; last: boolean; numberOfElements: number } | null>(null);
  readonly categories = signal<CategoryResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly formError = signal<string | null>(null);
  readonly editing = signal<BudgetResponse | null>(null);

  readonly currentPage = signal(0);
  readonly months = Array.from({ length: 12 }, (_, i) => i + 1);

  readonly expenseCategories = computed(() =>
    this.categories().filter((c) => c.type === 'EXPENSE'),
  );

  readonly form = this.fb.group({
    categoryId: ['', Validators.required],
    year: [currentMonth().year, [Validators.required, Validators.min(2000), Validators.max(2100)]],
    month: [currentMonth().month, [Validators.required, Validators.min(1), Validators.max(12)]],
    amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
  });

  ngOnInit(): void {
    this.categoryService.list().subscribe((c) => this.categories.set(c));
    this.load();
  }

  prev(): void {
    this.month.update((m) => shiftMonth(m, -1));
    this.currentPage.set(0);
    this.load();
  }

  next(): void {
    this.month.update((m) => shiftMonth(m, 1));
    this.currentPage.set(0);
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);

    const { year, month } = this.month();
    this.budgetService.list({ year, month }, this.currentPage()).subscribe({
      next: (page) => this.budgets.set(page),
      error: (e) => this.error.set(httpErrorMessage(e, 'No se pudieron cargar los presupuestos.')),
      complete: () => this.loading.set(false),
    });
  }

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.load();
  }

  openCreate(): void {
    this.editing.set(null);
    const m = this.month();
    this.form.reset({
      categoryId: '',
      year: m.year,
      month: m.month,
      amount: null,
    });
    this.modal()?.nativeElement.showModal();
  }

  openEdit(b: BudgetResponse): void {
    this.editing.set(b);
    this.form.patchValue({
      categoryId: b.categoryId,
      year: b.year,
      month: b.month,
      amount: b.amount,
    });
    this.modal()?.nativeElement.showModal();
  }

  onModalClose(): void {
    this.formError.set(null);
    this.editing.set(null);
  }

  closeModal(): void {
    this.modal()?.nativeElement.close();
  }

  onSubmit(): void {
    if (this.form.invalid || this.submitting()) return;

    this.submitting.set(true);
    this.formError.set(null);

    const raw = this.form.getRawValue();
    const request: BudgetRequest = {
      categoryId: raw.categoryId!,
      year: raw.year!,
      month: raw.month!,
      amount: raw.amount!,
    };

    const action$ = this.editing()
      ? this.budgetService.update(this.editing()!.id, request)
      : this.budgetService.create(request);

    action$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modal()?.nativeElement.close();
        this.load();
      },
      error: (e: HttpErrorResponse) => {
        this.formError.set(httpErrorMessage(e, 'No se pudo guardar el presupuesto.'));
        this.submitting.set(false);
      },
    });
  }

  onDelete(b: BudgetResponse): void {
    if (!confirm('¿Eliminar este presupuesto?')) return;

    this.budgetService.delete(b.id).subscribe({
      next: () => this.load(),
      error: (e) => this.error.set(httpErrorMessage(e, 'No se pudo eliminar el presupuesto.')),
    });
  }
}