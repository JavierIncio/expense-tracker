export interface ErrorResponse {
  timestamp: string; // ISO 8601 format
  status: number;
  error: string;
  message: string;
  path: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

export type BudgetStatus = 'NO_BUDGET' | 'WITHIN_LIMIT' | 'EXCEEDED';

export type TransactionType = 'INCOME' | 'EXPENSE';
