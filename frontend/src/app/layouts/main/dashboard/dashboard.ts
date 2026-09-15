import { Component, computed, inject, OnInit, signal } from '@angular/core';
import type { ChartConfiguration } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { finalize } from 'rxjs';
import { MonthlySummaryResponse } from '@core/models/summary.models';
import { SummaryService } from '@core/services/summary.service';
import { ErrorAlert } from '@shared/components/error-alert/error-alert';
import { MoneyPipe } from '@shared/pipes/money.pipe';
import { httpErrorMessage } from '@shared/utils/http-error';
import { currentMonth, formatMonthLabel, Month, shiftMonth } from '@shared/utils/month';

const CHART_COLORS = [
  '#6366f1',
  '#f43f5e',
  '#f59e0b',
  '#10b981',
  '#0ea5e9',
  '#8b5cf6',
  '#ec4899',
  '#14b8a6',
];

@Component({
  selector: 'app-dashboard',
  imports: [BaseChartDirective, ErrorAlert, MoneyPipe],
  templateUrl: './dashboard.html',
})
export class Dashboard implements OnInit {
  private readonly summaryService = inject(SummaryService);

  readonly month = signal<Month>(currentMonth());
  readonly summary = signal<MonthlySummaryResponse | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly monthLabel = computed(() => formatMonthLabel(this.month()));

  readonly expenseCategories = computed(() =>
    (this.summary()?.byCategory ?? []).filter((c) => c.type === 'EXPENSE'),
  );

  readonly chartData = computed<ChartConfiguration<'doughnut'>['data']>(() => {
    const categories = this.expenseCategories();
    return {
      labels: categories.map((c) => c.categoryName),
      datasets: [
        {
          data: categories.map((c) => c.amount),
          backgroundColor: categories.map((_, i) => CHART_COLORS[i % CHART_COLORS.length]),
        },
      ],
    };
  });

  readonly chartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'bottom' } },
  };

  readonly budgetRows = computed(() =>
    (this.summary()?.byCategory ?? [])
      .filter((c) => c.budgetAmount != null)
      .map((c) => {
        const percent =
          c.budgetAmount && c.budgetAmount > 0
            ? Math.min(100, Math.round((c.amount / c.budgetAmount) * 100))
            : 0;
        return { ...c, percent };
      }),
  );

  ngOnInit(): void {
    this.load();
  }

  prev(): void {
    this.month.update((m) => shiftMonth(m, -1));
    this.load();
  }

  next(): void {
    this.month.update((m) => shiftMonth(m, 1));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);

    const { year, month } = this.month();
    this.summaryService
      .getMonthlySummary(year, month)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (summary) => this.summary.set(summary),
        error: (e) => this.error.set(httpErrorMessage(e, 'No se pudo cargar el resumen.')),
      });
  }
}
