import { BudgetStatus, TransactionType } from './common.models';

export interface MonthlySummaryResponse {
  year: number;
  month: number;
  totalIncome: number;
  totalExpense: number;
  balance: number;
  byCategory: CategorySummary[];
}

export type CategorySummary = {
  categoryId: string;
  categoryName: string;
  type: TransactionType;
  amount: number;
  budgetAmount: number | null;
  budgetStatus: BudgetStatus;
};
