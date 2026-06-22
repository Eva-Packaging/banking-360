import { Routes, Route } from 'react-router-dom';
import TransactionHistoryPage from '../features/transactions/pages/TransactionHistoryPage';

function Placeholder({ title }: { title: string }) {
    return (
        <main className="p-8">
            <h1 className="text-2xl font-bold">{title}</h1>
            <p className="mt-2">Placeholder — coming soon</p>
        </main>
    );
}

export default function AppRouter() {
    return (
        <Routes>
            <Route path="/login"         element={<Placeholder title="Login" />} />
            <Route path="/register"      element={<Placeholder title="Register" />} />
            <Route path="/dashboard"     element={<Placeholder title="Dashboard" />} />
            <Route path="/accounts"      element={<Placeholder title="Accounts" />} />
            <Route path="/transfer"      element={<Placeholder title="Transfer" />} />
            <Route path="/transactions"  element={<TransactionHistoryPage />} />
            <Route path="/notifications" element={<Placeholder title="Notifications" />} />
            <Route path="/"              element={<Placeholder title="Home" />} />
        </Routes>
    );
}