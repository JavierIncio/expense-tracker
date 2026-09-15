import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, inject, OnInit, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoryRequest, CategoryResponse } from '@core/models/category.models';
import { CategoryService } from '@core/services/category.service';
import { ErrorAlert } from '@shared/components/error-alert/error-alert';
import { httpErrorMessage } from '@shared/utils/http-error';

@Component({
  selector: 'app-categories',
  imports: [ReactiveFormsModule, ErrorAlert],
  templateUrl: './categories.html',
})
export class Categories implements OnInit {
  private readonly categoryService = inject(CategoryService);
  private readonly fb = inject(FormBuilder);

  readonly modal = viewChild<ElementRef<HTMLDialogElement>>('modal');

  readonly categories = signal<CategoryResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly formError = signal<string | null>(null);
  readonly editing = signal<CategoryResponse | null>(null);

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(50)]],
    type: ['EXPENSE', Validators.required],
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.categoryService.list().subscribe({
      next: (categories) => this.categories.set(categories),
      error: (e) => this.error.set(httpErrorMessage(e, 'No se pudieron cargar las categorías.')),
      complete: () => this.loading.set(false),
    });
  }

  openCreate(): void {
    this.editing.set(null);
    this.form.reset({ name: '', type: 'EXPENSE' });
    this.modal()?.nativeElement.showModal();
  }

  openEdit(c: CategoryResponse): void {
    this.editing.set(c);
    this.form.patchValue({ name: c.name, type: c.type });
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
    const request: CategoryRequest = { name: raw.name!, type: raw.type as 'INCOME' | 'EXPENSE' };

    const action$ = this.editing()
      ? this.categoryService.update(this.editing()!.id, request)
      : this.categoryService.create(request);

    action$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.modal()?.nativeElement.close();
        this.load();
      },
      error: (e: HttpErrorResponse) => {
        this.formError.set(httpErrorMessage(e, 'No se pudo guardar la categoría.'));
        this.submitting.set(false);
      },
    });
  }

  onDelete(c: CategoryResponse): void {
    if (!confirm(`¿Eliminar la categoría "${c.name}"?`)) return;

    this.categoryService.delete(c.id).subscribe({
      next: () => this.load(),
      error: (e) => this.error.set(httpErrorMessage(e, 'No se pudo eliminar la categoría.')),
    });
  }
}