import { TransactionType } from './common.models';

export interface TransactionRequest {
  type: TransactionType;
  description?: string;
  amount: number;
  categoryId: string;
  date: string;
}

export interface TransactionResponse {
  id: string;
  type: TransactionType;
  description?: string;
  amount: number;
  categoryId: string;
  date: string;
  createdAt: string;
}

export interface TransactionFilter {
  type?: TransactionType;
  categoryId?: string;
  toDate?: string;
  fromDate?: string;
}
