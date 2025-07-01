// src/services/apiClient.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export interface ApiError {
    message: string;
    status?: number;
    code?: string;
    details?: any;
}

class ApiClient {
    private api: AxiosInstance;
    private isRefreshing = false;
    private failedQueue: Array<{
        resolve: (token: string) => void;
        reject: (error: any) => void;
    }> = [];

    constructor() {
        this.api = axios.create({
            baseURL: API_BASE_URL,
            headers: {
                'Content-Type': 'application/json',
            },
            timeout: 10000, // 10 secondes de timeout
        });

        this.setupInterceptors();
    }

    private setupInterceptors() {
        // Intercepteur pour ajouter le token d'authentification
        this.api.interceptors.request.use(
            (config) => {
                const token = localStorage.getItem('accessToken');
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
                return config;
            },
            (error) => {
                return Promise.reject(this.handleError(error));
            }
        );

        // Intercepteur pour gérer les réponses et erreurs
        this.api.interceptors.response.use(
            (response) => response,
            async (error) => {
                const originalRequest = error.config;

                // Gestion du refresh token pour les erreurs 401
                if (error.response?.status === 401 && !originalRequest._retry) {
                    if (this.isRefreshing) {
                        return new Promise((resolve, reject) => {
                            this.failedQueue.push({ resolve, reject });
                        }).then((token) => {
                            originalRequest.headers.Authorization = `Bearer ${token}`;
                            return this.api(originalRequest);
                        }).catch((err) => {
                            return Promise.reject(this.handleError(err));
                        });
                    }

                    originalRequest._retry = true;
                    this.isRefreshing = true;

                    try {
                        const refreshToken = localStorage.getItem('refreshToken');
                        if (refreshToken) {
                            const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
                                refreshToken
                            });

                            const { accessToken, refreshToken: newRefreshToken } = response.data;
                            localStorage.setItem('accessToken', accessToken);
                            localStorage.setItem('refreshToken', newRefreshToken);

                            // Traiter la queue des requêtes en échec
                            this.processQueue(null, accessToken);

                            originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                            return this.api(originalRequest);
                        }
                    } catch (refreshError) {
                        this.processQueue(refreshError, null);
                        this.logout();
                        return Promise.reject(this.handleError(refreshError));
                    } finally {
                        this.isRefreshing = false;
                    }
                }

                return Promise.reject(this.handleError(error));
            }
        );
    }

    private processQueue(error: any, token: string | null) {
        this.failedQueue.forEach(({ resolve, reject }) => {
            if (error) {
                reject(error);
            } else if (token) {
                resolve(token);
            }
        });

        this.failedQueue = [];
    }

    private logout() {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
    }

    private handleError(error: any): ApiError {
        if (error.response) {
            // Erreur HTTP avec réponse du serveur
            const { status, data } = error.response;
            return {
                message: data?.message || this.getDefaultErrorMessage(status),
                status,
                code: data?.code,
                details: data
            };
        } else if (error.request) {
            // Erreur réseau (pas de réponse)
            return {
                message: 'Impossible de contacter le serveur. Vérifiez votre connexion internet.',
                status: 0,
                code: 'NETWORK_ERROR'
            };
        } else {
            // Autre erreur
            return {
                message: error.message || 'Une erreur inattendue s\'est produite',
                code: 'UNKNOWN_ERROR'
            };
        }
    }

    private getDefaultErrorMessage(status: number): string {
        switch (status) {
            case 400:
                return 'Données invalides envoyées au serveur';
            case 401:
                return 'Vous devez vous connecter pour accéder à cette ressource';
            case 403:
                return 'Vous n\'avez pas les permissions pour accéder à cette ressource';
            case 404:
                return 'Ressource non trouvée';
            case 409:
                return 'Conflit avec l\'état actuel de la ressource';
            case 422:
                return 'Données invalides';
            case 429:
                return 'Trop de requêtes. Veuillez patienter avant de réessayer';
            case 500:
                return 'Erreur interne du serveur';
            case 502:
                return 'Erreur de passerelle';
            case 503:
                return 'Service temporairement indisponible';
            case 504:
                return 'Timeout de la passerelle';
            default:
                return 'Une erreur s\'est produite';
        }
    }

    // Méthodes HTTP publiques
    async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
        try {
            const response: AxiosResponse<T> = await this.api.get(url, config);
            return response.data;
        } catch (error) {
            throw error; // L'erreur est déjà traitée par l'intercepteur
        }
    }

    async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
        try {
            const response: AxiosResponse<T> = await this.api.post(url, data, config);
            return response.data;
        } catch (error) {
            throw error;
        }
    }

    async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
        try {
            const response: AxiosResponse<T> = await this.api.put(url, data, config);
            return response.data;
        } catch (error) {
            throw error;
        }
    }

    async patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
        try {
            const response: AxiosResponse<T> = await this.api.patch(url, data, config);
            return response.data;
        } catch (error) {
            throw error;
        }
    }

    async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
        try {
            const response: AxiosResponse<T> = await this.api.delete(url, config);
            return response.data;
        } catch (error) {
            throw error;
        }
    }

    // Méthodes utilitaires
    setToken(token: string) {
        localStorage.setItem('accessToken', token);
    }

    removeToken() {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
    }

    getToken(): string | null {
        return localStorage.getItem('accessToken');
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }
}

// Instance singleton
export const apiClient = new ApiClient();
export default apiClient;