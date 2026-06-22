import type { TransactionHistoryItem } from '../../../types/transaction';

interface Props {
    transaction: TransactionHistoryItem;
}

export default function TransactionRow({ transaction }: Props) {
    const { createdAt, description, amount, transactionType, status } = transaction;

    const formattedDate = new Date(createdAt).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
    });

    const formattedAmount = new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
    }).format(amount);

    const amountClass = transactionType === 'CREDIT' ? 'text-green-600' : 'text-red-600';
    const amountPrefix = transactionType === 'CREDIT' ? '+' : '-';

    const statusClass: Record<string, string> = {
        COMPLETED: 'bg-green-100 text-green-800',
        PENDING:   'bg-yellow-100 text-yellow-800',
        FAILED:    'bg-red-100 text-red-800',
    };

    return (
        <tr className="border-b border-gray-100 hover:bg-gray-50 transition-colors">
            <td className="px-4 py-3 text-sm text-gray-500">{formattedDate}</td>
            <td className="px-4 py-3 text-sm text-gray-800">{description}</td>
            <td className={`px-4 py-3 text-sm font-semibold ${amountClass}`}>
                {amountPrefix}{formattedAmount}
            </td>
            <td className="px-4 py-3">
        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${statusClass[status] ?? 'bg-gray-100 text-gray-800'}`}>
          {status}
        </span>
            </td>
        </tr>
    );
}