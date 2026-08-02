import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';

export const ProtectedRoute = () => {
    const { isAuthenticated } = useAuth();

    if (!isAuthenticated) {
        // Redirect unauthenticated users hitting a protected workflow back to login, ignoring their destination history cleanly.
        return <Navigate to="/login" replace />;
    }

    // If authenticated, render the children routes mapped in App.tsx (<AppLayout />, <Dashboard />, etc.)
    return <Outlet />;
};
