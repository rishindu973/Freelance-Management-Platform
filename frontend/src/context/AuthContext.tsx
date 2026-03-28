import React, { createContext, useContext, useState, useEffect } from 'react';

interface AuthContextType {
    token: string | null;
    role: string | null;
    isAuthenticated: boolean;
    isManager: boolean;
    isFreelancer: boolean;
    login: (token: string, role: string) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
    const [role, setRole] = useState<string | null>(localStorage.getItem('role'));

    // Sync token and role changes to LocalStorage instantly
    useEffect(() => {
        if (token && role) {
            localStorage.setItem('token', token);
            localStorage.setItem('role', role);
        } else {
            localStorage.removeItem('token');
            localStorage.removeItem('role');
        }
    }, [token, role]);

    const login = (newToken: string, newRole: string) => {
        setToken(newToken);
        setRole(newRole);
    };

    const logout = () => {
        setToken(null);
        setRole(null);
        window.location.href = '/login'; // Hard reset application state
    };

    return (
        <AuthContext.Provider value={{
            token,
            role,
            isAuthenticated: !!token,
            isManager: role === 'MANAGER',
            isFreelancer: role === 'FREELANCER',
            login,
            logout
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
