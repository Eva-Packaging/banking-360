import axiosClient from './axiosClient';
import type { Transaction, PageResponse } from '../types/transaction';

export const getMyTransactions = (
    page: number = 0,
    size: number = 10
): Promise<PageResponse<Transaction>> =>
    axiosClient
        .get<PageResponse<Transaction>>('/transactions/my', { params: { page, size } })
        .then((res) => res.data);