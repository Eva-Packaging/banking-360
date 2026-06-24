import axiosClient from './axiosClient';
import type { TransactionHistoryItem, PagedTransactionResponse } from '../types/transaction';

export const getMyTransactions = (
    page: number = 0,
    size: number = 10
): Promise<PagedTransactionResponse> =>
    axiosClient
        .get<PagedTransactionResponse>('/transactions/my', { params: { page, size } })
        .then((res) => res.data);