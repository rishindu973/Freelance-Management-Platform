import React, { createContext, useContext, useState, useEffect } from 'react';

export type UserRole = 'MANAGER' | 'FREELANCER' | null;

interface AuthContextType {
    token: string | null;
    role: UserRole;
    isAuthenticated: boolean;
    isManager: boolean;
    isFreelancer: boolean;
    login: (token: string, role: string) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
    const [role, setRole] = useState<UserRole>(
        (localStorage.getItem('role') as UserRole) || null
    );

    useEffect(() => {
        if (token) {
            localStorage.setItem('token', token);
        } else {
            localStorage.removeItem('token');
        }
    }, [token]);

    useEffect(() => {
        if (role) {
            localStorage.setItem('role', role);
        } else {
            localStorage.removeItem('role');
        }
    }, [role]);

    const login = (newToken: string, newRole: string) => {
        setToken(newToken);
        setRole((newRole?.toUpperCase() as UserRole) || 'MANAGER');
    };

    const logout = () => {
        setToken(null);
        setRole(null);
        window.location.href = '/login';
    };

    return (
        <AuthContext.Provider value={{
            token,
            role,
            isAuthenticated: !!token,
            isManager: role === 'MANAGER',
            isFreelancer: role === 'FREELANCER',
            login,
            logout,
        }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = (): AuthContextType => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
