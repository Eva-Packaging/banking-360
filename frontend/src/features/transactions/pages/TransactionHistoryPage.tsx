import { useState, useEffect } from 'react';
import type { TransactionHistoryItem, PagedTransactionResponse } from '../../../types/transaction';
import { getMyTransactions } from '../../../api/transactionClient';
import TransactionRow from '../components/TransactionRow';
import PaginationControls from '../components/PaginationControls';

export default function TransactionHistoryPage() {
    const [data, setData] = useState<PagedTransactionResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [currentPage, setCurrentPage] = useState(0);

    useEffect(() => {
        setLoading(true);
        setError(null);

        getMyTransactions(currentPage)
            .then(setData)
            .catch(() => setError('Failed to load transactions. Please try again.'))
            .finally(() => setLoading(false));
    }, [currentPage]);

    return (
        <main className="max-w-4xl mx-auto p-6">
            <h1 className="text-2xl font-bold text-gray-800 mb-6">Transaction History</h1>

            {loading && (
                <div className="flex justify-center items-center py-16" role="status" aria-label="Loading transactions">
                    <div className="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin" />
                </div>
            )}

            {!loading && error && (
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
                    {error}
                </div>
            )}

            {!loading && !error && data && (
                <div className="bg-white rounded-lg shadow overflow-hidden">
                    <table className="w-full">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                        </tr>
                        </thead>
                        <tbody>
                        {data.content.map((transaction: TransactionHistoryItem) => (
                            <TransactionRow key={transaction.transactionId} transaction={transaction} />
                        ))}
                        </tbody>
                    </table>
                    <PaginationControls
                        currentPage={currentPage}
                        totalPages={data.totalPages}
                        onPageChange={setCurrentPage}
                    />
                </div>
            )}
        </main>
    );
}