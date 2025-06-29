import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { authService, AuthResponse, User } from '../services/authService';

interface AuthContextType {
    user: User | null;
    token: string | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (username: string, password: string) => Promise<void>;
    register: (userData: RegisterData) => Promise<void>;
    logout: () => void;
    refreshToken: () => Promise<void>;
}

interface RegisterData {
    username: string;
    email: string;
    password: string;
    firstName?: string;
    lastName?: string;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

interface AuthProviderProps {
    children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [token, setToken] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const isAuthenticated = !!user && !!token;

    // Initialisation au chargement de l'app
    useEffect(() => {
        const initAuth = async () => {
            const storedToken = localStorage.getItem('accessToken');
            if (storedToken) {
                try {
                    authService.setToken(storedToken);
                    const userInfo = await authService.getCurrentUser();
                    setUser(userInfo);
                    setToken(storedToken);
                } catch (error) {
                    console.error('Erreur lors de la vérification du token:', error);
                    // Token invalide, on le supprime
                    localStorage.removeItem('accessToken');
                    localStorage.removeItem('refreshToken');
                }
            }
            setIsLoading(false);
        };

        initAuth();
    }, []);

    const login = async (username: string, password: string) => {
        setIsLoading(true);
        try {
            const response: AuthResponse = await authService.login({ username, password });

            // Stocker les tokens
            localStorage.setItem('accessToken', response.accessToken);
            localStorage.setItem('refreshToken', response.refreshToken);

            // Mettre à jour l'état
            authService.setToken(response.accessToken);
            setUser(response.user);
            setToken(response.accessToken);
        } catch (error) {
            console.error('Erreur de connexion:', error);
            throw error;
        } finally {
            setIsLoading(false);
        }
    };

    const register = async (userData: RegisterData) => {
        setIsLoading(true);
        try {
            const response: AuthResponse = await authService.register(userData);

            // Stocker les tokens
            localStorage.setItem('accessToken', response.accessToken);
            localStorage.setItem('refreshToken', response.refreshToken);

            // Mettre à jour l'état
            authService.setToken(response.accessToken);
            setUser(response.user);
            setToken(response.accessToken);
        } catch (error) {
            console.error('Erreur d\'inscription:', error);
            throw error;
        } finally {
            setIsLoading(false);
        }
    };

    const logout = () => {
        // Nettoyer le stockage local
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');

        // Nettoyer l'état
        authService.removeToken();
        setUser(null);
        setToken(null);
    };

    const refreshToken = async () => {
        const storedRefreshToken = localStorage.getItem('refreshToken');
        if (!storedRefreshToken) {
            logout();
            return;
        }

        try {
            const response = await authService.refreshToken(storedRefreshToken);

            // Mettre à jour les tokens
            localStorage.setItem('accessToken', response.accessToken);
            localStorage.setItem('refreshToken', response.refreshToken);

            authService.setToken(response.accessToken);
            setToken(response.accessToken);
            setUser(response.user);
        } catch (error) {
            console.error('Erreur lors du rafraîchissement du token:', error);
            logout();
        }
    };

    const value: AuthContextType = {
        user,
        token,
        isAuthenticated,
        isLoading,
        login,
        register,
        logout,
        refreshToken,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};