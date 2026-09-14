import { TransactionType } from './common.models';

export interface CategoryRequest {
  name: string;
  type: TransactionType;
}

export interface CategoryResponse {
  id: string;
  name: string;
  type: TransactionType;
  createdAt: string;
  updatedAt: string;
}
