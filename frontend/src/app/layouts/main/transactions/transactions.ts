import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, ElementRef, inject, OnInit, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoryResponse } from '@core/models/category.models';
import { TransactionType } from '@core/models/common.models';
import { TransactionFilter, TransactionRequest, TransactionResponse } from '@core/models/transaction.models';
import { CategoryService } from '@core/services/category.service';
import { TransactionService } from '@core/services/transaction.service';
import { CategoryNamePipe } from '@shared/pipes/category-name.pipe';
import { MoneyPipe } from '@shared/pipes/money.pipe';
import { ErrorAlert } from '@shared/components/error-alert/error-alert';
import { httpErrorMessage } from '@shared/utils/http-error';

@Component({
  selector: 'app-transactions',
  imports: [ReactiveFormsModule, MoneyPipe, CategoryNamePipe, ErrorAlert],
  templateUrl: './transactions.html',
})
export class Transactions implements OnInit {
  private readonly transactionService = inject(TransactionService);
  private readonly categoryService = inject(CategoryService);
  private readonly fb = inject(FormBuilder);

  readonly modal = viewChild<ElementRef<HTMLDialogElement>>('modal');

  readonly transactions = signal<{ content: TransactionResponse[]; totalElements: number; totalPages: number; number: number; first: boolean; last: boolean; numberOfElements: number } | null>(null);
  readonly categories = signal<CategoryResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly formError = signal<string | null>(null);
  readonly editing = signal<TransactionResponse | null>(null);

  readonly filterType = signal<TransactionType | ''>('');
  readonly filterCategoryId = signal('');
  readonly filterFromDate = signal('');
  readonly filterToDate = signal('');
  readonly currentPage = signal(0);

  readonly typeFilter = signal<TransactionType>('EXPENSE');

  readonly filterCategories = computed(() => {
    const type = this.filterType();
    return type ? this.categories().filter((c) => c.type === type) : this.categories();
  });

  readonly filteredCategories = computed(() => {
    const type = this.typeFilter();
    return this.categories().filter((c) => c.type === type);
  });

  readonly form = this.fb.group({
    type: ['EXPENSE', Validators.required],
    categoryId: ['', Validators.required],
    amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
    date: [new Date().toISOString().slice(0, 10), Validators.required],
    description: [''],
  });

  ngOnInit(): void {
    this.categoryService.list().subscribe((c) => this.categories.set(c));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);

    const filter: TransactionFilter = {
      type: this.filterType() || undefined,
      categoryId: this.filterCategoryId() || undefined,
      fromDate: this.filterFromDate() || undefined,
      toDate: this.filterToDate() || undefined,
    };

    this.transactionService.list(filter, this.currentPage()).subscribe({
      next: (page) => this.transactions.set(page),
      error: (e) => this.error.set(httpErrorMessage(e, 'No se pudieron cargar las transacciones.')),
      complete: () => this.loading.set(false),
    });
  }

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.load();
  }

  onTypeChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as TransactionType | '';
    this.filterType.set(value);
    this.filterCategoryId.set('');
    this.currentPage.set(0);
    this.load();
  }

  onCategoryFilterChange(event: Event): void {
    this.filterCategoryId.set((event.target as HTMLSelectElement).value);
    this.currentPage.set(0);
    this.load();
  }

  onDateChange(field: 'from' | 'to', event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    if (field === 'from') this.filterFromDate.set(value);
    else this.filterToDate.set(value);
    this.currentPage.set(0);
    this.load();
  }

  clearFilters(): void {
    this.filterType.set('');
    this.filterCategoryId.set('');
    this.filterFromDate.set('');
    this.filterToDate.set('');
    this.currentPage.set(0);
    this.load();
  }

  onFormTypeChange(event: Event): void {
    const type = (event.target as HTMLSelectElement).value as TransactionType;
    this.typeFilter.set(type);
    this.form.controls.categoryId.reset('');
  }

  openCreate(): void {
    this.editing.set(null);
    this.typeFilter.set('EXPENSE');
    this.form.reset({
      type: 'EXPENSE',
      categoryId: '',
      amount: null,
      date: new Date().toISOString().slice(0, 10),
      description: '',
    });
    this.modal()?.nativeElement.showModal();
  }

  openEdit(t: TransactionResponse): void {
    this.editing.set(t);
    this.typeFilter.set(t.type);
    this.form.patchValue({
      type: t.type,
      categoryId: t.categoryId,
      amount: t.amount,
      date: t.date,
      description: t.description ?? '',
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
    const request: TransactionRequest = {
      type: raw.type as TransactionType,
      categoryId: raw.categoryId!,
      amount: raw.amount!,
      date: raw.date!,
      description: raw.description || undefined,
    };

    const action$ = this.editing()
      ? this.transactionService.update(this.editing()!.id, request)
      : this.transactionService.create(request);

    action$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modal()?.nativeElement.close();
        this.load();
      },
      error: (e: HttpErrorResponse) => {
        this.formError.set(httpErrorMessage(e, 'No se pudo guardar la transacción.'));
        this.submitting.set(false);
      },
    });
  }

  onDelete(t: TransactionResponse): void {
    if (!confirm('¿Eliminar esta transacción?')) return;

    this.transactionService.delete(t.id).subscribe({
      next: () => this.load(),
      error: (e) => this.error.set(httpErrorMessage(e, 'No se pudo eliminar la transacción.')),
    });
  }
}