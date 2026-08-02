import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';

export const FreelancerProtectedRoute: React.FC = () => {
    const { isAuthenticated, isFreelancer } = useAuth();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (!isFreelancer) {
        // Enforce RBAC: Non-freelancers get forcibly redirected explicitly to the main core dashboard
        return <Navigate to="/dashboard" replace />;
    }

    return <Outlet />;
};
