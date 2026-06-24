export type TransactionType = 'CREDIT' | 'DEBIT';
export type TransactionStatus = 'COMPLETED' | 'PENDING' | 'FAILED';

export interface TransactionHistoryItem {
    transactionId: string;
    fromAccountId: string;
    toAccountId: string;
    amount: number;
    transactionType: TransactionType;
    status: TransactionStatus;
    description: string;
    createdAt: string;
}

export interface PagedTransactionResponse {
    content: TransactionHistoryItem[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}