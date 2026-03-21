import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';

export const FreelancerProtectedRoute = () => {
    const { isAuthenticated, isFreelancer } = useAuth();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (!isFreelancer) {
        return <Navigate to="/dashboard" replace />;
    }

    return <Outlet />;
};
