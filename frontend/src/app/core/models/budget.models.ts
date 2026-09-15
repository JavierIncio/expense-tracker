export interface BudgetRequest {
  categoryId: string;
  year: number;
  month: number;
  amount: number;
}

export interface BudgetResponse {
  id: string;
  categoryId: string;
  year: number;
  month: number;
  amount: number;
}

export interface BudgetFilter {
  categoryId?: string;
  year?: number;
  month?: number;
}
