import axios, { AxiosInstance } from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Types
export interface User {
    id: number;
    username: string;
    email: string;
    firstName?: string;
    lastName?: string;
    role: 'USER' | 'ADMIN';
    createdAt: string;
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    user: User;
}

export interface LoginRequest {
    username: string;
    password: string;
}

export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
    firstName?: string;
    lastName?: string;
}

class AuthService {
    private api: AxiosInstance;
    private token: string | null = null;

    constructor() {
        this.api = axios.create({
            baseURL: API_BASE_URL,
            headers: {
                'Content-Type': 'application/json',
            },
        });

        // Intercepteur pour ajouter automatiquement le token
        this.api.interceptors.request.use(
            (config) => {
                if (this.token) {
                    config.headers.Authorization = `Bearer ${this.token}`;
                }
                return config;
            },
            (error) => {
                return Promise.reject(error);
            }
        );

        // Intercepteur pour gérer les erreurs de token
        this.api.interceptors.response.use(
            (response) => response,
            async (error) => {
                const originalRequest = error.config;

                // Si erreur 401 et pas déjà en cours de retry
                if (error.response?.status === 401 && !originalRequest._retry) {
                    originalRequest._retry = true;

                    try {
                        const refreshToken = localStorage.getItem('refreshToken');
                        if (refreshToken) {
                            const response = await this.refreshToken(refreshToken);
                            this.setToken(response.accessToken);
                            localStorage.setItem('accessToken', response.accessToken);

                            // Refaire la requête originale avec le nouveau token
                            originalRequest.headers.Authorization = `Bearer ${response.accessToken}`;
                            return this.api(originalRequest);
                        }
                    } catch (refreshError) {
                        // Si le refresh échoue, rediriger vers login
                        this.removeToken();
                        localStorage.removeItem('accessToken');
                        localStorage.removeItem('refreshToken');
                        window.location.href = '/login';
                    }
                }

                return Promise.reject(error);
            }
        );
    }

    setToken(token: string) {
        this.token = token;
    }

    removeToken() {
        this.token = null;
    }

    async login(credentials: LoginRequest): Promise<AuthResponse> {
        try {
            const response = await this.api.post('/auth/login', credentials);
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur de connexion');
        }
    }

    async register(userData: RegisterRequest): Promise<AuthResponse> {
        try {
            const response = await this.api.post('/auth/register', userData);
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Erreur d\'inscription');
        }
    }

    async refreshToken(refreshToken: string): Promise<AuthResponse> {
        try {
            const response = await this.api.post('/auth/refresh', {
                refreshToken,
            });
            return response.data;
        } catch (error: any) {
            throw new Error('Erreur lors du rafraîchissement du token');
        }
    }

    async getCurrentUser(): Promise<User> {
        try {
            const response = await this.api.get('/auth/me');
            return response.data;
        } catch (error: any) {
            throw new Error('Erreur lors de la récupération du profil utilisateur');
        }
    }

    async logout(): Promise<void> {
        try {
            const refreshToken = localStorage.getItem('refreshToken');
            if (refreshToken) {
                await this.api.post('/auth/logout', { refreshToken });
            }
        } catch (error) {
            console.error('Erreur lors de la déconnexion:', error);
        } finally {
            this.removeToken();
        }
    }
}

export const authService = new AuthService();